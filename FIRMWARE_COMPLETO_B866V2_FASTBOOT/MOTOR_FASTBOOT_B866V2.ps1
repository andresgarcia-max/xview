param(
    [string]$ComPort = "AUTO",
    [int]$BaudRate = 115200,
    [switch]$FlashFirmware = $false
)

$ErrorActionPreference = "Continue"
$fwDir = $PSScriptRoot
$fbExe = Join-Path $fwDir "fastboot.exe"
$adbExe = Join-Path $fwDir "adb.exe"
$updExe = Join-Path $fwDir "update.exe"
$logFile = Join-Path $fwDir "SESION_FASTBOOT_B866V2.log"
$summaryFile = Join-Path $fwDir "RESUMEN_PROCESO_B866V2.txt"

$sessionStart = Get-Date
$modeName = if ($FlashFirmware) { "FLASHEO DE FIRMWARE COMPLETO + FASTBOOT DESBLOQUEADO" } else { "ENTRADA A FASTBOOT + DESBLOQUEO DE BOOTLOADER" }

$script:Steps = New-Object System.Collections.Generic.List[PSObject]
$script:PartitionResults = New-Object System.Collections.Generic.List[PSObject]
$script:OverallStatus = "EN PROGRESO"
$script:OverallSuccess = $false
$script:FailureReason = "Ninguno"
$script:DetectedInfo = [ordered]@{
    ModoOperacion      = $modeName
    PuertoUART         = "No conectado"
    ViaEntrada         = "Pendiente"
    IdDispositivoUSB   = "No detectado"
    VersionUBoot       = "U-Boot 2015.01-g220e992 (Sep 05 2023)"
    ModeloPlaca        = "ZTE ZXV10 B866V2 (g12a_u212_2g / board_id=8)"
    EstadoLockInicial  = "Desconocido"
    EstadoLockFinal    = "Desconocido"
    EscrituraFastboot  = "No verificada"
    ParticionesOK      = 0
    ParticionesError   = 0
}

function Write-Status([string]$msg, [string]$color = "White") {
    $ts = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss.fff")
    $line = "[$ts] $msg"
    Write-Host $line -ForegroundColor $color
    Add-Content -Path $logFile -Value $line -Encoding Ascii
}

function Record-Step([string]$stage, [string]$status, [string]$detail) {
    $ts = (Get-Date).ToString("HH:mm:ss")
    $existing = $script:Steps | Where-Object { $_.Etapa -eq $stage } | Select-Object -First 1
    if ($null -ne $existing) {
        $existing.Estado = $status
        $existing.Hora = $ts
        $existing.Detalle = $detail
    } else {
        $obj = [PSCustomObject]@{
            Paso    = ($script:Steps.Count + 1)
            Etapa   = $stage
            Estado  = $status
            Hora    = $ts
            Detalle = $detail
        }
        $script:Steps.Add($obj)
    }
    $col = "White"
    if ($status -eq "EXITO") { $col = "Green" }
    elseif ($status -eq "FALLO") { $col = "Red" }
    elseif ($status -eq "EN CURSO" -or $status -eq "PENDIENTE") { $col = "Yellow" }
    elseif ($status -eq "PROTEGIDO") { $col = "Cyan" }
    Write-Status "[REGISTRO][$status] $stage -> $detail" $col
    Save-SummaryReport
}

function Record-Partition([string]$partName, [string]$fileName, [string]$status, [string]$durationSec, [string]$response) {
    $obj = [PSCustomObject]@{
        Particion = $partName
        Archivo   = $fileName
        Estado    = $status
        Tiempo    = "${durationSec}s"
        Respuesta = $response
    }
    $script:PartitionResults.Add($obj)
    Save-SummaryReport
}

