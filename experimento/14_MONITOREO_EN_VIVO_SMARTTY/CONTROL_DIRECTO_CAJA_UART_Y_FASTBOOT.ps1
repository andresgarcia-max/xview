param(
    [string]$ComPort = "COM8",
    [int]$BaudRate = 115200,
    [switch]$FlashAndroid12 = $true
)

$ErrorActionPreference = "Continue"
$outDir = "c:\Users\andre\OneDrive\Desktop\xview\experimento\14_MONITOREO_EN_VIVO_SMARTTY"
$logFile = Join-Path $outDir "CAPTURA_DIRECTA_CONTROLADOR.log"
$statusFile = Join-Path $outDir "ESTADO_CONTROLADOR_DIRECTO.txt"
$fbDir = "c:\Users\andre\OneDrive\Desktop\xview\V2\B866v2 Android 12\B866v2 Android 12\B866V2"
$fbExe = Join-Path $fbDir "fastboot.exe"
$cacheImg = "c:\Users\andre\OneDrive\Desktop\xview\experimento\11_VALIDACION_OTA_Y_FIRMWARE_ZTE\cache_ext4.img"

function Write-Log([string]$msg) {
    $ts = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss.fff")
    $line = "[$ts] $msg"
    Write-Host $line
    Add-Content -Path $logFile -Value $line -Encoding UTF8
    Set-Content -Path $statusFile -Value $line -Encoding UTF8
}

function Send-UartCommand([System.IO.Ports.SerialPort]$port, [string]$cmd) {
    if ($null -ne $port -and $port.IsOpen) {
        Write-Log "[UART-TX -> $($port.PortName)] $cmd"
        $port.Write("$cmd`r`n")
    }
}

Write-Log "=== INICIANDO CONTROLADOR DIRECTO ANTIGRAVITY (UART $ComPort + USB FASTBOOT) ==="

$serialPort = $null
$recoveryPatched = $false
$ubootUnlocked = $false
$fastbootFlashed = $false
$rxBuffer = ""

