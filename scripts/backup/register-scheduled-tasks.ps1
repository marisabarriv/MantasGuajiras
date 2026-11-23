<#
.SYNOPSIS
    Programa las copias automáticas en el Programador de tareas de Windows.

.DESCRIPTION
    Crea dos tareas para el usuario actual:
      - "MantasGuajiras - Copias del día": una copia cada -EveryHours horas
        entre -StartTime y -EndTime, y además una copia cada vez que inicias
        sesión en Windows (por ejemplo, cuando vuelve la luz y prendes el PC).
      - "MantasGuajiras - Prueba semanal": cada domingo restaura la última
        copia en una base temporal y verifica que esté sana.
    Si el computador está apagado a una hora programada, la copia se hace
    en cuanto se encienda (StartWhenAvailable).

    Ejecuta una sola vez (PowerShell normal, no hace falta administrador):
      powershell -ExecutionPolicy Bypass -File scripts\backup\register-scheduled-tasks.ps1
    Ejemplo: cada hora de 7 a. m. a 8 p. m.:
      powershell -ExecutionPolicy Bypass -File scripts\backup\register-scheduled-tasks.ps1 -EveryHours 1 -StartTime 07:00 -EndTime 20:00

    Volver a ejecutarlo reemplaza la programación anterior.
    Para quitarlas:
      Unregister-ScheduledTask -TaskName 'MantasGuajiras - *' -Confirm:$false
#>
param(
    [ValidateRange(1, 12)] [int] $EveryHours = 2,
    [string] $StartTime = '06:00',
    [string] $EndTime = '21:00',
    [string] $TestTime = '21:30'
)

$ErrorActionPreference = 'Stop'

if (-not (Test-Path (Join-Path $PSScriptRoot 'backup.config.ps1'))) {
    throw 'Primero crea backup.config.ps1 (copia de backup.config.example.ps1).'
}

$start = [datetime]::ParseExact($StartTime, 'HH:mm', $null)
$end = [datetime]::ParseExact($EndTime, 'HH:mm', $null)
if ($end -le $start) { throw '-EndTime debe ser posterior a -StartTime.' }

$settings = New-ScheduledTaskSettingsSet -StartWhenAvailable -AllowStartIfOnBatteries `
    -DontStopIfGoingOnBatteries -ExecutionTimeLimit (New-TimeSpan -Hours 1) `
    -MultipleInstances IgnoreNew

function New-ScriptAction($script) {
    $path = Join-Path $PSScriptRoot $script
    return New-ScheduledTaskAction -Execute 'powershell.exe' `
        -Argument "-NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File `"$path`"" `
        -WorkingDirectory $PSScriptRoot
}

# Copia diaria que se repite cada N horas dentro del horario indicado.
$repeating = New-ScheduledTaskTrigger -Daily -At $StartTime
$repeating.Repetition = (New-ScheduledTaskTrigger -Once -At $StartTime `
        -RepetitionInterval (New-TimeSpan -Hours $EveryHours) `
        -RepetitionDuration ($end - $start)).Repetition

# Copia final exactamente a la hora de cierre (la repetición puede no
# caer justo en -EndTime, por ejemplo cada 2 h desde las 6:00 termina a las 20:00).
$closing = New-ScheduledTaskTrigger -Daily -At $EndTime

# Copia al iniciar sesión (2 minutos después, para que PostgreSQL ya esté arriba).
$atLogon = New-ScheduledTaskTrigger -AtLogOn -User "$env:USERDOMAIN\$env:USERNAME"
$atLogon.Delay = 'PT2M'

# La tarea antigua de una sola copia diaria (versiones anteriores del script).
Unregister-ScheduledTask -TaskName 'MantasGuajiras - Copia diaria' -Confirm:$false -ErrorAction SilentlyContinue

Register-ScheduledTask -TaskName 'MantasGuajiras - Copias del día' `
    -Description "Copia de seguridad de MantasGuajiras cada $EveryHours h ($StartTime a $EndTime) y al iniciar sesión." `
    -Action (New-ScriptAction 'backup-db.ps1') `
    -Trigger @($repeating, $closing, $atLogon) `
    -Settings $settings -Force | Out-Null

Register-ScheduledTask -TaskName 'MantasGuajiras - Prueba semanal' `
    -Description 'Restaura la última copia en una base temporal y verifica que esté sana.' `
    -Action (New-ScriptAction 'restore-test.ps1') `
    -Trigger (New-ScheduledTaskTrigger -Weekly -DaysOfWeek Sunday -At $TestTime) `
    -Settings $settings -Force | Out-Null

Write-Host "Tareas creadas:"
Write-Host "  - Copia cada $EveryHours hora(s) desde las $StartTime, una final a las $EndTime y otra al iniciar sesión en Windows"
Write-Host "  - Prueba de restauración los domingos a las $TestTime"
Write-Host "Revísalas en: Inicio > Programador de tareas > Biblioteca del Programador de tareas."
Write-Host "El historial queda en el archivo backup.log de la carpeta de copias."