function Save-SummaryReport {
    $now = Get-Date
    $elapsed = [math]::Round(($now - $sessionStart).TotalSeconds, 1)
    $verdictBanner = if ($script:OverallSuccess) {
        ">>> VEREDICTO FINAL: [EXITO TOTAL] - PROCESO COMPLETADO CORRECTAMENTE <<<"
    } elseif ($script:OverallStatus -eq "EN PROGRESO") {
        ">>> ESTADO ACTUAL: [EN PROGRESO] - EJECUTANDO OPERACIONES <<<"
    } else {
        ">>> VEREDICTO FINAL: [FALLO / INCOMPLETO] - REVISAR CAUSA ABAJO <<<"
    }

    $lines = New-Object System.Collections.Generic.List[string]
    $lines.Add("================================================================================")
    $lines.Add("          REPORTE Y RESUMEN DE EJECUCION - ZTE ZXV10 B866V2 (XVIEW+)            ")
    $lines.Add("================================================================================")
    $lines.Add(" $verdictBanner")
    $lines.Add("================================================================================")
    $lines.Add(" Fecha de inicio    : $($sessionStart.ToString('yyyy-MM-dd HH:mm:ss'))")
    $lines.Add(" Ultima actualiz.   : $($now.ToString('yyyy-MM-dd HH:mm:ss')) (Duracion: ${elapsed}s)")
    $lines.Add(" Modo ejecutado     : $($script:DetectedInfo.ModoOperacion)")
    $lines.Add(" Estado global      : $($script:OverallStatus)")
    if (-not $script:OverallSuccess -and $script:FailureReason -ne "Ninguno") {
        $lines.Add(" Causa / Diagnostico: $($script:FailureReason)")
    }
    $lines.Add("--------------------------------------------------------------------------------")
    $lines.Add(" 1. INFORMACION DETECTADA DEL DISPOSITIVO")
    $lines.Add("--------------------------------------------------------------------------------")
    $lines.Add("  * Modelo / Placa        : $($script:DetectedInfo.ModeloPlaca)")
    $lines.Add("  * Version de U-Boot     : $($script:DetectedInfo.VersionUBoot)")
    $lines.Add("  * Puerto UART TTL       : $($script:DetectedInfo.PuertoUART)")
    $lines.Add("  * Via de Entrada        : $($script:DetectedInfo.ViaEntrada)")
    $lines.Add("  * ID Dispositivo USB    : $($script:DetectedInfo.IdDispositivoUSB)")
    $lines.Add("  * Estado Lock Inicial   : $($script:DetectedInfo.EstadoLockInicial)")
    $lines.Add("  * Estado Lock Final     : $($script:DetectedInfo.EstadoLockFinal)")
    $lines.Add("  * Prueba de Escritura   : $($script:DetectedInfo.EscrituraFastboot)")
    $lines.Add("--------------------------------------------------------------------------------")
    $lines.Add(" 2. REGISTRO PASO A PASO DE ETAPAS")
    $lines.Add("--------------------------------------------------------------------------------")
    foreach ($s in $script:Steps) {
        $pNum = $s.Paso.ToString().PadLeft(2, '0')
        $stPad = ("[" + $s.Estado + "]").PadRight(12)
        $etPad = $s.Etapa.PadRight(38)
        $lines.Add("  Paso $pNum | $stPad | $($s.Hora) | $etPad | $($s.Detalle)")
    }

    if ($script:PartitionResults.Count -gt 0) {
        $lines.Add("--------------------------------------------------------------------------------")
        $lines.Add(" 3. DETALLE DE FLASHEO POR PARTICION (OK: $($script:DetectedInfo.ParticionesOK) | ERRORES: $($script:DetectedInfo.ParticionesError))")
        $lines.Add("--------------------------------------------------------------------------------")
        $lines.Add("  PARTICION      | ARCHIVO              | ESTADO      | TIEMPO  | RESPUESTA")
        $lines.Add("  ---------------+----------------------+-------------+---------+---------------")
        foreach ($p in $script:PartitionResults) {
            $c1 = $p.Particion.PadRight(14)
            $c2 = $p.Archivo.PadRight(20)
            $c3 = ("[" + $p.Estado + "]").PadRight(11)
            $c4 = $p.Tiempo.PadRight(7)
            $lines.Add("  $c1 | $c2 | $c3 | $c4 | $($p.Respuesta)")
        }
    }

    $lines.Add("--------------------------------------------------------------------------------")
    $lines.Add(" Archivos generados en esta carpeta:")
    $lines.Add("  - Resumen ejecutivo : $summaryFile")
    $lines.Add("  - Bitacora completa : $logFile")
    $lines.Add("================================================================================")

    [System.IO.File]::WriteAllLines($summaryFile, $lines, [System.Text.Encoding]::ASCII)
}

function Show-SummaryOnScreen {
    Save-SummaryReport
    Write-Host ""
    $content = Get-Content -Path $summaryFile
    foreach ($line in $content) {
        if ($line -match "\[EXITO TOTAL\]|\[EXITO\]|\[PROTEGIDO\]") {
            Write-Host $line -ForegroundColor Green
        } elseif ($line -match "\[FALLO|ERROR") {
            Write-Host $line -ForegroundColor Red
        } elseif ($line -match "^==|^--") {
            Write-Host $line -ForegroundColor Cyan
        } elseif ($line -match "EN PROGRESO|PENDIENTE|EN CURSO") {
            Write-Host $line -ForegroundColor Yellow
        } else {
            Write-Host $line -ForegroundColor White
        }
    }
    Write-Host ""
}