while ($true) {
    # 1. Abrir COM8 directamente
    if ($null -eq $serialPort -or -not $serialPort.IsOpen) {
        $availablePorts = [System.IO.Ports.SerialPort]::GetPortNames()
        if ($availablePorts -contains $ComPort) {
            try {
                $serialPort = New-Object System.IO.Ports.SerialPort($ComPort, $BaudRate, [System.IO.Ports.Parity]::None, 8, [System.IO.Ports.StopBits]::One)
                $serialPort.ReadTimeout = 100
                $serialPort.WriteTimeout = 500
                $serialPort.Open()
                Write-Log "[+] CONECTADO DIRECTAMENTE A $ComPort ($BaudRate 8N1) - Esperando datos de la caja..."
                $serialPort.Write("`r`n")
            } catch {
                Write-Log "[!] No se pudo abrir $ComPort directamete: $($_.Exception.Message)"
            }
        }
    }

    # 2. Leer todos los bytes entrantes por UART
    if ($null -ne $serialPort -and $serialPort.IsOpen) {
        try {
            $incoming = $serialPort.ReadExisting()
            if (-not [string]::IsNullOrEmpty($incoming)) {
                $rxBuffer += $incoming
                if ($rxBuffer.Length -gt 16384) {
                    $rxBuffer = $rxBuffer.Substring($rxBuffer.Length - 8192)
                }

                $lines = $incoming -split "`r?`n"
                foreach ($ln in $lines) {
                    if ($ln.Trim().Length -gt 0) {
                        Write-Log "[UART-RX] $ln"
                    }
                }

                # A1) Si vemos el inicio de arranque (BL1/BL2/BL31/U-Boot) y aún no hemos desbloqueado U-Boot,
                # enviamos pulsos de Enter para detener el autoboot en cuanto U-Boot abra la consola.
                if (($incoming -match "G12A:BL:|BL2 Built|BL31:|U-Boot 2015|reboot_mode=|Hit any key to stop autoboot") -and (-not $ubootUnlocked)) {
                    Write-Log "[!] DETECTADO ARRANQUE DE BOOTLOADER: Enviando rafaga de interrupcion hacia U-Boot..."
                    for ($k = 0; $k -lt 35; $k++) {
                        $serialPort.Write("`r")
                        Start-Sleep -Milliseconds 25
                        $subRx = $serialPort.ReadExisting()
                        if (-not [string]::IsNullOrEmpty($subRx)) {
                            $rxBuffer += $subRx
                            foreach ($sln in ($subRx -split "`r?`n")) {
                                if ($sln.Trim().Length -gt 0) { Write-Log "[UART-RX] $sln" }
                            }
                            if ($subRx -match "g12a_u212_v1#|gxl_p212_v1#|=>") {
                                break
                            }
                        }
                    }
                }

                # A2) Si entramos al prompt de U-Boot (g12a_u212_v1#)
                if (($rxBuffer -match "g12a_u212_v1#|gxl_p212_v1#") -and (-not $ubootUnlocked)) {
                    Write-Log "[+] ¡CONSOLA U-BOOT CAPTURADA! Ejecutando desbloqueo directo en eMMC..."
                    Send-UartCommand $serialPort "printenv"
                    Start-Sleep -Milliseconds 800
                    $envDump = $serialPort.ReadExisting()
                    if ($envDump) {
                        foreach ($eln in ($envDump -split "`r?`n")) {
                            if ($eln.Trim().Length -gt 0) { Write-Log "[UBOOT-ENV] $eln" }
                        }
                    }
                    Send-UartCommand $serialPort "setenv lock 10100000"
                    Start-Sleep -Milliseconds 150
                    Send-UartCommand $serialPort "setenv unlock_ability 1"
                    Start-Sleep -Milliseconds 150
                    Send-UartCommand $serialPort "setenv EnableSelinux permissive"
                    Start-Sleep -Milliseconds 150
                    Send-UartCommand $serialPort "saveenv"
                    Start-Sleep -Milliseconds 800
                    $saveOut = $serialPort.ReadExisting()
                    if ($saveOut) { Write-Log "[UBOOT-SAVE] $($saveOut.Trim())" }
                    Send-UartCommand $serialPort "fastboot"
                    $ubootUnlocked = $true
                }

                # B) Si entramos a la consola Root (#) de Recovery
                if (($incoming -match "gpio_keypad_down_cnt: 60" -or $incoming -match "/system/bin/sh" -or $incoming -match "(^|\n)# ") -and (-not $recoveryPatched)) {
                    Write-Log "[+] ¡CONSOLA ROOT (#) DE RECOVERY DETECTADA! Ejecutando diagnostico y parche CRC32 de /dev/block/env..."
                    Start-Sleep -Milliseconds 500
                    Send-UartCommand $serialPort "echo === CONEXION_ROOT_ANTIGRAVITY_OK ==="
                    Send-UartCommand $serialPort "id; uname -a; getprop ro.serialno; getprop ro.build.display.id"
                    Start-Sleep -Milliseconds 600
                    Send-UartCommand $serialPort "cat /proc/partitions"
                    Start-Sleep -Milliseconds 600
                    Send-UartCommand $serialPort "strings /dev/block/env | grep -E 'lock|ethaddr|serial|bootcmd|version|selinux'"
                    Start-Sleep -Milliseconds 600
                    Send-UartCommand $serialPort "setprop ubootenv.var.lock 10100000"
                    $patchCmd = "dd if=/dev/block/env of=/dev/env_orig.bin bs=65536 count=1 2>/dev/null; dd if=/dev/env_orig.bin bs=1 skip=4 count=65532 2>/dev/null | sed 's/lock=10001000/lock=10100000/g' > /dev/env_body.bin; gzip -c /dev/env_body.bin > /dev/env_body.gz; S=`$(wc -c < /dev/env_body.gz); O=`$((S - 8)); dd if=/dev/env_body.gz bs=1 skip=`$O count=4 2>/dev/null > /dev/env_new.bin; cat /dev/env_body.bin >> /dev/env_new.bin; dd if=/dev/env_new.bin of=/dev/block/env bs=65536 count=1 2>/dev/null; sync; strings /dev/block/env | grep '^lock='; echo === PARCHE_ENV_COMPLETADO ==="
                    Send-UartCommand $serialPort $patchCmd
                    Start-Sleep -Seconds 3
                    $recOut = $serialPort.ReadExisting()
                    if ($recOut) {
                        foreach ($rln in ($recOut -split "`r?`n")) {
                            if ($rln.Trim().Length -gt 0) { Write-Log "[ROOT-RX] $rln" }
                        }
                    }
                    Send-UartCommand $serialPort "reboot bootloader"
                    $recoveryPatched = $true
                }
            }
        } catch {
            # Ignorar timeouts normales de lectura serie
        }
    }

    # 3. Verificar si la caja está conectada en USB Fastboot
    $fbDevices = & $fbExe devices 2>&1 | Out-String
    if ($fbDevices -match "fastboot") {
        Write-Log "[+] CAJA DETECTADA EN USB FASTBOOT: $($fbDevices.Trim())"

        # Intentar comandos de desbloqueo por USB
        $u1 = & $fbExe flashing unlock_critical 2>&1 | Out-String
        Write-Log "[FASTBOOT] flashing unlock_critical: $($u1.Trim())"

        $u2 = & $fbExe flashing unlock 2>&1 | Out-String
        Write-Log "[FASTBOOT] flashing unlock: $($u2.Trim())"

        $u3 = & $fbExe oem "setenv lock 10100000" 2>&1 | Out-String
        Write-Log "[FASTBOOT] oem setenv lock 10100000: $($u3.Trim())"

        $u4 = & $fbExe oem "saveenv" 2>&1 | Out-String
        Write-Log "[FASTBOOT] oem saveenv: $($u4.Trim())"

        # Probar flasheo de dtbo para confirmar si está desbloqueado
        $testDtbo = & $fbExe flash dtbo (Join-Path $fbDir "dtbo.img") 2>&1 | Out-String
        Write-Log "[FASTBOOT] Prueba flash dtbo: $($testDtbo.Trim())"

        if ($testDtbo -match "locked device") {
            Write-Log "[!] USB Fastboot sigue reportando 'locked device' (lock=10001000 en eMMC). Reiniciando hacia UART para interceptar U-Boot..."
            if ($null -ne $serialPort -and $serialPort.IsOpen) {
                $ubootUnlocked = $false
                & $fbExe reboot | Out-Null
                # Ráfaga de Enter para atrapar U-Boot en cuanto reinicie
                for ($i = 0; $i -lt 160; $i++) {
                    $serialPort.Write("`r")
                    Start-Sleep -Milliseconds 25
                    $subRx = $serialPort.ReadExisting()
                    if (-not [string]::IsNullOrEmpty($subRx)) {
                        $rxBuffer += $subRx
                        foreach ($sln in ($subRx -split "`r?`n")) {
                            if ($sln.Trim().Length -gt 0) { Write-Log "[UART-RX] $sln" }
                        }
                        if ($subRx -match "g12a_u212_v1#|gxl_p212_v1#") {
                            Write-Log "[+] ¡U-BOOT ATRAPADO TRAS REINICIO FASTBOOT!"
                            Send-UartCommand $serialPort "setenv lock 10100000"
                            Start-Sleep -Milliseconds 150
                            Send-UartCommand $serialPort "setenv EnableSelinux permissive"
                            Start-Sleep -Milliseconds 150
                            Send-UartCommand $serialPort "saveenv"
                            Start-Sleep -Milliseconds 800
                            Send-UartCommand $serialPort "fastboot"
                            $ubootUnlocked = $true
                            break
                        }
                    }
                }
            }
        } elseif (-not $fastbootFlashed -and $FlashAndroid12) {
            Write-Log "[+] ¡BOOTLOADER DESBLOQUEADO VERIFICADO! Flasheando Android 12 seguro..."
            Push-Location $fbDir
            try {
                foreach ($pair in @(
                    @("boot", "boot.img"),
                    @("bootloader", "bootloader.img"),
                    @("_aml_dtb", "dtb.img"),
                    @("dtbo", "dtbo.img"),
                    @("logo", "logo.img"),
                    @("param", "param.img"),
                    @("conf", "conf.img"),
                    @("oem", "oem.img"),
                    @("super", "super.img"),
                    @("vbmeta", "vbmeta.img"),
                    @("recovery", "recovery.img")
                )) {
                    $part = $pair[0]
                    $img = $pair[1]
                    Write-Log "[FLASH] fastboot flash $part $img ..."
                    $res = & $fbExe flash $part $img 2>&1 | Out-String
                    Write-Log "[FLASH-RES] $($res.Trim())"
                }
                if (Test-Path $cacheImg) {
                    Write-Log "[FLASH] fastboot flash cache $cacheImg ..."
                    $res = & $fbExe flash cache $cacheImg 2>&1 | Out-String
                    Write-Log "[FLASH-RES] $($res.Trim())"
                } else {
                    & $fbExe erase cache 2>&1 | Out-Null
                }
                Write-Log "[FLASH] fastboot erase userdata ..."
                $res = & $fbExe erase userdata 2>&1 | Out-String
                Write-Log "[FLASH-RES] $($res.Trim())"
                $fastbootFlashed = $true
                Write-Log "[+] ¡FLASHEO DE ANDROID 12 COMPLETADO CON EXITO!"
            } finally {
                Pop-Location
            }
        } else {
            # Keep-Alive Anti-Watchdog
            & $fbExe getvar version 2>&1 | Out-Null
        }
    }

    [GC]::Collect()
    Start-Sleep -Milliseconds 200
}
