<#
.SYNOPSIS
    Programa las copias automáticas en el Programador de tareas de Windows.

.DESCRIPTION
    Crea dos tareas para el usuario actual:
      - "MantasGuajiras - Copia diaria":   todos los días a la hora -BackupTime.
      - "MantasGuajiras - Prueba semanal": cada domingo, restaura la última
        copia en una base temporal y verifica que esté sana.
    Si el computador está apagado a esa hora, la tarea se ejecuta en
    cuanto se encienda (StartWhenAvailable).

    Ejecuta una sola vez (PowerShell normal, no hace falta administrador):
      powershell -ExecutionPolicy Bypass -File scripts\backup\register-scheduled-tasks.ps1
      powershell -ExecutionPolicy Bypass -File scripts\backup\register-scheduled-tasks.ps1 -BackupTime 20:30

    Para quitarlas:
      Unregister-ScheduledTask -TaskName 'MantasGuajiras - *' -Confirm:$false
#>
param(
    [string] $BackupTime = '21:00',
    [string] $TestTime = '21:30'
)

$ErrorActionPreference = 'Stop'

if (-not (Test-Path (Join-Path $PSScriptRoot 'backup.config.ps1'))) {
    throw 'Primero crea backup.config.ps1 (copia de backup.config.example.ps1).'
}

$settings = New-ScheduledTaskSettingsSet -StartWhenAvailable -AllowStartIfOnBatteries `
    -DontStopIfGoingOnBatteries -ExecutionTimeLimit (New-TimeSpan -Hours 2)

function New-ScriptAction($script) {
    $path = Join-Path $PSScriptRoot $script
    return New-ScheduledTaskAction -Execute 'powershell.exe' `
        -Argument "-NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File `"$path`"" `
        -WorkingDirectory $PSScriptRoot
}

Register-ScheduledTask -TaskName 'MantasGuajiras - Copia diaria' `
    -Description 'Copia de seguridad diaria de la base de datos de MantasGuajiras.' `
    -Action (New-ScriptAction 'backup-db.ps1') `
    -Trigger (New-ScheduledTaskTrigger -Daily -At $BackupTime) `
    -Settings $settings -Force | Out-Null

Register-ScheduledTask -TaskName 'MantasGuajiras - Prueba semanal' `
    -Description 'Restaura la última copia en una base temporal y verifica que esté sana.' `
    -Action (New-ScriptAction 'restore-test.ps1') `
    -Trigger (New-ScheduledTaskTrigger -Weekly -DaysOfWeek Sunday -At $TestTime) `
    -Settings $settings -Force | Out-Null

Write-Host "Tareas creadas:"
Write-Host "  - Copia diaria a las $BackupTime"
Write-Host "  - Prueba de restauración los domingos a las $TestTime"
Write-Host "Revísalas en: Inicio > Programador de tareas > Biblioteca del Programador de tareas."
Write-Host "El historial queda en el archivo backup.log de la carpeta de copias."