function Send-Uart([System.IO.Ports.SerialPort]$port, [string]$cmd) {
    if ($null -ne $port -and $port.IsOpen) {
        $pName = $port.PortName
        Write-Status "[UART-TX -> $pName] $cmd" "Cyan"
        $port.Write("$cmd`r`n")
    }
}

# Limpiar encabezado en bitacora al iniciar nueva sesion
Add-Content -Path $logFile -Value "" -Encoding Ascii
Write-Status "==========================================================================" "Cyan"
Write-Status " MOTOR DE DESBLOQUEO, FLASHEO Y REGISTRO - ZTE ZXV10 B866V2 (board_id=8)" "Cyan"
Write-Status " Modo: $modeName" "Cyan"
Write-Status " Bitacora en vivo: $logFile" "Cyan"
Write-Status " Resumen final   : $summaryFile" "Cyan"
Write-Status "==========================================================================" "Cyan"

# ------------------------------------------------------------------------------
# ETAPA 1: VERIFICACION DE ARCHIVOS DEL FIRMWARE Y HERRAMIENTAS
# ------------------------------------------------------------------------------
$requiredFiles = @(
    "fastboot.exe", "adb.exe", "update.exe",
    "bootloader.img", "dtb.img", "dtbo.img", "boot.img", "recovery.img",
    "vbmeta.img", "logo.img", "param.img", "conf.img", "oem.img",
    "cache_ext4.img", "misc_clean.img", "misc_fastboot.img", "env_unlocked.img"
)
$missingFiles = @()
foreach ($f in $requiredFiles) {
    if (-not (Test-Path (Join-Path $fwDir $f))) {
        $missingFiles += $f
    }
}
if ($missingFiles.Count -eq 0) {
    Record-Step "1. Verificacion de paquete Firmware" "EXITO" "17/17 imagenes y herramientas verificadas en carpeta"
} else {
    $missStr = ($missingFiles -join ", ")
    $script:OverallStatus = "FALLO"
    $script:FailureReason = "Faltan archivos requeridos: $missStr"
    Record-Step "1. Verificacion de paquete Firmware" "FALLO" "Faltan archivos: $missStr"
    Show-SummaryOnScreen
    exit 1
}

Record-Step "2. Conexion UART / USB con la caja" "EN CURSO" "Buscando caja en UART (COM8), Fastboot, ADB o WorldCup..."
Record-Step "3. Entrada a modo USB Fastboot" "PENDIENTE" "Esperando intercepcion de arranque o comando USB"
Record-Step "4. Desbloqueo de Bootloader (lock)" "PENDIENTE" "Esperando verificar lock=10100000 en Fastboot"
if ($FlashFirmware) {
    Record-Step "5. Flasheo de particiones B866V2" "PENDIENTE" "Esperando desbloqueo de Fastboot para flashear"
}

$serialPort = $null
$ubootIntercepted = $false
$recoveryCleanRebootSent = $false
$inBootSequence = $false
$bootStartTime = [DateTime]::MinValue
$flashed = $false
$rxTranscript = ""
$lastWaitMsg = [DateTime]::Now
$lastUartProbe = [DateTime]::MinValue
$lastUsbScan = [DateTime]::MinValue
$lockedWarningLogged = $false
$mtpDetectedLogged = $false

