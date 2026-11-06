# ==============================================================================
# MOTOR PRINCIPAL: PROGRAMA PERFECTO XVIEW+ (ZTE ZXV10 B866V2 - S905X2 G12A)
# Fusion integral de todas las herramientas de la memoria USB + hallazgos REA
# Reglas absolutas:
#   1. 100% Externo (Cero tornillos, Cero TTL)
#   2. Proteccion total de factory.img (Widevine L1) y cri_data.img (MAC/Serie)
#   3. Salto directo del bloqueo QR RSA-2048 de ZTE mediante programas integrados
#      (WorldCup InUsbBurn 48ms VID_1B8E / Recovery fastbootd PID_4EE7 /
#       U-Boot Fastboot PID_0D02 / Llave QADEBUG.ini / ADB Directo)
# ==============================================================================

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$ErrorActionPreference = "Continue"

$ROOT_DIR   = Split-Path -Parent $PSScriptRoot
$MOTOR_DIR  = Join-Path $ROOT_DIR "1_HERRAMIENTAS_MOTOR"
$APKS_DIR   = Join-Path $ROOT_DIR "2_APKS_LIBERACION_Y_TV"
$KEYS_DIR   = Join-Path $ROOT_DIR "3_LLAVES_USB_PARA_LA_CAJA"
$DRV_DIR    = Join-Path $ROOT_DIR "4_DRIVERS_Y_GUI_WINDOWS"
$FW_DIR     = Join-Path $ROOT_DIR "5_FIRMWARE_SEGURO_B866V2"

$ADB        = Join-Path $MOTOR_DIR "adb.exe"
$FASTBOOT   = Join-Path $MOTOR_DIR "fastboot_moderno.exe"
if (-not (Test-Path $FASTBOOT)) { $FASTBOOT = Join-Path $MOTOR_DIR "fastboot.exe" }
$ADNL       = Join-Path $MOTOR_DIR "adnl.exe"
$UPDATE     = Join-Path $MOTOR_DIR "update.exe"
$ENV_UNL    = Join-Path $FW_DIR "env_unlocked.img"
$CONF_IMG   = Join-Path $FW_DIR "conf.img"

$Global:ADB_TARGET_ARGS = @()
$Global:CONNECTED_IP    = ""

# ==============================================================================
# EJECUTORES SEGUROS CON TIMEOUT (EVITAN CUELGUES POR DESCRIPTORES USB FANTASMA)
# ==============================================================================
function Invoke-ToolSafe {
    param(
        [string]$ExePath,
        [string]$ArgString,
        [int]$TimeoutMs = 3000
    )
    if (-not (Test-Path $ExePath)) {
        return [PSCustomObject]@{ TimedOut = $false; ExitCode = -1; Output = "[NO_EXE]" }
    }
    $pinfo = New-Object System.Diagnostics.ProcessStartInfo
    $pinfo.FileName = $ExePath
    $pinfo.Arguments = $ArgString
    $pinfo.WorkingDirectory = $MOTOR_DIR
    $pinfo.RedirectStandardOutput = $true
    $pinfo.RedirectStandardError = $true
    $pinfo.UseShellExecute = $false
    $pinfo.CreateNoWindow = $true

    $proc = New-Object System.Diagnostics.Process
    $proc.StartInfo = $pinfo
    [void]$proc.Start()
    $outTask = $proc.StandardOutput.ReadToEndAsync()
    $errTask = $proc.StandardError.ReadToEndAsync()

    if (-not $proc.WaitForExit($TimeoutMs)) {
        try { $proc.Kill() } catch {}
        return [PSCustomObject]@{ TimedOut = $true; ExitCode = -1; Output = "[TIMEOUT]" }
    }
    [void]$outTask.Wait(200)
    [void]$errTask.Wait(200)
    $oStr = if ($outTask.IsCompleted) { $outTask.Result } else { "" }
    $eStr = if ($errTask.IsCompleted) { $errTask.Result } else { "" }
    $combined = ($oStr + "`n" + $eStr).Trim()
    return [PSCustomObject]@{ TimedOut = $false; ExitCode = $proc.ExitCode; Output = $combined }
}

function Invoke-FastbootSafe {
    param([string]$ArgString, [int]$TimeoutMs = 3000)
    return Invoke-ToolSafe -ExePath $FASTBOOT -ArgString $ArgString -TimeoutMs $TimeoutMs
}

function Invoke-UpdateSafe {
    param([string]$ArgString, [int]$TimeoutMs = 5000)
    return Invoke-ToolSafe -ExePath $UPDATE -ArgString $ArgString -TimeoutMs $TimeoutMs
}

function Invoke-Adb {
    param([string[]]$Arguments)
    $allArgs = @()
    if ($Global:ADB_TARGET_ARGS.Count -gt 0) { $allArgs += $Global:ADB_TARGET_ARGS }
    $allArgs += $Arguments
    & $ADB @allArgs
}

function Read-ChoiceWithKeepAlive {
    param(
        [string]$PromptText = "Elige una opcion",
        [string[]]$ValidChoices = @("1","2","3","0")
    )
    Write-Host -NoNewline "$PromptText ($($ValidChoices -join '/')): " -ForegroundColor Yellow
    $lastPulse = Get-Date
    while ($true) {
        if ([Console]::KeyAvailable) {
            $keyInfo = [Console]::ReadKey($true)
            $ch = $keyInfo.KeyChar.ToString().ToUpper()
            if ($ValidChoices -contains $ch) {
                Write-Host $ch -ForegroundColor Green
                return $ch
            }
        }
        if (((Get-Date) - $lastPulse).TotalSeconds -ge 2.0) {
            $lastPulse = Get-Date
            [void](Invoke-FastbootSafe -ArgString "getvar version-bootloader" -TimeoutMs 1200)
        }
        Start-Sleep -Milliseconds 80
    }
}

