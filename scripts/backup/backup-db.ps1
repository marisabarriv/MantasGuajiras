<#
.SYNOPSIS
    Crea una copia de seguridad completa de la base de MantasGuajiras.

.DESCRIPTION
    - Genera un archivo .dump (formato personalizado de pg_dump, comprimido).
    - Comprueba que el archivo se pueda leer (pg_restore --list).
    - Guarda su huella SHA-256 para detectar archivos dañados.
    - Borra las copias viejas según RetentionDays / MinKeep, también en CopyTo.
    - Opcional: deja una segunda copia en CopyTo (OneDrive, USB...).

    Uso:  powershell -ExecutionPolicy Bypass -File scripts\backup\backup-db.ps1
#>

. (Join-Path $PSScriptRoot 'common.ps1')

$cfg = Get-BackupConfig

try {
    $stamp = Get-Date -Format 'yyyy-MM-dd_HHmmss'
    $file  = Join-Path $cfg.BackupDir "mantas_$stamp.dump"

    Write-Log $cfg "Iniciando copia de '$($cfg.Database)' -> $file"

    $connArgs = Get-ConnArgs $cfg
    & (Get-PgTool $cfg 'pg_dump') @connArgs --format=custom --compress=9 --file $file $cfg.Database
    Assert-ExitCode 'pg_dump'

    # El archivo debe poder leerse completo.
    & (Get-PgTool $cfg 'pg_restore') --list $file | Out-Null
    Assert-ExitCode 'La lectura de la copia (pg_restore --list)'

    $hash = (Get-FileHash -Algorithm SHA256 $file).Hash
    Set-Content -Path "$file.sha256" -Value $hash -Encoding ASCII

    $sizeMb = [math]::Round((Get-Item $file).Length / 1MB, 2)
    Write-Log $cfg "Copia correcta ($sizeMb MB)."

    # ---- Segunda copia fuera de este disco ----
    if ($cfg.CopyTo) {
        if (-not (Test-Path $cfg.CopyTo)) {
            New-Item -ItemType Directory -Path $cfg.CopyTo | Out-Null
        }
        Copy-Item $file, "$file.sha256" -Destination $cfg.CopyTo -Force
        Write-Log $cfg "Segunda copia guardada en $($cfg.CopyTo)."
    }

    # ---- Limpieza de copias viejas (carpeta principal y segunda copia) ----
    Remove-OldBackups $cfg $cfg.BackupDir
    if ($cfg.CopyTo) {
        Remove-OldBackups $cfg $cfg.CopyTo
    }

    exit 0
}
catch {
    Write-Log $cfg "ERROR: $($_.Exception.Message)"
    Write-FailureAlert $cfg "La copia de seguridad FALLÓ: $($_.Exception.Message)"
    exit 1
}
