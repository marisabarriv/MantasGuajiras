<#
.SYNOPSIS
    Prueba que una copia de seguridad SÍ se puede restaurar.

.DESCRIPTION
    Una copia que nunca se ha restaurado no es una copia confiable.
    Este script:
      1. Toma la copia más reciente (o la indicada con -BackupFile).
      2. Verifica su huella SHA-256.
      3. La restaura en una base TEMPORAL (RestoreTestDb). Tu base de
         trabajo NO se toca.
      4. Revisa la integridad (scripts\backup\integrity-check.sql).
      5. Compara la cantidad de registros con la base de trabajo.
      6. Borra la base temporal.

    Uso:  powershell -ExecutionPolicy Bypass -File scripts\backup\restore-test.ps1
          powershell -ExecutionPolicy Bypass -File scripts\backup\restore-test.ps1 -BackupFile C:\ruta\mantas_....dump
#>
param(
    [string] $BackupFile,
    # Solo para copias antiguas sin .sha256: permite probarlas igual.
    [switch] $AllowMissingHash
)

. (Join-Path $PSScriptRoot 'common.ps1')

$cfg = Get-BackupConfig
$connArgs = Get-ConnArgs $cfg
$psql = Get-PgTool $cfg 'psql'
$testDb = $cfg.RestoreTestDb

if ($testDb -eq $cfg.Database) {
    throw 'RestoreTestDb no puede ser la misma base de trabajo.'
}

function Get-TableCounts($database) {
    $sql = @"
SELECT 'app_user', COUNT(*) FROM app_user UNION ALL
SELECT 'product', COUNT(*) FROM product UNION ALL
SELECT 'inventory', COUNT(*) FROM inventory UNION ALL
SELECT 'inventory_movement', COUNT(*) FROM inventory_movement UNION ALL
SELECT 'sale', COUNT(*) FROM sale UNION ALL
SELECT 'sale_item', COUNT(*) FROM sale_item UNION ALL
SELECT 'purchase', COUNT(*) FROM purchase UNION ALL
SELECT 'production', COUNT(*) FROM production UNION ALL
SELECT 'orders', COUNT(*) FROM orders UNION ALL
SELECT 'order_payment', COUNT(*) FROM order_payment UNION ALL
SELECT 'flyway_version', COALESCE(MAX(installed_rank), 0) FROM flyway_schema_history WHERE success
"@
    $rows = & $psql @connArgs --dbname $database --no-align --tuples-only --field-separator '|' --command $sql
    Assert-ExitCode "La consulta de conteos en $database"
    $result = [ordered]@{}
    foreach ($row in $rows) {
        if ($row) { $parts = $row.Split('|'); $result[$parts[0]] = [long]$parts[1] }
    }
    return $result
}

$ok = $true

try {
    if (-not $BackupFile) {
        $latest = Get-ChildItem $cfg.BackupDir -Filter 'mantas_*.dump' |
            Sort-Object LastWriteTime -Descending | Select-Object -First 1
        if (-not $latest) { throw "No hay copias en $($cfg.BackupDir). Ejecuta primero backup-db.ps1." }
        $BackupFile = $latest.FullName
    }

    Write-Log $cfg "PRUEBA DE RESTAURACIÓN de $BackupFile"

    # 1. Huella (obligatoria)
    Assert-BackupHash $cfg $BackupFile $AllowMissingHash.IsPresent

    # 2. Restaurar en una base temporal
    & (Get-PgTool $cfg 'dropdb') @connArgs --if-exists $testDb
    Assert-ExitCode 'dropdb (limpieza previa)'
    & (Get-PgTool $cfg 'createdb') @connArgs $testDb
    Assert-ExitCode 'createdb'

    & (Get-PgTool $cfg 'pg_restore') @connArgs --dbname $testDb --no-owner --exit-on-error $BackupFile
    Assert-ExitCode 'pg_restore'
    Write-Log $cfg "Copia restaurada en la base temporal '$testDb'."

    # 3. Integridad
    $checks = & $psql @connArgs --dbname $testDb --no-align --tuples-only --field-separator '|' `
        --file (Join-Path $PSScriptRoot 'integrity-check.sql')
    Assert-ExitCode 'La verificación de integridad'

    foreach ($row in $checks) {
        if (-not $row) { continue }
        $parts = $row.Split('|')
        if ([long]$parts[1] -eq 0) {
            Write-Log $cfg ("  OK     {0}" -f $parts[0])
        }
        else {
            Write-Log $cfg ("  FALLA  {0}: {1} problema(s)" -f $parts[0], $parts[1])
            $ok = $false
        }
    }

    # 4. Comparar con la base de trabajo
    $restored = Get-TableCounts $testDb
    $live = Get-TableCounts $cfg.Database

    Write-Log $cfg ('  {0,-20} {1,10} {2,10}' -f 'Tabla', 'Copia', 'Actual')
    foreach ($table in $restored.Keys) {
        $note = ''
        if ($restored[$table] -gt $live[$table]) {
            $note = '  <- la copia tiene MÁS registros que la base actual: revisar'
            $ok = $false
        }
        elseif ($restored[$table] -lt $live[$table]) {
            $note = '  (operaciones posteriores a la copia)'
        }
        Write-Log $cfg ('  {0,-20} {1,10} {2,10}{3}' -f $table, $restored[$table], $live[$table], $note)
    }
}
catch {
    Write-Log $cfg "ERROR: $($_.Exception.Message)"
    $ok = $false
}
finally {
    & (Get-PgTool $cfg 'dropdb') @connArgs --if-exists $testDb
}

if ($ok) {
    Write-Log $cfg 'RESULTADO: la copia se restauró y pasó todas las verificaciones.'
    exit 0
}

Write-Log $cfg 'RESULTADO: LA PRUEBA DE RESTAURACIÓN FALLÓ. Revisa los mensajes anteriores.'
Write-FailureAlert $cfg 'La prueba semanal de restauración FALLÓ: la última copia podría no servir.'
exit 1
