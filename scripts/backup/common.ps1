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

function Assert-ExitCode($what) {
    if ($LASTEXITCODE -ne 0) {
        throw "$what falló (código $LASTEXITCODE)."
    }
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
