<#
.SYNOPSIS
    RECUPERACIÓN: restaura una copia de seguridad en una base NUEVA.

.DESCRIPTION
    Úsalo cuando la base de trabajo se dañó o se perdió.
    Por seguridad NUNCA sobrescribe una base existente: restaura en
    -TargetDatabase (por defecto mantas_guajiras_db_restaurada) y luego
    tú decides apuntar el backend a esa base (ver docs/09-copias-de-seguridad.md).

    Uso:
      powershell -ExecutionPolicy Bypass -File scripts\backup\restore-db.ps1 -BackupFile C:\...\mantas_2026-11-23_210000.dump
      powershell -ExecutionPolicy Bypass -File scripts\backup\restore-db.ps1 -BackupFile ... -TargetDatabase mantas_guajiras_db
#>
param(
    [Parameter(Mandatory = $true)] [string] $BackupFile,
    [string] $TargetDatabase = 'mantas_guajiras_db_restaurada',
    # Solo para copias antiguas sin .sha256, si sabes que son buenas.
    [switch] $AllowMissingHash
)

. (Join-Path $PSScriptRoot 'common.ps1')

$cfg = Get-BackupConfig
$connArgs = Get-ConnArgs $cfg

if (-not (Test-Path $BackupFile)) { throw "No existe el archivo $BackupFile." }

Assert-BackupHash $cfg $BackupFile $AllowMissingHash.IsPresent

$exists = & (Get-PgTool $cfg 'psql') @connArgs --dbname postgres --tuples-only --no-align `
    --command "SELECT 1 FROM pg_database WHERE datname = '$TargetDatabase'"
Assert-ExitCode 'La consulta de bases existentes'

if ($exists) {
    throw "La base '$TargetDatabase' ya existe. Elige otro nombre con -TargetDatabase o bórrala tú mismo después de revisarla. Este script nunca sobrescribe datos."
}

Write-Host ''
Write-Host "Se va a restaurar:  $BackupFile"
Write-Host "En la base NUEVA:   $TargetDatabase"
$answer = Read-Host "Escribe el nombre de la base para confirmar"
if ($answer -ne $TargetDatabase) { Write-Host 'Cancelado.'; exit 1 }

& (Get-PgTool $cfg 'createdb') @connArgs $TargetDatabase
Assert-ExitCode 'createdb'

& (Get-PgTool $cfg 'pg_restore') @connArgs --dbname $TargetDatabase --no-owner --exit-on-error $BackupFile
Assert-ExitCode 'pg_restore'

Write-Log $cfg "RECUPERACIÓN: $BackupFile restaurada en '$TargetDatabase'."
Write-Host ''
Write-Host "Listo. Para usarla, cambia el nombre de la base en backend/src/main/resources/application-dev.yml"
Write-Host "(spring.datasource.url ...:5432/$TargetDatabase) y reinicia el backend."