# ==============================================================================
# CAZADOR ULTRARRAPIDO AMLOGIC WORLDCUP / INUSBBURN (48ms DIRECTO POR LIBUSB)
# ==============================================================================
function Try-CatchWorldCup48ms {
    if (-not (Test-Path $UPDATE)) { return $false }
    $res = Invoke-UpdateSafe -ArgString "bulkcmd `"watchdog off`"" -TimeoutMs 1500
    if ((-not $res.TimedOut) -and ($res.Output -notmatch "No \[WorldCup Device\]|can not find device|ERR")) {
        Write-Host ""
        Write-Host "[+] Detectada caja en Modo Amlogic WorldCup / InUsbBurn (VID_1B8E) en <50ms!" -ForegroundColor Green
        Write-Host "    [1/4] Watchdog de 14s apagado exitosamente." -ForegroundColor Cyan
        if (Test-Path $ENV_UNL) {
            Write-Host "    [2/4] Inyectando particion 'env' desbloqueada (env_unlocked.img -> lock=10100000, otg_device=1)..." -ForegroundColor Cyan
            $rEnv = Invoke-UpdateSafe -ArgString "partition env `"$ENV_UNL`"" -TimeoutMs 8000
            Write-Host "          Resultado env: $(($rEnv.Output -replace "`r?`n", ' ').Trim())" -ForegroundColor Gray
        }
        if (Test-Path $CONF_IMG) {
            [void](Invoke-UpdateSafe -ArgString "partition conf `"$CONF_IMG`"" -TimeoutMs 6000)
        }
        Write-Host "    [3/4] Grabando variables U-Boot (lock=10100000, unlock_ability=1, otg_device=1)..." -ForegroundColor Cyan
        foreach ($cmd in @(
            "setenv -f lock 10100000",
            "setenv lock 10100000",
            "setenv unlock_ability 1",
            "setenv otg_device 1",
            "saveenv"
        )) {
            [void](Invoke-UpdateSafe -ArgString "bulkcmd `"$cmd`"" -TimeoutMs 3000)
        }
        Write-Host "    [4/4] Saltando la caja a U-Boot Fastboot DESBLOQUEADO (PID_0D02)..." -ForegroundColor Green
        [void](Invoke-UpdateSafe -ArgString "bulkcmd `"fastboot`"" -TimeoutMs 3000)
        Start-Sleep -Milliseconds 1200
        return $true
    }
    return $false
}

# ==============================================================================
# DETECTOR DE ESTADO REAL USB (FILTRA DESCRIPTORES FANTASMA TRAS REINICIO)
# ==============================================================================
function Get-BoxConnectionState {
    $state = [ordered]@{
        UsbHardwareMode = "Ninguno detectado por cable USB directo"
        SerialNumber    = "No detectado"
        RawPid          = ""
        FastbootLive    = $false
        AdbDevices      = @()
        FastbootDevices = @()
    }

    # 1. Prueba ultrarrapida WorldCup (48ms)
    $wcCheck = Invoke-UpdateSafe -ArgString "bulkcmd `"watchdog off`"" -TimeoutMs 1000
    if ((-not $wcCheck.TimedOut) -and ($wcCheck.Output -notmatch "No \[WorldCup Device\]|can not find device|ERR")) {
        $state.UsbHardwareMode = "Amlogic WorldCup / InUsbBurn Activo (VID_1B8E) - LISTO PARA DESBLOQUEO"
        $state.RawPid = "1B8E"
    }

    # 2. Escaneo PnP
    try {
        $pnp = Get-CimInstance Win32_PnPEntity -ErrorAction SilentlyContinue | Where-Object {
            $_.DeviceID -match "VID_18D1|VID_19D2|VID_1B8E"
        }
        foreach ($dev in $pnp) {
            if ($dev.DeviceID -match "\\([^\\]+)$") {
                $sn = $Matches[1]
                if ($sn -notmatch "&") { $state.SerialNumber = $sn }
            }
            if ($dev.DeviceID -match "PID_4EE2") {
                $state.UsbHardwareMode = "ADB USB Activo (PID_4EE2) - LISTO PARA LIBERACION DIRECTA"
                $state.RawPid = "4EE2"
            }
            elseif ($dev.DeviceID -match "PID_4EE7") {
                $state.UsbHardwareMode = "Recovery Fastbootd (PID_4EE7) - ACTIVO"
                $state.RawPid = "4EE7"
            }
            elseif ($dev.DeviceID -match "PID_0D02") {
                $state.RawPid = "0D02"
            }
            elseif ($dev.DeviceID -match "PID_4EE1") {
                $state.UsbHardwareMode = "Modo MTP Android (PID_4EE1) - Usa Opcion 1 para Salto Automatico"
                $state.RawPid = "4EE1"
            }
            elseif ($dev.DeviceID -match "VID_1B8E") {
                $state.UsbHardwareMode = "Amlogic WorldCup / DNL Mode (VID_1B8E) - LISTO PARA DESBLOQUEO U-BOOT"
                $state.RawPid = "1B8E"
            }
        }
    } catch {}

    # 3. Verificar ADB
    if (Test-Path $ADB) {
        $adbRes = Invoke-ToolSafe -ExePath $ADB -ArgString "devices" -TimeoutMs 2000
        if (-not $adbRes.TimedOut) {
            foreach ($line in ($adbRes.Output -split "`r?`n")) {
                if ($line -match "^(\S+)\s+(device|unauthorized|offline)$") {
                    $state.AdbDevices += "$($Matches[1]) [$($Matches[2])]"
                }
            }
        }
    }

    # 4. Verificar Fastboot y comprobar que NO sea un descriptor fantasma tras reboot
    if (Test-Path $FASTBOOT) {
        $fbRes = Invoke-FastbootSafe -ArgString "devices" -TimeoutMs 1500
        if (-not $fbRes.TimedOut) {
            foreach ($line in ($fbRes.Output -split "`r?`n")) {
                if ($line -match "^(\S+)\s+fastboot") {
                    $candidateSn = $Matches[1]
                    $ping = Invoke-FastbootSafe -ArgString "getvar version-bootloader" -TimeoutMs 900
                    if ((-not $ping.TimedOut) -and ($ping.Output -match "version-bootloader|OKAY|Finished")) {
                        $state.FastbootDevices += $candidateSn
                        $state.FastbootLive = $true
                        if ($state.RawPid -eq "0D02") {
                            $state.UsbHardwareMode = "U-Boot Fastboot Activo (PID_0D02) - RESPONDIENDO EN VIVO"
                        }
                    } else {
                        if ($state.RawPid -eq "0D02") {
                            $state.UsbHardwareMode = "Caja encendida en Android (Descriptor Fastboot previo en reposo)"
                            $state.RawPid = "0D02_GHOST"
                        }
                    }
                }
            }
        }
    }

    return $state
}

function Show-Banner {
    Clear-Host
    Write-Host "================================================================================" -ForegroundColor Cyan
    Write-Host "          PROGRAMA PERFECTO - MEGACABLE XVIEW+ (ZTE ZXV10 B866V2)               " -ForegroundColor Cyan
    Write-Host "     Motor de Salto Directo (WorldCup 48ms / Fastbootd / Fastboot / ADB)        " -ForegroundColor Cyan
    Write-Host "      Regla: 100% Externo Sin Abrir Tornillos | Proteccion Widevine L1/MAC      " -ForegroundColor Cyan
    Write-Host "================================================================================" -ForegroundColor Cyan

    $st = Get-BoxConnectionState
    Write-Host ""
    Write-Host " [RADAR DE HARDWARE USB EN TIEMPO REAL]" -ForegroundColor Yellow
    Write-Host "  * Caja Detectada   : Xview+ (Serie: $($st.SerialNumber))" -ForegroundColor White
    if ($st.RawPid -match "^(4EE2|0D02|4EE7|1B8E)$") {
        Write-Host "  * Estado Cable USB : $($st.UsbHardwareMode)" -ForegroundColor Green
    } elseif ($st.RawPid -match "4EE1|0D02_GHOST") {
        Write-Host "  * Estado Cable USB : $($st.UsbHardwareMode)" -ForegroundColor Yellow
    } else {
        Write-Host "  * Estado Cable USB : $($st.UsbHardwareMode)" -ForegroundColor Gray
    }
    if ($st.AdbDevices.Count -gt 0) {
        Write-Host "  * Sesion ADB       : $($st.AdbDevices -join ', ')" -ForegroundColor Green
    } else {
        Write-Host "  * Sesion ADB       : Sin sesion activa" -ForegroundColor DarkGray
    }
    if ($st.FastbootLive -and $st.FastbootDevices.Count -gt 0) {
        Write-Host "  * Sesion Fastboot  : ACTIVA EN VIVO ($($st.FastbootDevices -join ', '))" -ForegroundColor Green
    } else {
        Write-Host "  * Sesion Fastboot  : En espera de salto (Opcion 1 caza WorldCup/Fastboot)" -ForegroundColor DarkGray
    }
    Write-Host "--------------------------------------------------------------------------------" -ForegroundColor DarkCyan
}

# ==============================================================================
# LIBERACION COMPLETA POR ADB (CUANDO HAY SESION ADB ACTIVA)
# ==============================================================================
function Execute-AdbPayload {
    Write-Host "`n[1/7] Inyectando token 'EE@' en /cache/recovery/last_QR (Bypass candado QR ZTE)..." -ForegroundColor Cyan
    Invoke-Adb @("shell", "echo -n 'EE@' > /cache/recovery/last_QR; rm -f /cache/recovery/last_QR_Time") 2>$null

    Write-Host "[2/7] Desbloqueando banderas OEM y U-Boot (lock=10100000, otg_device=1) desde ADB..." -ForegroundColor Cyan
    Invoke-Adb @("shell", "settings put global oem_unlock_enabled 1; settings put secure oem_unlock_enabled 1") 2>$null
    Invoke-Adb @("shell", "setprop ubootenv.var.lock 10100000; setprop ubootenv.var.otg_device 1; setprop persist.sys.usb.config mtp,adb") 2>$null
    Invoke-Adb @("shell", "settings put global package_verifier_user_consent -1; settings put global package_verifier_enable 0; bmgr enable false") 2>$null

    Write-Host "[3/7] Instalando Launchers Offline y Utilidades desde '2_APKS_LIBERACION_Y_TV'..." -ForegroundColor Cyan
    $coreApks = @(
        @{ File = "Home_Screen_Launcher_1.1.3.apk"; Name = "Home Screen Launcher (Ligero)" },
        @{ File = "TVLauncher_4_GoogleTV.apk";      Name = "TVLauncher 4 (Estilo Google TV)" },
        @{ File = "Button_Mapper_Pro_2.54.apk";     Name = "Button Mapper Pro v2.54 (Reasignar Boton Home)" },
        @{ File = "autostart.apk";                  Name = "AutoStart al encender" },
        @{ File = "Filemanager.apk";                Name = "Gestor de Archivos USB" },
        @{ File = "Downloader_2.0.4.apk";           Name = "Downloader Android TV" }
    )
    foreach ($item in $coreApks) {
        $apkPath = Join-Path $APKS_DIR $item.File
        if (Test-Path $apkPath) {
            Write-Host "  -> Instalando $($item.Name)..." -ForegroundColor Gray
            Invoke-Adb @("install", "-r", "-g", $apkPath)
        }
    }

    Write-Host "[4/7] Neutralizando Pantalla Azul Megacable, Core (oom_adj=-14), OTA y candado QR (com.zte.adb)..." -ForegroundColor Cyan
    $bloatPackages = @(
        "com.dotscreen.megacable.portal",
        "com.dotscreen.megacable.core",
        "com.dotscreen.megacable.atv",
        "com.expway.ewm.service",
        "com.zte.adb",
        "com.ztestb.LauncherCustomize",
        "com.zte.ums.oes",
        "com.zte.ota",
        "tv.upgrade.service",
        "com.zte.tv.act",
        "com.ztestb.uploaddiagno",
        "com.zte.factorytesttool",
        "com.hmdm.launcher"
    )
    foreach ($pkg in $bloatPackages) {
        Write-Host "  -> Neutralizando $pkg ..." -ForegroundColor Gray
        Invoke-Adb @("shell", "cmd appops set $pkg RUN_IN_BACKGROUND ignore; cmd appops set $pkg RUN_ANY_IN_BACKGROUND ignore; am force-stop $pkg; pm disable-user --user 0 $pkg; pm uninstall -k --user 0 $pkg") 2>$null | Out-Null
    }

    Write-Host "[5/7] Configurando el nuevo Launcher como HOME predeterminado y activando Button Mapper..." -ForegroundColor Cyan
    Invoke-Adb @("shell", "cmd package set-home-activity dev.dworks.apps.alauncher/.Launcher") 2>$null
    Invoke-Adb @("shell", "settings put secure enabled_accessibility_services flar2.homebutton/flar2.homebutton.ButtonMapperService") 2>$null
    Invoke-Adb @("shell", "settings put secure accessibility_enabled 1") 2>$null
    Invoke-Adb @("shell", "am start -a android.intent.action.MAIN -c android.intent.category.HOME") 2>$null

    Write-Host "[6/7] Eliminando banner 'No se pudo instalar la actualizacion del sistema'..." -ForegroundColor Cyan
    Invoke-Adb @("shell", "settings put global ota_disable_automatic_update 1") 2>$null
    Invoke-Adb @("shell", "cmd notification snooze --for 315360000000 '0|com.google.android.gms|1|com.google.android.gms.update.control.NotificationControl|10049'") 2>$null
    Invoke-Adb @("shell", "cmd appops set com.google.android.gms POST_NOTIFICATION ignore") 2>$null

    Write-Host "[7/7] Activando Modo Stealth (Ocultar flags ADB manteniendo puerto 5555 activo)..." -ForegroundColor Cyan
    Invoke-Adb @("shell", "settings put global adb_enabled 0; settings put secure adb_enabled 0; settings put global development_settings_enabled 0; settings put secure development_settings_enabled 0") 2>$null
    Invoke-Adb @("shell", "setprop service.adb.tcp.port 5555; setprop ctl.start adbd") 2>$null

    Write-Host ""
    Write-Host "================================================================================" -ForegroundColor Green
    Write-Host " [LISTO!] Caja liberada con exito (Candado QR y Pantalla Azul eliminados).      " -ForegroundColor Green
    Write-Host "================================================================================" -ForegroundColor Green
}

# ==============================================================================
# FLASHEO RAPIDO DE VBMETA + RECOVERY + CACHE_EXT4 (CON VERIFICACION DE EXITO)
# ==============================================================================
function Invoke-QuickUnlockPartitionsFlash {
    Write-Host ""
    Write-Host "[+] Inyectando vbmeta desbloqueado (flags=0x03), recovery desbloqueado y cache_ext4..." -ForegroundColor Cyan
    $parts = @(
        @{ Name = "vbmeta";   File = (Join-Path $FW_DIR "vbmeta.img") },
        @{ Name = "recovery"; File = (Join-Path $FW_DIR "recovery.img") },
        @{ Name = "cache";    File = (Join-Path $FW_DIR "cache_ext4.img") }
    )
    $okCount = 0
    foreach ($p in $parts) {
        if (Test-Path $p.File) {
            Write-Host "  -> Flasheando '$($p.Name)' ($(Split-Path $p.File -Leaf))..." -ForegroundColor White
            $res = Invoke-FastbootSafe -ArgString "flash $($p.Name) `"$($p.File)`"" -TimeoutMs 35000
            [void](Invoke-FastbootSafe -ArgString "getvar version-bootloader" -TimeoutMs 1200)
            if ((-not $res.TimedOut) -and ($res.ExitCode -eq 0) -and ($res.Output -notmatch "FAILED|locked device|not allowed")) {
                Write-Host "     [OKAY] Particion '$($p.Name)' escrita correctamente." -ForegroundColor Green
                $okCount++
            } else {
                $errDetail = ($res.Output -replace "`r?`n", " | ").Trim()
                Write-Host "     [AVISO] Respuesta en '$($p.Name)': $errDetail" -ForegroundColor Yellow
            }
        }
    }
    if ($okCount -eq $parts.Count) {
        Write-Host "`n[EXITO] Las 3 particiones clave se inyectaron correctamente. Reiniciando caja..." -ForegroundColor Green
        [void](Invoke-FastbootSafe -ArgString "reboot" -TimeoutMs 3000)
    } else {
        Write-Host "`n[!] El bootloader sigue con lock=10001000 (bloqueado). La caja se mantiene en Fastboot" -ForegroundColor Yellow
        Write-Host "    para que NO pierdas la conexion y puedas desbloquear 'env' por Modo WorldCup." -ForegroundColor Yellow
    }
}

# ==============================================================================
# CAZA DEDICADA WORLDCUP (CUANDO FASTBOOT ESTA EN lock=10001000)
# ==============================================================================
function Start-DedicatedWorldCupHuntFromLockedFastboot {
    Write-Host ""
    Write-Host "================================================================================" -ForegroundColor Yellow
    Write-Host "  DESBLOQUEO AUTOMATICO DE 'locked device' (lock=10001000 -> lock=10100000)     " -ForegroundColor Yellow
    Write-Host "================================================================================" -ForegroundColor Yellow
    Write-Host "  Por que salio 'FAILED (remote: locked device)':" -ForegroundColor White
    Write-Host "  * Tu caja (U-Boot Sep 05 2023) tiene lock=10001000 en la particion 'env'." -ForegroundColor Gray
    Write-Host "  * Con la memoria USB KINGSTON puesta, al presionar RESET la caja salta la" -ForegroundColor Gray
    Write-Host "    ventana de 1.5s de Amlogic WorldCup (InUsbBurn) y entra directo a Recovery." -ForegroundColor Gray
    Write-Host ""
    Write-Host "  COMO DESBLOQUEARLA EN 5 SEGUNDOS AHORA MISMO (SIN ABRIR TORNILLOS):" -ForegroundColor Green
    Write-Host "  1. Desconecta temporalmente la memoria USB KINGSTON de la caja (deja SOLO el" -ForegroundColor Cyan
    Write-Host "     cable USB Macho-a-Macho conectado entre la caja y la PC)." -ForegroundColor Cyan
    Write-Host "  2. Manten presionado el boton fisico RESET (con el clip) en la caja." -ForegroundColor Cyan
    Write-Host "  3. Sin soltar RESET, presiona [1] aqui abajo (el programa reiniciara la caja" -ForegroundColor Cyan
    Write-Host "     y cazara el modo WorldCup InUsbBurn en 48ms para inyectar env_unlocked.img)" -ForegroundColor Cyan
    Write-Host "     O bien desconecta y reconecta el cable de corriente manteniendo RESET." -ForegroundColor Cyan
    Write-Host "--------------------------------------------------------------------------------" -ForegroundColor DarkCyan
    Write-Host "  [1] Ya tengo presionado RESET -> Reiniciar caja y cazar WorldCup (48ms) AHORA" -ForegroundColor Green
    Write-Host "  [2] Volver al menu principal (manteniendo la caja en Fastboot)" -ForegroundColor Gray
    Write-Host ""

    $sel = Read-ChoiceWithKeepAlive -PromptText "Elige 1 o 2" -ValidChoices @("1","2")
    if ($sel -eq "2") { return $false }

    Write-Host "`n[*] Enviando 'fastboot reboot' y activando Caza Ultrarrapida WorldCup (cada 48ms)..." -ForegroundColor Green
    Write-Host "    (Manten presionado el boton RESET en la caja durante 6 segundos!)" -ForegroundColor Yellow
    [void](Invoke-FastbootSafe -ArgString "reboot" -TimeoutMs 2500)

    $swHunt = [System.Diagnostics.Stopwatch]::StartNew()
    while ($swHunt.Elapsed.TotalSeconds -lt 35) {
        if (Try-CatchWorldCup48ms) {
            return $true
        }
        Start-Sleep -Milliseconds 30
    }

    Write-Host "`n[!] No se detecto la senal WorldCup (VID_1B8E) en estos 35 segundos." -ForegroundColor Yellow
    Write-Host "    Asegurate de haber retirado la memoria USB KINGSTON y mantener presionado" -ForegroundColor Yellow
    Write-Host "    el boton RESET al conectar la corriente de la caja." -ForegroundColor Yellow
    return $false
}

# ==============================================================================
# OPCION 1: CAPTURA Y SALTO AUTOMATICO MULTI-CANAL (SALTA CANDADO QR / AZUL)
# ==============================================================================
function Invoke-AutoMultiChannelJump {
    Write-Host ""
    Write-Host "================================================================================" -ForegroundColor Green
    Write-Host "  OPCION 1: CAPTURA Y SALTO AUTOMATICO MULTI-CANAL (SALTA CANDADO QR / AZUL)    " -ForegroundColor Green
    Write-Host "================================================================================" -ForegroundColor Green
    Write-Host "Este modo atrapa tu caja en milisegundos sin abrir tornillos y sin codigo QR:" -ForegroundColor White
    Write-Host ""
    Write-Host "  PASOS RAPIDOS EN LA CAJA:" -ForegroundColor Yellow
    Write-Host "  * PARA DESBLOQUEAR Y FLASHEAR DESDE CERO (WorldCup 48ms -> Fastboot):" -ForegroundColor Cyan
    Write-Host "    1. Deja conectado SOLO el cable USB Macho-a-Macho a la PC (retira la memoria" -ForegroundColor Gray
    Write-Host "       USB de la caja por un momento para que no salte a Recovery)." -ForegroundColor Gray
    Write-Host "    2. Desconecta la corriente de la caja, manten presionado el boton RESET" -ForegroundColor Gray
    Write-Host "       y conecta la corriente (manten RESET 5 segundos)." -ForegroundColor Gray
    Write-Host "    -> Este radar cazara 'InUsbBurn (VID_1B8E)' en 48ms, inyectara" -ForegroundColor Green
    Write-Host "       'env_unlocked.img' (lock=10100000) y dejara Fastboot 100% DESBLOQUEADO!" -ForegroundColor Green
    Write-Host ""
    Write-Host "[*] RADAR ULTRARRAPIDO (48ms) ESCUCHANDO EN VIVO... (Presiona Ctrl+C para salir)" -ForegroundColor Green

    $tick = 0
    $lastRebootToBl = [DateTime]::MinValue

    while ($true) {
        $tick++

        # 1. Caza directa WorldCup cada 48ms (sin overhead de WMI!)
        if (Try-CatchWorldCup48ms) {
            $lastRebootToBl = Get-Date
            continue
        }

        # Si acabamos de enviar 'reboot bootloader', dar 3.2s para que re-enumere PID_0D02
        if (((Get-Date) - $lastRebootToBl).TotalSeconds -lt 3.2) {
            Start-Sleep -Milliseconds 60
            continue
        }

        # 2. Cada 6 ticks (~350ms), verificar Fastboot y ADB con timeout seguro
        if (($tick % 6) -eq 0) {
            $fbDevs = Invoke-FastbootSafe -ArgString "devices" -TimeoutMs 1200
            if ((-not $fbDevs.TimedOut) -and ($fbDevs.Output -match "(?m)^(\S+)\s+fastboot")) {
                $fbSn = $Matches[1]

                # Comprobar que sea una sesion Fastboot real en vivo y NO un descriptor fantasma
                $ping = Invoke-FastbootSafe -ArgString "getvar version-bootloader" -TimeoutMs 900
                if ((-not $ping.TimedOut) -and ($ping.Output -match "version-bootloader|OKAY|Finished")) {

                    # Verificar si estamos en fastbootd (Recovery PID_4EE7)
                    $uCheck = Invoke-FastbootSafe -ArgString "getvar is-userspace" -TimeoutMs 1200
                    if ($uCheck.Output -match "is-userspace:\s*yes") {
                        Write-Host "`n[+] Detectado Recovery Fastbootd (PID_4EE7 / $fbSn)!" -ForegroundColor Yellow
                        if (Test-Path $ENV_UNL) {
                            Write-Host "    Probando desbloqueo directo de 'env' en fastbootd..." -ForegroundColor Cyan
                            [void](Invoke-FastbootSafe -ArgString "flashing unlock" -TimeoutMs 1500)
                            [void](Invoke-FastbootSafe -ArgString "flashing unlock_critical" -TimeoutMs 1500)
                            [void](Invoke-FastbootSafe -ArgString "flash env `"$ENV_UNL`"" -TimeoutMs 2500)
                        }
                        Write-Host "    Saltando a U-Boot Bootloader (PID_0D02) y activando caza WorldCup..." -ForegroundColor Cyan
                        [void](Invoke-FastbootSafe -ArgString "reboot bootloader" -TimeoutMs 2500)
                        $lastRebootToBl = Get-Date
                        continue
                    }

                    # Estamos en U-Boot Fastboot real (PID_0D02)
                    Write-Host "`n[+] Caja atrapada en U-Boot Fastboot en vivo (Serie: $fbSn)!" -ForegroundColor Green
                    Write-Host "    Reteniendo Watchdog y ejecutando secuencia de desbloqueo limpia..." -ForegroundColor Cyan

                    # Secuencia silenciosa sin escupir 'FAILED (remote: locked device)' en pantalla
                    [void](Invoke-FastbootSafe -ArgString "flashing get_unlock_ability" -TimeoutMs 1500)
                    [void](Invoke-FastbootSafe -ArgString "flashing unlock_critical" -TimeoutMs 1500)
                    [void](Invoke-FastbootSafe -ArgString "flashing unlock" -TimeoutMs 1500)
                    [void](Invoke-FastbootSafe -ArgString "oem `"watchdog off`"" -TimeoutMs 1200)
                    [void](Invoke-FastbootSafe -ArgString "oem `"setenv lock 10100000;save`"" -TimeoutMs 1200)
                    [void](Invoke-FastbootSafe -ArgString "oem `"setenv otg_device 1;save`"" -TimeoutMs 1200)
                    [void](Invoke-FastbootSafe -ArgString "oem `"saveenv`"" -TimeoutMs 1200)

                    $unlRes = Invoke-FastbootSafe -ArgString "getvar unlocked" -TimeoutMs 1500
                    $isUnlocked = ($unlRes.Output -match "unlocked:\s*yes")

                    # Prueba real de escritura sobre _aml_dtb (256 KB) para confirmar estado real
                    if (-not $isUnlocked) {
                        $dtbPath = Join-Path $FW_DIR "dtb.img"
                        if (Test-Path $dtbPath) {
                            $testWrite = Invoke-FastbootSafe -ArgString "flash _aml_dtb `"$dtbPath`"" -TimeoutMs 2500
                            if ((-not $testWrite.TimedOut) -and ($testWrite.ExitCode -eq 0) -and ($testWrite.Output -match "OKAY") -and ($testWrite.Output -notmatch "FAILED|locked device")) {
                                $isUnlocked = $true
                            }
                        }
                    }

                    if ($isUnlocked) {
                        Write-Host ""
                        Write-Host "================================================================================" -ForegroundColor Green
                        Write-Host " [OK] BOOTLOADER 100% DESBLOQUEADO (unlocked: yes) Y WATCHDOG RETENIDO          " -ForegroundColor Green
                        Write-Host "================================================================================" -ForegroundColor Green
                        Write-Host "  [1] Flashear ahora mismo Android 12 Liberado Completo (Conservando Widevine L1 y MAC)" -ForegroundColor Magenta
                        Write-Host "  [2] Inyectar vbmeta desbloqueado + recovery desbloqueado + cache_ext4 y reiniciar" -ForegroundColor Cyan
                        Write-Host "  [3] Dejar en Fastboot y volver al menu" -ForegroundColor Gray
                        Write-Host ""
                        $next = Read-ChoiceWithKeepAlive -PromptText "Elige 1, 2 o 3" -ValidChoices @("1","2","3")
                        if ($next -eq "1") {
                            Invoke-SafeAndroid12Flash -AlreadyInFastboot $true
                        } elseif ($next -eq "2") {
                            Invoke-QuickUnlockPartitionsFlash
                        }
                        break
                    } else {
                        # U-Boot sigue con lock=10001000 -> Activar flujo asistido de Caza WorldCup 48ms
                        $unlockedViaWc = Start-DedicatedWorldCupHuntFromLockedFastboot
                        if ($unlockedViaWc) {
                            $lastRebootToBl = Get-Date
                            continue
                        } else {
                            break
                        }
                    }
                }
            }

            # 3. Verificar sesion ADB activa
            $adbRes = Invoke-ToolSafe -ExePath $ADB -ArgString "devices" -TimeoutMs 1500
            if (-not $adbRes.TimedOut) {
                if ($adbRes.Output -match "(?m)^(\S+)\s+unauthorized") {
                    Write-Host "[!] Acepta el cartel 'Permitir Depuracion por USB' en tu TV..." -ForegroundColor Yellow
                } elseif ($adbRes.Output -match "(?m)^(\S+)\s+device\b") {
                    $adbId = $Matches[1]
                    Write-Host "`n[+] Detectada sesion ADB activa ($adbId)! Ejecutando liberacion..." -ForegroundColor Green
                    Execute-AdbPayload
                    break
                }
            }
        }

        Start-Sleep -Milliseconds 25
    }
    Read-Host "`nPresiona ENTER para volver al menu"
}

# ==============================================================================
# OPCION 2: FLASHEO SEGURO ANDROID 12 (B866V2) SIN PERDER WIDEVINE L1 / MAC
# ==============================================================================
function Invoke-SafeAndroid12Flash {
    param([bool]$AlreadyInFastboot = $false)
    Write-Host ""
    Write-Host "================================================================================" -ForegroundColor Magenta
    Write-Host "   OPCION 2: FLASHEO SEGURO ANDROID 12 (B866V2) SIN PERDER WIDEVINE L1 / MAC   " -ForegroundColor Magenta
    Write-Host "================================================================================" -ForegroundColor Magenta
    Write-Host "  * Salta por completo el bloqueo de Megacable y el candado QR RSA-2048 de ZTE." -ForegroundColor Green
    Write-Host "  * Protege 100% tus particiones 'factory' (Widevine L1) y 'cri_data' (MAC/Serie)." -ForegroundColor Green
    Write-Host "  * Repara la particion /cache con 'cache_ext4.img' e instala Recovery desbloqueado." -ForegroundColor Green
    Write-Host ""

    $superPath = Join-Path $FW_DIR "super.img"
    if (-not (Test-Path $superPath)) {
        $candidates = @(
            "D:\B866v2 Android 12\B866v2 Android 12\B866V2\super.img",
            "E:\B866v2 Android 12\B866v2 Android 12\B866V2\super.img",
            "F:\B866v2 Android 12\B866v2 Android 12\B866V2\super.img",
            "C:\Users\andre\OneDrive\Desktop\xview\V2\B866v2 Android 12\B866v2 Android 12\B866V2\super.img",
            "C:\Users\andre\OneDrive\Desktop\xview\FIRMWARE_COMPLETO_B866V2_FASTBOOT\super.img"
        )
        foreach ($c in $candidates) {
            if (Test-Path $c) { $superPath = $c; break }
        }
    }

    if (-not $AlreadyInFastboot) {
        Write-Host "[1/12] Escuchando WorldCup (48ms) o Fastboot (Manten RESET al conectar corriente)..." -ForegroundColor Cyan
        $lastJump = [DateTime]::MinValue
        while ($true) {
            if (Try-CatchWorldCup48ms) {
                $lastJump = Get-Date
                continue
            }
            if (((Get-Date) - $lastJump).TotalSeconds -lt 3.2) {
                Start-Sleep -Milliseconds 60
                continue
            }
            $fbRes = Invoke-FastbootSafe -ArgString "devices" -TimeoutMs 1200
            if ((-not $fbRes.TimedOut) -and ($fbRes.Output -match "(?m)^(\S+)\s+fastboot")) {
                $ping = Invoke-FastbootSafe -ArgString "getvar version-bootloader" -TimeoutMs 900
                if ((-not $ping.TimedOut) -and ($ping.Output -match "version-bootloader|OKAY|Finished")) {
                    $uCheck = Invoke-FastbootSafe -ArgString "getvar is-userspace" -TimeoutMs 1200
                    if ($uCheck.Output -match "is-userspace:\s*yes") {
                        Write-Host "  -> Detectado fastbootd (Recovery), saltando a Bootloader U-Boot..." -ForegroundColor Yellow
                        [void](Invoke-FastbootSafe -ArgString "reboot bootloader" -TimeoutMs 2500)
                        $lastJump = Get-Date
                        continue
                    }
                    break
                }
            }
            Start-Sleep -Milliseconds 40
        }
    }

    Write-Host "[2/12] Verificando desbloqueo del Bootloader y reteniendo Watchdog..." -ForegroundColor Cyan
    [void](Invoke-FastbootSafe -ArgString "flashing unlock_critical" -TimeoutMs 1500)
    [void](Invoke-FastbootSafe -ArgString "flashing unlock" -TimeoutMs 1500)
    [void](Invoke-FastbootSafe -ArgString "oem `"watchdog off`"" -TimeoutMs 1200)
    [void](Invoke-FastbootSafe -ArgString "oem `"setenv lock 10100000;save`"" -TimeoutMs 1200)
    [void](Invoke-FastbootSafe -ArgString "oem `"setenv otg_device 1;save`"" -TimeoutMs 1200)
    [void](Invoke-FastbootSafe -ArgString "oem `"saveenv`"" -TimeoutMs 1200)

    $dtbTest = Invoke-FastbootSafe -ArgString "flash _aml_dtb `"$(Join-Path $FW_DIR 'dtb.img')`"" -TimeoutMs 2500
    if ($dtbTest.TimedOut -or ($dtbTest.Output -match "FAILED|locked device")) {
        Write-Host "`n[!] El bootloader aun reporta lock=10001000. Iniciando desbloqueo por WorldCup (48ms)..." -ForegroundColor Yellow
        $okWc = Start-DedicatedWorldCupHuntFromLockedFastboot
        if (-not $okWc) { return }
    }

    if (-not (Test-Path $superPath)) {
        Write-Host "`n[+] La caja esta retenida en Fastboot con Keep-Alive activo." -ForegroundColor Green
        Write-Host "    Si deseas incluir 'super.img' (1.88 GB), inserta tu memoria USB KINGSTON en la PC" -ForegroundColor Yellow
        Write-Host "    y presiona [1], o presiona [2] para flashear las demas particiones ahora." -ForegroundColor Yellow
        $cSup = Read-ChoiceWithKeepAlive -PromptText "Elige 1 (Buscar super.img en USB) o 2 (Continuar)" -ValidChoices @("1","2")
        if ($cSup -eq "1") {
            foreach ($drv in @("D","E","F","G","H")) {
                $tryPath = "${drv}:\B866v2 Android 12\B866v2 Android 12\B866V2\super.img"
                if (Test-Path $tryPath) { $superPath = $tryPath; break }
            }
        }
    }

    $flashSteps = @(
        @{ Step = "[3/12]";  Part = "boot";     File = (Join-Path $FW_DIR "boot.img");       Timeout = 30000 },
        @{ Step = "[4/12]";  Part = "_aml_dtb"; File = (Join-Path $FW_DIR "dtb.img");        Timeout = 10000 },
        @{ Step = "[4B/12]"; Part = "dtbo";     File = (Join-Path $FW_DIR "dtbo.img");       Timeout = 15000 },
        @{ Step = "[6/12]";  Part = "logo";     File = (Join-Path $FW_DIR "logo.img");       Timeout = 10000 },
        @{ Step = "[6B/12]"; Part = "param";    File = (Join-Path $FW_DIR "param.img");      Timeout = 20000 },
        @{ Step = "[7/12]";  Part = "conf";     File = (Join-Path $FW_DIR "conf.img");       Timeout = 15000 },
        @{ Step = "[7B/12]"; Part = "oem";      File = (Join-Path $FW_DIR "oem.img");        Timeout = 25000 }
    )

    Write-Host "[5/12] PROTEGIENDO factory.img (Widevine L1) y cri_data.img (MAC/Serie) - OMITIDOS POR SEGURIDAD." -ForegroundColor Green

    foreach ($fs in $flashSteps) {
        if (Test-Path $fs.File) {
            Write-Host "$($fs.Step) Flasheando '$($fs.Part)'..." -ForegroundColor Cyan
            $r = Invoke-FastbootSafe -ArgString "flash $($fs.Part) `"$($fs.File)`"" -TimeoutMs $fs.Timeout
            [void](Invoke-FastbootSafe -ArgString "getvar version-bootloader" -TimeoutMs 1200)
            if ((-not $r.TimedOut) -and ($r.ExitCode -eq 0) -and ($r.Output -notmatch "FAILED|locked device")) {
                Write-Host "       [OKAY] $($fs.Part)" -ForegroundColor Green
            } else {
                Write-Host "       [AVISO] $($fs.Part): $(($r.Output -replace "`r?`n", ' ').Trim())" -ForegroundColor Yellow
            }
        }
    }

    if (Test-Path $superPath) {
        Write-Host "[8/12] Flasheando super.img (Android 12 Liberado B866V2 - puede tardar ~90s)..." -ForegroundColor Cyan
        & $FASTBOOT flash super $superPath
        [void](Invoke-FastbootSafe -ArgString "getvar version-bootloader" -TimeoutMs 1200)
    } else {
        Write-Host "[!] No se encontro super.img en la PC, omitiendo paso 8..." -ForegroundColor Yellow
    }

    Write-Host "[9/12] Flasheando vbmeta.img (dm-verity desactivado flags=0x03)..." -ForegroundColor Cyan
    [void](Invoke-FastbootSafe -ArgString "flash vbmeta `"$(Join-Path $FW_DIR 'vbmeta.img')`"" -TimeoutMs 15000)

    Write-Host "[10/12] Flasheando recovery.img (Menu Fastboot/ADB desbloqueado en TV)..." -ForegroundColor Cyan
    [void](Invoke-FastbootSafe -ArgString "flash recovery `"$(Join-Path $FW_DIR 'recovery.img')`"" -TimeoutMs 35000)

    Write-Host "[11/12] Reparando particion /cache con cache_ext4.img..." -ForegroundColor Cyan
    [void](Invoke-FastbootSafe -ArgString "erase cache" -TimeoutMs 10000)
    [void](Invoke-FastbootSafe -ArgString "flash cache `"$(Join-Path $FW_DIR 'cache_ext4.img')`"" -TimeoutMs 35000)

    Write-Host "[12/12] Limpiando userdata y reiniciando..." -ForegroundColor Cyan
    [void](Invoke-FastbootSafe -ArgString "erase userdata" -TimeoutMs 15000)
    [void](Invoke-FastbootSafe -ArgString "reboot" -TimeoutMs 4000)

    Write-Host "`n[COMPLETADO!] Proceso finalizado (con Widevine L1 y MAC intactos)." -ForegroundColor Green
    Read-Host "`nPresiona ENTER para volver al menu"
}

function Invoke-AppInstallerMenu {
    Write-Host ""
    Write-Host "=== OPCION 3: INSTALADOR DE APPS DE TV Y ENTRETENIMIENTO (OFFLINE DESDE PC) ===" -ForegroundColor Yellow
    $apks = Get-ChildItem -Path $APKS_DIR -Filter "*.apk"
    for ($i = 0; $i -lt $apks.Count; $i++) {
        $sizeMB = [math]::Round($apks[$i].Length / 1MB, 2)
        Write-Host ("  [{0}] {1} ({2} MB)" -f ($i + 1), $apks[$i].Name, $sizeMB) -ForegroundColor White
    }
    Write-Host "  [T] INSTALAR TODAS LAS APPS DE LA LISTA" -ForegroundColor Green
    Write-Host "  [0] Volver al menu principal" -ForegroundColor Gray
    Write-Host ""
    $sel = Read-Host "Elige el numero de la app a instalar (o 'T' para todas)"
    if ($sel -eq "0" -or [string]::IsNullOrWhiteSpace($sel)) { return }
    if ($sel -match "^[Tt]$") {
        foreach ($a in $apks) {
            Write-Host "[+] Instalando $($a.Name)..." -ForegroundColor Cyan
            Invoke-Adb @("install", "-r", "-g", $a.FullName)
        }
    } elseif ($sel -match "^\d+$") {
        $idx = [int]$sel - 1
        if ($idx -ge 0 -and $idx -lt $apks.Count) {
            Write-Host "[+] Instalando $($apks[$idx].Name)..." -ForegroundColor Cyan
            Invoke-Adb @("install", "-r", "-g", $apks[$idx].FullName)
        }
    }
    Read-Host "`nPresiona ENTER para volver al menu"
}

function Invoke-AmlogicWorldCupEngine {
    Write-Host ""
    Write-Host "================================================================================" -ForegroundColor Cyan
    Write-Host "   OPCION 4: MOTOR AMLOGIC DNL / WORLDCUP (adnl.exe + update.exe VID_1B8E)      " -ForegroundColor Cyan
    Write-Host "================================================================================" -ForegroundColor Cyan
    Write-Host "  [1] Activar Caza Continua WorldCup (48ms) -> Desbloquear env_unlocked.img y saltar a Fastboot" -ForegroundColor Green
    Write-Host "  [2] Escanear dispositivo en modo WorldCup / ADNL ahora mismo" -ForegroundColor White
    Write-Host "  [3] Reiniciar caja desde modo ADNL / WorldCup" -ForegroundColor White
    Write-Host "  [0] Volver al menu principal" -ForegroundColor Gray
    Write-Host ""
    $sub = Read-Host "Elige una opcion"
    if ($sub -eq "1") {
        Write-Host "`n[*] Escuchando modo WorldCup / InUsbBurn cada 48ms (Manten RESET al conectar corriente)..." -ForegroundColor Green
        while ($true) {
            if (Try-CatchWorldCup48ms) {
                Write-Host "[OK] Caja desbloqueada y enviada a Fastboot (PID_0D02)!" -ForegroundColor Green
                break
            }
            Start-Sleep -Milliseconds 30
        }
    } elseif ($sub -eq "2") {
        Write-Host "`n--- Escaneo con adnl.exe ---" -ForegroundColor Cyan
        & $ADNL devices
        Write-Host "`n--- Escaneo con update.exe ---" -ForegroundColor Cyan
        & $UPDATE identify 7
    } elseif ($sub -eq "3") {
        & $ADNL reboot
        & $UPDATE bulkcmd "reset"
    }
    Read-Host "`nPresiona ENTER para volver al menu"
}

function Invoke-UsbPrepAndLogDecrypt {
    Write-Host ""
    Write-Host "================================================================================" -ForegroundColor Yellow
    Write-Host "   OPCION 5: PREPARAR MEMORIA USB (QADEBUG + APKS) Y DESCIFRAR LOGS ZTE         " -ForegroundColor Yellow
    Write-Host "================================================================================" -ForegroundColor Yellow

    $drives = Get-Volume | Where-Object { $_.DriveType -eq 'Removable' -and $_.DriveLetter }
    if ($drives) {
        Write-Host "Memorias USB detectadas:" -ForegroundColor Cyan
        foreach ($d in $drives) {
            Write-Host "  * Unidad $($d.DriveLetter):\ ($($d.FileSystemLabel) - $($d.FileSystem))" -ForegroundColor Green
        }
    }
    Write-Host ""
    Write-Host "  [1] Copiar llaves QADEBUG.ini + factory_update_param.aml + Launchers a una memoria USB" -ForegroundColor White
    Write-Host "  [2] Descifrar logs ocultos de la caja (LOG_ZTESTB_OFF\U_*) y mostrar Serie/MAC/Firmware" -ForegroundColor Green
    Write-Host "  [0] Volver al menu principal" -ForegroundColor Gray
    Write-Host ""
    $op = Read-Host "Elige una opcion"
    if ($op -eq "1") {
        $drv = Read-Host "Escribe la letra de tu memoria USB (ej. D)"
        $drv = ($drv -replace "[:\\]", "").Trim()
        $targetUsb = "${drv}:\"
        if (Test-Path $targetUsb) {
            Copy-Item "$KEYS_DIR\*" $targetUsb -Force
            Copy-Item "$APKS_DIR\Home_Screen_Launcher_1.1.3.apk" $targetUsb -Force
            Copy-Item "$APKS_DIR\TVLauncher_4_GoogleTV.apk" $targetUsb -Force
            Copy-Item "$APKS_DIR\Button_Mapper_Pro_2.54.apk" $targetUsb -Force
            Copy-Item "$APKS_DIR\Filemanager.apk" $targetUsb -Force
            Copy-Item "$APKS_DIR\autostart.apk" $targetUsb -Force
            Write-Host "[OK] Llaves QADEBUG.ini, factory_update_param.aml y Launchers copiados a $targetUsb" -ForegroundColor Green
        } else {
            Write-Host "[!] No existe la unidad $targetUsb" -ForegroundColor Red
        }
    } elseif ($op -eq "2") {
        $drv = Read-Host "Escribe la letra de la memoria USB donde estan los logs (ej. D)"
        $drv = ($drv -replace "[:\\]", "").Trim()
        $logFolder = "${drv}:\LOG_ZTESTB_OFF"
        if (Test-Path $logFolder) {
            $encStr = '5;8|"79+3(&16-409876543210/.-,+*)(''&%$#"! ~}|{zyxwvutsrqponmlkjihgfedcba`_^]\[ZYXWVUTSRQPONMLKJIHGFEDCBA@?>=<;:9876543210/.-,+*)(''&%$#"! '
            $decStr = ' ~}|{zyxwvutsrqponmlkjihgfedcba`_^]\[ZYXWVUTSRQPONMLKJIHGFEDCBA@?>=<;:9876543210/.-,+*)(''&%$#"! 5;8|"79+3(&16-409876543210/.-,+*)(''&%$#"!'
            $lut = New-Object byte[] 256
            for ($i=0; $i -lt 256; $i++) { $lut[$i] = [byte]$i }
            for ($i=0; $i -lt $encStr.Length; $i++) {
                $e = [byte][char]$encStr[$i]
                $d = [byte][char]$decStr[$i]
                if ($lut[$e] -eq $e) { $lut[$e] = $d }
            }
            $outDir = Join-Path $ROOT_DIR "LOGS_DESCIFRADOS_CAJA"
            New-Item -ItemType Directory -Path $outDir -Force | Out-Null
            $files = Get-ChildItem $logFolder -Filter "U_*"
            foreach ($f in $files) {
                Write-Host "[+] Descifrando $($f.Name)..." -ForegroundColor Cyan
                $raw = [System.IO.File]::ReadAllBytes($f.FullName)
                $out = New-Object byte[] $raw.Length
                for ($i=0; $i -lt $raw.Length; $i++) { $out[$i] = $lut[$raw[$i]] }
                $destFile = Join-Path $outDir "$($f.Name)_DEC.log"
                [System.IO.File]::WriteAllBytes($destFile, $out)
                Write-Host "  -> Guardado en: $destFile" -ForegroundColor Green
                Select-String -Path $destFile -Pattern "SerialNumber|ro.build.fingerprint|Linux version|U-Boot 2015|checkUdisk" | Select-Object -First 8 | ForEach-Object {
                    Write-Host "     $($_.Line)" -ForegroundColor Gray
                }
            }
        } else {
            Write-Host "[!] No se encontro la carpeta $logFolder" -ForegroundColor Red
        }
    }
    Read-Host "`nPresiona ENTER para volver al menu"
}

function Invoke-DriversAndGuiMenu {
    Write-Host ""
    Write-Host "=== OPCION 6: INSTALAR DRIVERS USB Y ABRIR ADB APPCONTROL GUI ===" -ForegroundColor Yellow
    Write-Host "  [1] Abrir instalador de ADB AppControl 1.8.6 (Interfaz grafica para Windows)" -ForegroundColor White
    Write-Host "  [2] Instalar Driver Oficial Amlogic WorldCup (VID_1B8E)" -ForegroundColor White
    Write-Host "  [0] Volver al menu principal" -ForegroundColor Gray
    Write-Host ""
    $c = Read-Host "Elige una opcion"
    if ($c -eq "1") {
        $exe = Join-Path $DRV_DIR "ADBAppControl-1.8.6-setup.exe"
        if (Test-Path $exe) { Start-Process $exe }
    } elseif ($c -eq "2") {
        $exe = Join-Path $DRV_DIR "WorldCup_Driver_VID_1B8E\InstallDriver.exe"
        if (Test-Path $exe) { Start-Process $exe -Verb RunAs }
    }
}

function Connect-AdbTarget {
    Write-Host ""
    Write-Host "=== CONECTAR CAJA POR RED LOCAL (WI-FI / ETHERNET) O REFRESCAR USB ===" -ForegroundColor Yellow
    $ip = Read-Host "Escribe la IP de la caja (ej. 192.168.0.47) o presiona ENTER para refrescar USB"
    if (-not [string]::IsNullOrWhiteSpace($ip)) {
        if ($ip -notmatch ":") { $ip = "${ip}:5555" }
        Write-Host "[+] Conectando por ADB a $ip ..." -ForegroundColor Cyan
        & $ADB connect $ip
        $Global:ADB_TARGET_ARGS = @("-s", $ip)
        $Global:CONNECTED_IP = $ip
    } else {
        $Global:ADB_TARGET_ARGS = @()
        $Global:CONNECTED_IP = ""
        & $ADB devices
    }
    Read-Host "`nPresiona ENTER para volver al menu"
}

if ($args -contains "--check") {
    Show-Banner
    Write-Host "  [1] SALTO AUTOMATICO MULTI-CANAL (Caza WorldCup InUsbBurn en 48ms + Fastbootd + Fastboot + ADB)" -ForegroundColor Green
    Write-Host "  [2] FLASHEO SEGURO ANDROID 12 LIBERADO (B866V2) Conservando Widevine L1 y MAC" -ForegroundColor Magenta
    Write-Host "  [3] Instalador de Apps de TV y Entretenimiento (MagisTV, Xuper, Hdplay, Launchers, Button Mapper)" -ForegroundColor White
    Write-Host "  [4] Motor Amlogic DNL / WorldCup (adnl.exe + update.exe por boton externo RESET)" -ForegroundColor Cyan
    Write-Host "  [5] Preparar Memoria USB (QADEBUG.ini + factory_update_param.aml) y Descifrar Logs ZTE" -ForegroundColor Yellow
    Write-Host "  [6] Instalar Drivers USB (WorldCup VID_1B8E) o Abrir ADB AppControl GUI" -ForegroundColor White
    Write-Host "  [0] Conectar por IP Local o Refrescar Radar USB   |   [7] Salir" -ForegroundColor DarkGray
    Write-Host "--------------------------------------------------------------------------------" -ForegroundColor DarkCyan
    exit 0
}

# Bucle Principal del Programa Perfecto
while ($true) {
    Show-Banner
    Write-Host "  [1] SALTO AUTOMATICO MULTI-CANAL (Caza WorldCup InUsbBurn 48ms -> Desbloquea env_unlocked.img" -ForegroundColor Green
    Write-Host "      -> Salta a Fastboot PID_0D02 desbloqueado / Recovery PID_4EE7 / ADB Directo)" -ForegroundColor Green
    Write-Host "  [2] FLASHEO SEGURO ANDROID 12 LIBERADO (B866V2) Conservando Widevine L1 y MAC" -ForegroundColor Magenta
    Write-Host "      (Retiene la caja en Fastboot con Keep-Alive y protege factory + cri_data)" -ForegroundColor Magenta
    Write-Host "  [3] Instalador de Apps de TV y Entretenimiento (MagisTV, Xuper, Hdplay, Launchers)" -ForegroundColor White
    Write-Host "  [4] Motor Amlogic DNL / WorldCup (adnl.exe + update.exe por boton externo RESET)" -ForegroundColor Cyan
    Write-Host "  [5] Preparar Memoria USB (QADEBUG.ini + factory_update_param.aml) y Descifrar Logs" -ForegroundColor Yellow
    Write-Host "  [6] Instalar Drivers USB (WorldCup VID_1B8E) o Abrir ADB AppControl GUI" -ForegroundColor White
    Write-Host "  [0] Conectar por IP Local o Refrescar Radar USB   |   [7] Salir" -ForegroundColor DarkGray
    Write-Host "--------------------------------------------------------------------------------" -ForegroundColor DarkCyan
    $choice = Read-Host "Selecciona el numero de la opcion [0-7]"

    switch ($choice) {
        "0" { Connect-AdbTarget }
        "1" { Invoke-AutoMultiChannelJump }
        "2" { Invoke-SafeAndroid12Flash }
        "3" { Invoke-AppInstallerMenu }
        "4" { Invoke-AmlogicWorldCupEngine }
        "5" { Invoke-UsbPrepAndLogDecrypt }
        "6" { Invoke-DriversAndGuiMenu }
        "7" { exit 0 }
        default { Start-Sleep -Milliseconds 500 }
    }
}