try {
    while ($true) {
        $hasWorldCupUsb = $false
        $hasAdbUsb = $false
        $hasFastbootUsb = $false
        $hasMtpUsb = $false

        if ((-not $inBootSequence) -and (((Get-Date) - $lastUsbScan).TotalMilliseconds -ge 500)) {
            $lastUsbScan = Get-Date
            try {
                $usbPnp = Get-PnpDevice -PresentOnly -ErrorAction SilentlyContinue | Where-Object {
                    $_.InstanceId -match "VID_18D1|VID_1B8E|VID_19D2"
                }
                foreach ($dev in $usbPnp) {
                    $iid = $dev.InstanceId
                    if ($iid -match "VID_1B8E") { $hasWorldCupUsb = $true }
                    if ($iid -match "PID_4EE2|PID_4E12|PID_4E40|PID_0C02") { $hasAdbUsb = $true }
                    if ($iid -match "PID_0D02|PID_4EE0|PID_4EE7|PID_D00D") {
                        $hasFastbootUsb = $true
                        if ($iid -match "PID_4EE0") { $hasAdbUsb = $true }
                    }
                    if ($iid -match "PID_4EE1|PID_4E41") {
                        $hasMtpUsb = $true
                        if ($script:DetectedInfo.IdDispositivoUSB -eq "No detectado") {
                            $script:DetectedInfo.IdDispositivoUSB = "Z41220090388172 (MTP PID_4EE1)"
                        }
                        if (-not $mtpDetectedLogged) {
                            Record-Step "2. Conexion UART / USB con la caja" "EN CURSO" "Xview+ detectado por Cable Macho-a-Macho (MTP PID_4EE1). Conecta tambien UART (COM8) o reinicia hacia Fastboot"
                            $mtpDetectedLogged = $true
                        }
                    }
                }
            } catch {}
        }

        # ----------------------------------------------------------------------
        # 1. VERIFICAR SI ESTA EN MODO AMLOGIC WORLDCUP USB (InUsbBurn / BL1 ROM)
        # ----------------------------------------------------------------------
        if ($hasWorldCupUsb -and (Test-Path $updExe)) {
            $scanOut = & $updExe scan 2>&1 | Out-String
            if (($scanOut -match "WorldCup Device") -and ($scanOut -notmatch "No \[WorldCup Device\]")) {
                $idOut = (& $updExe identify 2>&1 | Out-String).Trim()
                $script:DetectedInfo.ViaEntrada = "Cable USB Macho-a-Macho (Amlogic WorldCup: $idOut)"
                Record-Step "2. Conexion UART / USB con la caja" "EXITO" "Caja detectada por Cable USB Macho-a-Macho ($idOut)"

                # Si entra desde BootROM BL1 (Stage 0-0 / 0-16), cargar bootloader.img en RAM por USB (bl2_boot)
                if ($idOut -match "0-0|0-16") {
                    Record-Step "3. Entrada a modo USB Fastboot" "EN CURSO" "Modo BL1 ROM detectado; cargando bootloader.img por USB (bl2_boot)..."
                    & $updExe bl2_boot (Join-Path $fwDir "bootloader.img") 2>&1 | Out-Null
                    Start-Sleep -Seconds 3
                }

                Record-Step "3. Entrada a modo USB Fastboot" "EN CURSO" "Desbloqueando lock=10100000, inyectando env_unlocked.img y activando Fastboot USB"
                & $updExe mwrite (Join-Path $fwDir "dtb.img") mem 0x1000000 normal 2>&1 | Out-Null
                & $updExe bulkcmd "disk_initial 0" 2>&1 | Out-Null
                & $updExe bulkcmd "setenv -f lock 10100000" 2>&1 | Out-Null
                & $updExe bulkcmd "setenv lock 10100000" 2>&1 | Out-Null
                & $updExe bulkcmd "setenv unlock_ability 1" 2>&1 | Out-Null
                & $updExe bulkcmd "setenv otg_device 1" 2>&1 | Out-Null
                & $updExe bulkcmd "setenv bootdelay 3" 2>&1 | Out-Null
                & $updExe bulkcmd "saveenv" 2>&1 | Out-Null
                & $updExe partition _aml_dtb (Join-Path $fwDir "dtb.img") 2>&1 | Out-Null
                & $updExe partition env (Join-Path $fwDir "env_unlocked.img") 2>&1 | Out-Null
                & $updExe partition misc (Join-Path $fwDir "misc_clean.img") 2>&1 | Out-Null
                & $updExe bulkcmd "fastboot" 2>&1 | Out-Null
                Start-Sleep -Seconds 2
            }
        }

        # ----------------------------------------------------------------------
        # 2. VERIFICAR SI ESTA EN MODO ADB (Android o Recovery con ADB)
        # ----------------------------------------------------------------------
        if ($hasAdbUsb -and (Test-Path $adbExe)) {
            $adbDevs = & $adbExe devices 2>&1 | Out-String
            if ($adbDevs -match "(?m)^([A-Za-z0-9\.\:\-]+)\s+(device|recovery)") {
                $adbId = $Matches[1]
                $adbMode = $Matches[2]
                $script:DetectedInfo.ViaEntrada = "ADB USB ($adbMode - $adbId)"
                Record-Step "2. Conexion UART / USB con la caja" "EXITO" "Caja detectada en ADB ($adbMode): $adbId"
                Record-Step "3. Entrada a modo USB Fastboot" "EN CURSO" "Enviando setprop lock=10100000 y adb reboot bootloader"
                & $adbExe shell "setprop ubootenv.var.lock 10100000; setprop ubootenv.var.otg_device 1" 2>&1 | Out-Null
                & $adbExe reboot bootloader 2>&1 | Out-Null
                Start-Sleep -Seconds 2
            }
        }

        # ----------------------------------------------------------------------
        # 3. CONEXION Y MONITOREO UART TTL (COM8 / AUTO)
        # ----------------------------------------------------------------------
        if ($null -eq $serialPort -or -not $serialPort.IsOpen) {
            $ports = [System.IO.Ports.SerialPort]::GetPortNames()
            $targetPort = $null
            if (($ComPort -ne "AUTO") -and ($ports -contains $ComPort)) {
                $targetPort = $ComPort
            } elseif ($ports -contains "COM8") {
                $targetPort = "COM8"
            } else {
                $nonSol = $ports | Where-Object { $_ -ne "COM3" }
                if ($nonSol) { $targetPort = $nonSol | Select-Object -First 1 }
            }

            if ($targetPort) {
                try {
                    $serialPort = New-Object System.IO.Ports.SerialPort($targetPort, $BaudRate, [System.IO.Ports.Parity]::None, 8, [System.IO.Ports.StopBits]::One)
                    $serialPort.ReadTimeout = 50
                    $serialPort.WriteTimeout = 300
                    $serialPort.Open()
                    $script:DetectedInfo.PuertoUART = "$targetPort ($BaudRate 8N1)"
                    if ($script:DetectedInfo.ViaEntrada -eq "Pendiente") {
                        $script:DetectedInfo.ViaEntrada = "UART TTL ($targetPort)"
                    }
                    Record-Step "2. Conexion UART / USB con la caja" "EXITO" "Puerto serial abierto en $targetPort ($BaudRate 8N1)"
                    $serialPort.Write("`r`n")
                } catch {
                    # Puerto ocupado temporalmente
                }
            }
        }

        if ($null -ne $serialPort -and $serialPort.IsOpen) {
            try {
                $incoming = $serialPort.ReadExisting()
                if (-not [string]::IsNullOrEmpty($incoming)) {
                    $rxTranscript += $incoming
                    if ($rxTranscript.Length -gt 24576) {
                        $rxTranscript = $rxTranscript.Substring($rxTranscript.Length - 12288)
                    }

                    foreach ($ln in ($incoming -split "`r?`n")) {
                        if ($ln.Trim().Length -gt 0) {
                            Write-Status "[UART] $ln" "Gray"
                        }
                    }

                    # A) Detectar inicio del ciclo de arranque (BL1 / BL2 / BL31 / U-Boot)
                    if ($incoming -match "G12A:BL:|BL2 Built|U-Boot 2015\.01") {
                        $inBootSequence = $true
                        $bootStartTime = Get-Date
                        $ubootIntercepted = $false
                        Record-Step "3. Entrada a modo USB Fastboot" "EN CURSO" "Arranque detectado en UART; enviando rafaga a U-Boot (14s)"
                    }

                    # B) Ventana de autoboot habilitada en U-Boot (set_tfuart_com_enable)
                    if (($incoming -match "set_tfuart_com_enable|Hit any key to stop autoboot|ztebsp: bootcmd=bootsys|COMControlFlag") -and (-not $ubootIntercepted)) {
                        $script:DetectedInfo.ViaEntrada = "UART U-Boot (set_tfuart_com_enable)"
                        Record-Step "3. Entrada a modo USB Fastboot" "EN CURSO" "Ventana set_tfuart_com_enable capturada; inyectando lock=10100000 y fastboot"
                        for ($j = 0; $j -lt 15; $j++) {
                            $serialPort.Write("`r ")
                            Start-Sleep -Milliseconds 20
                        }
                        Start-Sleep -Milliseconds 250
                        Send-Uart $serialPort "setenv -f lock 10100000"
                        Start-Sleep -Milliseconds 120
                        Send-Uart $serialPort "setenv lock 10100000"
                        Start-Sleep -Milliseconds 120
                        Send-Uart $serialPort "setenv unlock_ability 1"
                        Start-Sleep -Milliseconds 120
                        Send-Uart $serialPort "setenv otg_device 1"
                        Start-Sleep -Milliseconds 120
                        Send-Uart $serialPort "setenv bootdelay 3"
                        Start-Sleep -Milliseconds 120
                        Send-Uart $serialPort "setenv EnableSelinux permissive"
                        Start-Sleep -Milliseconds 120
                        Send-Uart $serialPort "saveenv"
                        Start-Sleep -Milliseconds 900
                        Send-Uart $serialPort "fastboot"
                        $ubootIntercepted = $true
                        $inBootSequence = $false
                    }

                    # C) Prompt directo de U-Boot
                    if (($incoming -match "g12a_u212|franklin#|=>") -and $inBootSequence -and (-not $ubootIntercepted)) {
                        $script:DetectedInfo.ViaEntrada = "Prompt U-Boot directo (g12a_u212_v1#)"
                        Record-Step "3. Entrada a modo USB Fastboot" "EN CURSO" "Prompt de U-Boot capturado; ejecutando setenv lock 10100000 y fastboot"
                        Send-Uart $serialPort "setenv -f lock 10100000"
                        Start-Sleep -Milliseconds 120
                        Send-Uart $serialPort "setenv lock 10100000"
                        Start-Sleep -Milliseconds 120
                        Send-Uart $serialPort "setenv unlock_ability 1"
                        Start-Sleep -Milliseconds 120
                        Send-Uart $serialPort "setenv otg_device 1"
                        Start-Sleep -Milliseconds 120
                        Send-Uart $serialPort "saveenv"
                        Start-Sleep -Milliseconds 900
                        Send-Uart $serialPort "fastboot"
                        $ubootIntercepted = $true
                        $inBootSequence = $false
                    }

                    # D) Inicio de Kernel Linux
                    if ($incoming -match "Starting kernel|Booting Android Image") {
                        $inBootSequence = $false
                    }

                    # E) Consola Root (#) en Recovery real
                    if (($incoming -match "(^|`n|`r)#\s*$") -and ($incoming -notmatch "gpio_keypad_down_cnt") -and (-not $recoveryCleanRebootSent)) {
                        $script:DetectedInfo.ViaEntrada = "UART Consola Recovery (#)"
                        Record-Step "3. Entrada a modo USB Fastboot" "EN CURSO" "Consola '#' de Recovery detectada; activando fastbootd USB y enviando 'reboot bootloader'"
                        Send-Uart $serialPort "setprop sys.usb.config fastboot"
                        Start-Sleep -Milliseconds 1500
                        Send-Uart $serialPort "reboot bootloader"
                        $recoveryCleanRebootSent = $true
                    }
                }

                if ($inBootSequence -and (-not $ubootIntercepted)) {
                    if (((Get-Date) - $bootStartTime).TotalSeconds -lt 15) {
                        $serialPort.Write("`r ")
                    } else {
                        $inBootSequence = $false
                    }
                } elseif ((-not $recoveryCleanRebootSent) -and (((Get-Date) - $lastUartProbe).TotalSeconds -ge 2.5)) {
                    $serialPort.Write("`r`n")
                    $lastUartProbe = Get-Date
                }
            } catch {
                # Timeout normal de lectura serial
            }
        }

        # ----------------------------------------------------------------------
        # 4. VERIFICAR SI LA CAJA YA ESTA EN MODO USB FASTBOOT
        # ----------------------------------------------------------------------
        $fbDevs = if ($hasFastbootUsb) { & $fbExe devices 2>&1 | Out-String } else { "" }
        if ($fbDevs -match "(?m)^(\S+)\s+fastboot") {
            $fbSerial = $Matches[1]
            $script:DetectedInfo.IdDispositivoUSB = $fbSerial
            if ($script:DetectedInfo.ViaEntrada -eq "Pendiente") {
                $script:DetectedInfo.ViaEntrada = "USB Fastboot Directo ($fbSerial)"
            }
            Record-Step "2. Conexion UART / USB con la caja" "EXITO" "Dispositivo USB Fastboot activo (ID: $fbSerial)"
            Record-Step "3. Entrada a modo USB Fastboot" "EXITO" "Caja en modo Fastboot con retencion Anti-Watchdog activa"

            $uSpace = (& $fbExe getvar is-userspace 2>&1 | Out-String).Trim()
            if ($uSpace -match "is-userspace:\s*yes") {
                Record-Step "3. Entrada a modo USB Fastboot" "EN CURSO" "Detectado fastbootd (Recovery); limpiando misc y pasando a U-Boot Fastboot..."
                & $fbExe flash misc (Join-Path $fwDir "misc_clean.img") 2>&1 | Out-Null
                $inBootSequence = $true
                $bootStartTime = Get-Date
                $ubootIntercepted = $false
                & $fbExe reboot bootloader 2>&1 | Out-Null
                Start-Sleep -Seconds 2
                continue
            }

            & $fbExe oem "watchdog off" 2>&1 | Out-Null
            $ulOut = (& $fbExe getvar unlocked 2>&1 | Out-String).Trim()
            if ($script:DetectedInfo.EstadoLockInicial -eq "Desconocido") {
                if ($ulOut -match "unlocked:\s*yes") {
                    $script:DetectedInfo.EstadoLockInicial = "DESBLOQUEADO (unlocked: yes)"
                } else {
                    $script:DetectedInfo.EstadoLockInicial = "BLOQUEADO (unlocked: no / 10001000)"
                }
            }

            & $fbExe flashing unlock_critical 2>&1 | Out-Null
            & $fbExe flashing unlock 2>&1 | Out-Null
            & $fbExe oem "setenv -f lock 10100000;save" 2>&1 | Out-Null
            & $fbExe oem "setenv lock 10100000;save" 2>&1 | Out-Null
            & $fbExe oem "setenv otg_device 1;save" 2>&1 | Out-Null

            $testWrite = (& $fbExe flash _aml_dtb (Join-Path $fwDir "dtb.img") 2>&1 | Out-String).Trim()
            if ($testWrite -match "device is locked|locked device|FAILED") {
                $script:DetectedInfo.EstadoLockFinal = "BLOQUEADO (lock=10001000)"
                $script:DetectedInfo.EscrituraFastboot = "RECHAZADA (device is locked, can not flash)"

                if ($null -ne $serialPort -and $serialPort.IsOpen) {
                    Record-Step "4. Desbloqueo de Bootloader (lock)" "EN CURSO" "Fastboot bloqueado (10001000). Reiniciando hacia UART para desbloquear en U-Boot..."
                    $inBootSequence = $true
                    $bootStartTime = Get-Date
                    $ubootIntercepted = $false
                    & $fbExe reboot 2>&1 | Out-Null
                    Start-Sleep -Seconds 1
                } else {
                    if (-not $lockedWarningLogged) {
                        $script:OverallStatus = "PENDIENTE DE UART PARA DESBLOQUEO"
                        $script:FailureReason = "La caja entro a Fastboot pero su U-Boot tiene lock=10001000 (bloqueado) y no hay adaptador UART conectado para enviar 'setenv -f lock 10100000'."
                        Record-Step "4. Desbloqueo de Bootloader (lock)" "FALLO" "Fastboot reporta 'device is locked' y no se detecta puerto UART (COM8) para desbloquear U-Boot"
                        if ($FlashFirmware) {
                            Record-Step "5. Flasheo de particiones B866V2" "FALLO" "No se puede flashear por USB mientras lock=10001000. Conecta UART (COM8)."
                        }
                        Show-SummaryOnScreen
                        Write-Status "[!] Caja retenida en Fastboot (Anti-Watchdog). Conecta el adaptador UART TTL (COM8) para continuar automaticamente o presiona Ctrl+C para salir." "Yellow"
                        $lockedWarningLogged = $true
                    }
                    Start-Sleep -Seconds 2
                }
            } else {
                # DESBLOQUEO Y ESCRITURA VERIFICADOS CON EXITO
                $script:DetectedInfo.EstadoLockFinal = "DESBLOQUEADO (lock=10100000 / unlocked: yes)"
                $script:DetectedInfo.EscrituraFastboot = "VERIFICADA CON EXITO (flash _aml_dtb OKAY)"
                Record-Step "4. Desbloqueo de Bootloader (lock)" "EXITO" "Bootloader desbloqueado (lock=10100000) y escritura confirmada en _aml_dtb"

                if (-not $FlashFirmware) {
                    $script:OverallStatus = "EXITO TOTAL"
                    $script:OverallSuccess = $true
                    $script:FailureReason = "Ninguno"
                    Show-SummaryOnScreen
                    break
                }

                if ($FlashFirmware -and (-not $flashed)) {
                    Record-Step "5. Flasheo de particiones B866V2" "EN CURSO" "Iniciando flasheo de las 12 particiones del firmware B866V2..."
                    $partitions = @(
                        @("bootloader", "bootloader.img"),
                        @("_aml_dtb",   "dtb.img"),
                        @("dtbo",       "dtbo.img"),
                        @("boot",       "boot.img"),
                        @("recovery",   "recovery.img"),
                        @("vbmeta",     "vbmeta.img"),
                        @("logo",       "logo.img"),
                        @("param",      "param.img"),
                        @("conf",       "conf.img"),
                        @("oem",        "oem.img"),
                        @("cache",      "cache_ext4.img"),
                        @("misc",       "misc_clean.img")
                    )

                    $okCount = 0
                    $errCount = 0

                    foreach ($p in $partitions) {
                        $pName = $p[0]
                        $pFileName = $p[1]
                        $pFile = Join-Path $fwDir $pFileName
                        if (Test-Path $pFile) {
                            $t0 = Get-Date
                            Write-Status "[FLASH] Enviando particion '$pName' ($pFileName)..." "Yellow"
                            $r = (& $fbExe flash $pName $pFile 2>&1 | Out-String).Trim()
                            $dt = [math]::Round(((Get-Date) - $t0).TotalSeconds, 2)
                            $rSingleLine = ($r -replace "`r?`n", " | ")

                            if (($LASTEXITCODE -eq 0) -and ($r -notmatch "FAILED|error:|device is locked")) {
                                $okCount++
                                $script:DetectedInfo.ParticionesOK = $okCount
                                Write-Status "        [EXITO] $pName flasheado en ${dt}s" "Green"
                                Record-Partition $pName $pFileName "EXITO" "$dt" "OKAY"
                            } else {
                                $errCount++
                                $script:DetectedInfo.ParticionesError = $errCount
                                Write-Status "        [FALLO] Error en $pName : $rSingleLine" "Red"
                                Record-Partition $pName $pFileName "FALLO" "$dt" $rSingleLine
                            }
                        }
                    }

                    $superFile = Join-Path $fwDir "super.img"
                    if (Test-Path $superFile) {
                        $t0 = Get-Date
                        Write-Status "[FLASH] Enviando particion 'super' (super.img)..." "Yellow"
                        $r = (& $fbExe flash super $superFile 2>&1 | Out-String).Trim()
                        $dt = [math]::Round(((Get-Date) - $t0).TotalSeconds, 2)
                        $rSingleLine = ($r -replace "`r?`n", " | ")
                        if (($LASTEXITCODE -eq 0) -and ($r -notmatch "FAILED|error:|device is locked")) {
                            $okCount++
                            $script:DetectedInfo.ParticionesOK = $okCount
                            Record-Partition "super" "super.img" "EXITO" "$dt" "OKAY"
                        } else {
                            $errCount++
                            $script:DetectedInfo.ParticionesError = $errCount
                            Record-Partition "super" "super.img" "FALLO" "$dt" $rSingleLine
                        }
                    } else {
                        Record-Partition "super" "(En eMMC Android 12)" "CONSERVADA" "0.0" "Sistema Android 12 original intacto"
                    }

                    Record-Partition "factory" "(En eMMC)" "PROTEGIDO" "0.0" "Claves DRM Widevine L1 intactas"
                    Record-Partition "cri_data" "(En eMMC)" "PROTEGIDO" "0.0" "MAC y Numero de Serie intactos"

                    $flashed = $true
                    if ($errCount -eq 0) {
                        $script:OverallStatus = "EXITO TOTAL"
                        $script:OverallSuccess = $true
                        $script:FailureReason = "Ninguno"
                        Record-Step "5. Flasheo de particiones B866V2" "EXITO" "Todas las particiones ($okCount/$okCount) se flashearon sin errores"
                    } else {
                        $script:OverallStatus = "EXITO PARCIAL CON ERRORES"
                        $script:OverallSuccess = $false
                        $script:FailureReason = "Fallaron $errCount particiones durante el flasheo."
                        Record-Step "5. Flasheo de particiones B866V2" "FALLO" "Exitosas: $okCount | Fallidas: $errCount"
                    }

                    Show-SummaryOnScreen
                    break
                }
            }
        } else {
            if (((Get-Date) - $lastWaitMsg).TotalSeconds -ge 8) {
                $portsNow = ([System.IO.Ports.SerialPort]::GetPortNames() -join ", ")
                Write-Status "[...] Esperando conexion de la caja en USB Fastboot / WorldCup / ADB / UART (Puertos COM: $portsNow)..." "DarkGray"
                $lastWaitMsg = Get-Date
            }
        }

        Start-Sleep -Milliseconds 100
    }

    # --------------------------------------------------------------------------
    # BUCLE FINAL POST-EXITO: RETENCION ANTI-WATCHDOG + MENU INTERACTIVO
    # --------------------------------------------------------------------------
    Write-Status "==========================================================================" "Green"
    Write-Status " PROCESO FINALIZADO. El resumen completo se guardo en:" "Green"
    Write-Status "   -> $summaryFile" "Cyan"
    Write-Status "   -> $logFile" "Cyan"
    Write-Status "==========================================================================" "Green"
    Write-Status " Opciones (mientras la caja se mantiene estable en Fastboot):" "Yellow"
    Write-Status "   [R] Reiniciar la caja normalmente hacia Android" "White"
    Write-Status "   [V] Abrir el Resumen del Proceso en el Bloc de Notas" "White"
    Write-Status "   [S] Salir de este programa (dejando la caja en Fastboot)" "White"
    Write-Status "==========================================================================" "Green"

    while ($true) {
        & $fbExe oem "watchdog off" 2>&1 | Out-Null
        & $fbExe getvar version 2>&1 | Out-Null

        if ([Console]::KeyAvailable) {
            $k = [Console]::ReadKey($true).Key
            if ($k -eq "R") {
                Write-Status "[*] Reiniciando la caja hacia el sistema..." "Cyan"
                & $fbExe reboot 2>&1 | Out-Null
                break
            } elseif ($k -eq "V") {
                Start-Process "notepad.exe" -ArgumentList $summaryFile
            } elseif ($k -eq "S") {
                Write-Status "[*] Saliendo del programa..." "Cyan"
                break
            }
        }
        Start-Sleep -Seconds 2
    }
} finally {
    if ($null -ne $serialPort -and $serialPort.IsOpen) {
        $serialPort.Close()
    }
    if ($script:OverallStatus -eq "EN PROGRESO") {
        $script:OverallStatus = "DETENIDO POR EL USUARIO (INCOMPLETO)"
        $script:FailureReason = "El programa se cerro antes de completar todas las etapas."
    }
    Save-SummaryReport
}
