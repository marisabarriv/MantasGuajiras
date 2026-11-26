# Funciones compartidas por los scripts de copias de seguridad.

$ErrorActionPreference = 'Stop'

function Get-BackupConfig {

    $configFile = Join-Path $PSScriptRoot 'backup.config.ps1'

    if (-not (Test-Path $configFile)) {
        throw "Falta $configFile. Copia backup.config.example.ps1 como backup.config.ps1 y ajústalo."
    }

    . $configFile

    foreach ($tool in 'pg_dump.exe', 'pg_restore.exe', 'psql.exe', 'createdb.exe', 'dropdb.exe') {
        if (-not (Test-Path (Join-Path $BackupConfig.PgBin $tool))) {
            throw "No se encontró $tool en '$($BackupConfig.PgBin)'. Revisa PgBin en backup.config.ps1."
        }
    }

    if (-not (Test-Path $BackupConfig.BackupDir)) {
        New-Item -ItemType Directory -Path $BackupConfig.BackupDir | Out-Null
    }

    return $BackupConfig
}

function Get-PgTool($cfg, $name) {
    return Join-Path $cfg.PgBin "$name.exe"
}

# Argumentos de conexión comunes. --no-password: si falta pgpass.conf
# falla enseguida en lugar de quedarse esperando una contraseña.
function Get-ConnArgs($cfg) {
    return @('--host', $cfg.DbHost, '--port', "$($cfg.DbPort)", '--username', $cfg.DbUser, '--no-password')
}

function Write-Log($cfg, $message) {
    $line = "{0}  {1}" -f (Get-Date -Format 'yyyy-MM-dd HH:mm:ss'), $message
    Write-Host $line
    Add-Content -Path (Join-Path $cfg.BackupDir 'backup.log') -Value $line -Encoding UTF8
}

<#
  Verifica la huella SHA-256 de una copia. Sin el archivo .sha256 NO se
  puede saber si la copia está dañada o fue modificada, así que falla,
  salvo que se pida expresamente con -AllowMissingHash (copias antiguas).
#>
function Assert-BackupHash($cfg, $file, [bool] $allowMissing = $false) {
    $hashFile = "$file.sha256"
    if (-not (Test-Path $hashFile)) {
        if ($allowMissing) {
            Write-Log $cfg 'ADVERTENCIA: la copia no tiene .sha256; se continúa porque se indicó -AllowMissingHash.'
            return
        }
        throw "Falta $hashFile. Sin huella no se puede comprobar que la copia esté sana. Usa otra copia o, si sabes que es buena, repite con -AllowMissingHash."
    }
    $expected = (Get-Content $hashFile -Raw).Trim()
    $actual = (Get-FileHash -Algorithm SHA256 $file).Hash
    if ($expected -ne $actual) {
        throw 'La huella SHA-256 no coincide: el archivo está dañado o fue modificado. Usa otra copia.'
    }
    Write-Log $cfg 'Huella SHA-256 correcta.'
}

<#
  Borra las copias más antiguas que RetentionDays de una carpeta, pero
  conserva siempre las MinKeep más recientes. Se aplica a la carpeta
  principal y a la segunda copia (CopyTo).
#>
function Remove-OldBackups($cfg, $dir) {
    if (-not $dir -or -not (Test-Path $dir)) { return }
    $limit = (Get-Date).AddDays(-$cfg.RetentionDays)
    Get-ChildItem $dir -Filter 'mantas_*.dump' |
        Sort-Object LastWriteTime -Descending |
        Select-Object -Skip $cfg.MinKeep |
        Where-Object { $_.LastWriteTime -lt $limit } |
        ForEach-Object {
            Remove-Item $_.FullName, "$($_.FullName).sha256" -ErrorAction SilentlyContinue
            Write-Log $cfg "Copia antigua eliminada: $($_.FullName)"
        }
}

function Assert-ExitCode($what) {
    if ($LASTEXITCODE -ne 0) {
        throw "$what falló (código $LASTEXITCODE)."
    }
}

<#
  Si la segunda copia (CopyTo) está dentro de OneDrive, comprueba que
  OneDrive esté abierto: si está cerrado, las copias quedan en la carpeta
  pero no suben a la nube. Lo abre si hace falta y, si no puede, deja la
  ALERTA en el Escritorio. La copia local sigue siendo válida: no falla.

  La subida en sí no se comprueba: el estado de sincronización que
  informa Windows marcó "pendiente" archivos que ya estaban en la nube
  (prueba del 26/11/2026). Se revisa a mano en onedrive.com (docs/09).
#>
function Confirm-OneDriveRunning($cfg) {
    if (-not $cfg.CopyTo) { return }

    $target = [IO.Path]::GetFullPath($cfg.CopyTo).TrimEnd('\') + '\'
    $inOneDrive = @($env:OneDrive, $env:OneDriveConsumer, $env:OneDriveCommercial) |
        Where-Object { $_ } |
        Where-Object { $target.StartsWith([IO.Path]::GetFullPath($_).TrimEnd('\') + '\', [StringComparison]::OrdinalIgnoreCase) }
    if (-not $inOneDrive) { return }

    if (Get-Process OneDrive -ErrorAction SilentlyContinue) {
        Write-Log $cfg 'OneDrive está abierto: subirá la segunda copia a la nube.'
        return
    }

    $exe = @(
        "$env:LOCALAPPDATA\Microsoft\OneDrive\OneDrive.exe",
        "$env:ProgramFiles\Microsoft OneDrive\OneDrive.exe",
        "${env:ProgramFiles(x86)}\Microsoft OneDrive\OneDrive.exe"
    ) | Where-Object { $_ -and (Test-Path $_) } | Select-Object -First 1

    if ($exe) {
        # Si Windows no deja abrirlo, no debe interrumpir la copia: se
        # anota y se sigue hasta la ALERTA de abajo.
        try {
            Start-Process $exe -ArgumentList '/background'
            Start-Sleep -Seconds 10
        }
        catch {
            Write-Log $cfg "No se pudo abrir OneDrive: $($_.Exception.Message)"
        }
        if (Get-Process OneDrive -ErrorAction SilentlyContinue) {
            Write-Log $cfg 'ADVERTENCIA: OneDrive estaba cerrado; se abrió para que suba las copias a la nube.'
            return
        }
    }

    $message = "OneDrive está cerrado y no se pudo abrir. La segunda copia quedó en $($cfg.CopyTo), " +
        'pero no sube a la nube hasta que abras OneDrive (la nube junto al reloj).'
    Write-Log $cfg "ADVERTENCIA: $message"
    Write-FailureAlert $cfg $message
}

# Las tareas programadas corren ocultas: si algo falla, se deja un
# archivo de aviso en el Escritorio para que no pase desapercibido.
function Write-FailureAlert($cfg, $message) {
    try {
        $desktop = [Environment]::GetFolderPath('Desktop')
        $alert = Join-Path $desktop 'ALERTA - Copia de seguridad MantasGuajiras.txt'
        $text = @"
$(Get-Date -Format 'yyyy-MM-dd HH:mm')
$message

Revisa el historial en: $(Join-Path $cfg.BackupDir 'backup.log')
Cuando lo soluciones, borra este archivo.
"@
        Set-Content -Path $alert -Value $text -Encoding UTF8
    }
    catch {
        # Si ni siquiera se puede escribir el aviso, queda en backup.log.
    }
}
