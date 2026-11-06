$ErrorActionPreference = "Continue"
$rootDir   = Split-Path -Parent $PSScriptRoot
$logFile   = Join-Path $rootDir "BITACORA_EN_VIVO_EXE.log"
$notesFile = Join-Path $rootDir "NOTAS_VERIFICACION_EN_VIVO.md"

$startLineCount = 0
if (Test-Path $logFile) {
    $startLineCount = @(Get-Content -Path $logFile -ErrorAction SilentlyContinue).Count
}

Write-Output "[MONITOR] Iniciando vigilancia en tiempo real sobre PROGRAMA_PERFECTO_XVIEW_B866V2.exe (Lineas previas en bitacora: $startLineCount)..."

# Esperar hasta 15s a que el proceso este activo
$waited = 0
while (-not (Get-Process -Name "PROGRAMA_PERFECTO_XVIEW_B866V2" -ErrorAction SilentlyContinue) -and $waited -lt 30) {
    Start-Sleep -Milliseconds 500
    $waited++
}

$proc = Get-Process -Name "PROGRAMA_PERFECTO_XVIEW_B866V2" -ErrorAction SilentlyContinue
if ($proc) {
    Write-Output "[MONITOR] Proceso activo detectado: PID=$($proc.Id)"
}

$lastCount = $startLineCount
$lastUsbId = ""

while ($true) {
    $running = Get-Process -Name "PROGRAMA_PERFECTO_XVIEW_B866V2" -ErrorAction SilentlyContinue

    # 1. Leer nuevas lineas de BITACORA_EN_VIVO_EXE.log
    if (Test-Path $logFile) {
        $lines = @(Get-Content -Path $logFile -ErrorAction SilentlyContinue)
        if ($lines.Count -gt $lastCount) {
            $newLines = $lines[$lastCount..($lines.Count - 1)]
            $lastCount = $lines.Count
            foreach ($nl in $newLines) {
                Write-Output $nl
                Add-Content -Path $notesFile -Value "- $nl" -Encoding UTF8 -ErrorAction SilentlyContinue

                # Si ocurre una anomalia o evento que requiera intervencion inmediata en vivo, despertar al agente:
                if ($nl -match "\[CMD_EX\]|\[FASTBOOT_LOCKED_ASSIST\]|No se detecto la senal WorldCup|\[AVISO\]") {
                    Write-Output "=== WAKEUP_REASON: EVENT_REQUIRING_ATTENTION ==="
                    exit 0
                }
            }
        }
    }

    # 2. Si el usuario cerro el programa .exe, finalizar monitoreo y despertar al agente
    if (-not $running) {
        Write-Output "=== WAKEUP_REASON: USER_CLOSED_PROGRAM ==="
        exit 0
    }

    Start-Sleep -Milliseconds 300
}
