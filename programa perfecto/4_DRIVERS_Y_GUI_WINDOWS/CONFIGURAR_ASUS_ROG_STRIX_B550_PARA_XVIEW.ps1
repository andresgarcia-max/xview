# ==============================================================================
# CONFIGURADOR ESPECIAL PARA TARJETA MADRE:
# ASUS ROG STRIX B550-A GAMING (Chipset AMD B550 / Procesador AMD Ryzen AM4)
#
# Prepara y corrige el controlador AMD USB 3.10 eXtensible Host Controller (xHCI),
# instala los 5 certificados autofirmados, los 4 paquetes de drivers de la Xview+
# (incluyendo VID_1B8E&PID_C004 ADNL y PID_C003 WorldCup), aplica el parche
# SkipBOSDescriptorQuery para AMD B550, apaga el ahorro de energía PCIe/USB y
# limpia dispositivos USB fallidos en caché.
# ==============================================================================

$ErrorActionPreference = "Continue"
$BaseDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "==============================================================================" -ForegroundColor Magenta
Write-Host " CONFIGURACION ESPECIAL PARA ASUS ROG STRIX B550-A GAMING (AMD B550 + XVIEW+)" -ForegroundColor Cyan
Write-Host " Parcheando AMD xHCI + Certificados + Drivers ADNL/WorldCup/Fastboot" -ForegroundColor Cyan
Write-Host "==============================================================================" -ForegroundColor Magenta
Write-Host ""

# Verificar privilegios de Administrador
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "[!] Elevando permisos a Administrador..." -ForegroundColor Yellow
    Start-Process powershell.exe -ArgumentList "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`"" -Verb RunAs
    exit
}

# Detectar placa base actual
$board = Get-CimInstance Win32_BaseBoard -ErrorAction SilentlyContinue
$cpu = Get-CimInstance Win32_Processor -ErrorAction SilentlyContinue
Write-Host "[INFO] Placa Base detectada : $($board.Manufacturer) - $($board.Product) (Rev: $($board.Version))" -ForegroundColor White
Write-Host "[INFO] Procesador detectado : $($cpu.Name)" -ForegroundColor White
Write-Host ""

# PASO 1: Limpiar dispositivos USB fallidos o fantasma en caché (Unknown USB Device / Descriptor Failure)
Write-Host "[1/7] Limpiando enumeraciones USB fallidas en caché de la placa B550..." -ForegroundColor Green
$badDevices = Get-PnpDevice -ErrorAction SilentlyContinue | Where-Object {
    $_.InstanceId -match 'VID_0000&PID_0002|VID_0000&PID_0004|VID_0000&PID_0000' -or
    ($_.InstanceId -match 'VID_1B8E|VID_18D1' -and $_.Status -ne 'OK')
}
$removedCount = 0
foreach ($dev in $badDevices) {
    pnputil.exe /remove-device "$($dev.InstanceId)" 2>&1 | Out-Null
    $removedCount++
}
Write-Host "  -> Nodos USB en conflicto o fantasma limpiados: $removedCount"

# PASO 2: Instalar los 5 Certificados Digitales en LocalMachine\Root y TrustedPublisher
Write-Host ""
Write-Host "[2/7] Instalando los 5 Certificados Digitales (libwdi, libusb-win32, Google, UniversalADB)..." -ForegroundColor Green
$certFolder = Join-Path $BaseDir "1_CERTIFICADOS_FIRMA_DIGITAL"
if (Test-Path $certFolder) {
    Get-ChildItem -Path $certFolder -Filter "*.cer" | ForEach-Object {
        Write-Host "  -> Importando certificado: $($_.Name)"
        certutil.exe -addstore -f "Root" $_.FullName | Out-Null
        certutil.exe -addstore -f "TrustedPublisher" $_.FullName | Out-Null
    }
}

# PASO 3: Copiar binarios de bajo nivel (libusb0.sys, libusb0.dll, libusbK.dll, winusb.dll)
Write-Host ""
Write-Host "[3/7] Instalando binarios de kernel y librerías USB en System32 y SysWOW64..." -ForegroundColor Green
$libDir = Join-Path $BaseDir "7_LIBRERIAS_SISTEMA_SYS_Y_DLL"
if (Test-Path "$libDir\System32_drivers\libusb0.sys") {
    Copy-Item "$libDir\System32_drivers\libusb0.sys" "$env:SystemRoot\System32\drivers\libusb0.sys" -Force -ErrorAction SilentlyContinue
    Write-Host "  -> Instalado: C:\Windows\System32\drivers\libusb0.sys"
}
if (Test-Path "$libDir\System32") {
    Get-ChildItem "$libDir\System32\*.dll" | ForEach-Object {
        Copy-Item $_.FullName "$env:SystemRoot\System32\$($_.Name)" -Force -ErrorAction SilentlyContinue
        Write-Host "  -> Copiado a System32: $($_.Name)"
    }
}
if (Test-Path "$libDir\SysWOW64") {
    Get-ChildItem "$libDir\SysWOW64\*.dll" | ForEach-Object {
        Copy-Item $_.FullName "$env:SystemRoot\SysWOW64\$($_.Name)" -Force -ErrorAction SilentlyContinue
        Write-Host "  -> Copiado a SysWOW64: $($_.Name)"
    }
}

# PASO 4: Instalar todos los drivers INF (ADNL PID_C004, WorldCup PID_C003, Fastboot PID_0D02 / PID_4EE7)
Write-Host ""
Write-Host "[4/7] Instalando los 4 paquetes de drivers para el puerto Macho-a-Macho..." -ForegroundColor Green
$driverFolders = @(
    "2_DRIVER_AMLOGIC_ADNL_VID_1B8E_PID_C004",
    "3_DRIVER_WORLDCUP_VID_1B8E_PID_C003",
    "4_DRIVER_ANDROID_FASTBOOT_ADB_VID_18D1_v1",
    "5_DRIVER_ANDROID_FASTBOOT_ADB_VID_18D1_v2"
)
foreach ($folder in $driverFolders) {
    $fullFolder = Join-Path $BaseDir $folder
    if (Test-Path $fullFolder) {
        Get-ChildItem -Path $fullFolder -Filter "*.inf" | ForEach-Object {
            Write-Host "  -> Registrando e instalando: $folder\$($_.Name)" -ForegroundColor Cyan
            pnputil.exe /add-driver $_.FullName /install
        }
    }
}

$dpinst = Join-Path $BaseDir "3_DRIVER_WORLDCUP_VID_1B8E_PID_C003\dpinst64.exe"
if (Test-Path $dpinst) {
    Write-Host "  -> Ejecutando dpinst64.exe para WorldCup Device (VID_1B8E&PID_C003)..."
    Start-Process -FilePath $dpinst -ArgumentList "/S /SE /SW" -Wait -NoNewWindow -ErrorAction SilentlyContinue
}

# PASO 5: Aplicar Parche de Registro Especial para AMD B550 xHCI (SkipBOSDescriptorQuery + SkipContainerIdQuery + osvc)
Write-Host ""
Write-Host "[5/7] Aplicando parche de registro especial para AMD B550 xHCI (SkipBOSDescriptorQuery)..." -ForegroundColor Green
$regB550 = Join-Path $BaseDir "8_REGISTRO_USBFLAGS_Y_ENERGIA_USB\FIX_REGISTRO_AMD_B550_XHCI_Y_USBFLAGS_XVIEW.reg"
if (Test-Path $regB550) {
    reg.exe import $regB550
    Write-Host "  -> Parche SkipBOSDescriptorQuery + SkipContainerIdQuery + IgnoreHWSerNum aplicado." -ForegroundColor Cyan
}

# PASO 6: Desactivar ahorro de energía en TODAS las controladoras USB AMD (VEN_1022) y Concentradores Raíz USB 3.0 / 2.0
Write-Host ""
Write-Host "[6/7] Blindando controladoras AMD USB 3.10 xHCI y hubs contra cortes de energía (U1/U2/D3)..." -ForegroundColor Green

# 6.1 Desactivar ahorro de energía en la clase USB {36fc9e60-c465-11cf-8056-444553540000}
$usbClassPath = 'HKLM:\SYSTEM\CurrentControlSet\Control\Class\{36fc9e60-c465-11cf-8056-444553540000}'
if (Test-Path $usbClassPath) {
    Get-ChildItem $usbClassPath -ErrorAction SilentlyContinue | ForEach-Object {
        $keyPath = $_.PSPath
        $p = Get-ItemProperty $keyPath -ErrorAction SilentlyContinue
        if ($p.DriverDesc -match 'USB|Host|Hub|Concentrador|AMD|Controlador') {
            Set-ItemProperty -Path $keyPath -Name "EnhancedPowerManagementEnabled" -Value 0 -Type DWord -ErrorAction SilentlyContinue
            Set-ItemProperty -Path $keyPath -Name "AllowIdleIrpInD3" -Value 0 -Type DWord -ErrorAction SilentlyContinue
            Set-ItemProperty -Path $keyPath -Name "SelectiveSuspendEnabled" -Value 0 -Type Binary -Value ([byte[]](0)) -ErrorAction SilentlyContinue
            Set-ItemProperty -Path $keyPath -Name "SelectiveSuspendOn" -Value 0 -Type DWord -ErrorAction SilentlyContinue
            Set-ItemProperty -Path $keyPath -Name "WaitWakeEnabled" -Value 1 -Type DWord -ErrorAction SilentlyContinue
        }
    }
}

# 6.2 Desactivar EnhancedPowerManagementEnabled en todos los USB\ROOT_HUB30 y PCI\VEN_1022 (AMD USB xHCI)
Get-ChildItem 'HKLM:\SYSTEM\CurrentControlSet\Enum\USB\ROOT_HUB*' -ErrorAction SilentlyContinue | ForEach-Object {
    Get-ChildItem $_.PSPath -ErrorAction SilentlyContinue | ForEach-Object {
        $wdfPath = Join-Path $_.PSPath "Device Parameters\WDF"
        $devParamPath = Join-Path $_.PSPath "Device Parameters"
        if (Test-Path $wdfPath) {
            Set-ItemProperty -Path $wdfPath -Name "IdleInWorkingState" -Value 0 -Type DWord -ErrorAction SilentlyContinue
        }
        if (Test-Path $devParamPath) {
            Set-ItemProperty -Path $devParamPath -Name "EnhancedPowerManagementEnabled" -Value 0 -Type DWord -ErrorAction SilentlyContinue
            Set-ItemProperty -Path $devParamPath -Name "SelectiveSuspendEnabled" -Value 0 -Type DWord -ErrorAction SilentlyContinue
        }
    }
}

Get-ChildItem 'HKLM:\SYSTEM\CurrentControlSet\Enum\PCI' -ErrorAction SilentlyContinue | Where-Object { $_.PSChildName -match 'VEN_1022|VEN_8086' } | ForEach-Object {
    Get-ChildItem $_.PSPath -ErrorAction SilentlyContinue | ForEach-Object {
        $devParamPath = Join-Path $_.PSPath "Device Parameters"
        $wdfPath = Join-Path $_.PSPath "Device Parameters\WDF"
        if (Test-Path $wdfPath) {
            Set-ItemProperty -Path $wdfPath -Name "IdleInWorkingState" -Value 0 -Type DWord -ErrorAction SilentlyContinue
        }
        if (Test-Path $devParamPath) {
            Set-ItemProperty -Path $devParamPath -Name "EnhancedPowerManagementEnabled" -Value 0 -Type DWord -ErrorAction SilentlyContinue
        }
    }
}

# 6.3 Desactivar Suspensión Selectiva USB y PCI Express Link State Power Management (ASPM) en el plan de energía
# (En placas B550, ASPM corta micro-segundos el enlace entre el CPU Ryzen y el chipset B550 causando caída de USB)
powercfg.exe /SETACVALUEINDEX SCHEME_CURRENT 2a737441-1930-4402-8d77-b2bebba308a3 48e6b7a6-50f5-4782-a5d4-53bb8f07e226 0 2>$null
powercfg.exe /SETDCVALUEINDEX SCHEME_CURRENT 2a737441-1930-4402-8d77-b2bebba308a3 48e6b7a6-50f5-4782-a5d4-53bb8f07e226 0 2>$null
# PCI Express -> Link State Power Management = 0 (Off)
powercfg.exe /SETACVALUEINDEX SCHEME_CURRENT 501a4d13-42af-4429-9fd1-a8218c268e20 ee12f906-d277-404b-b6da-e5fa1a576df5 0 2>$null
powercfg.exe /SETDCVALUEINDEX SCHEME_CURRENT 501a4d13-42af-4429-9fd1-a8218c268e20 ee12f906-d277-404b-b6da-e5fa1a576df5 0 2>$null
powercfg.exe /SETACTIVE SCHEME_CURRENT 2>$null
Write-Host "  -> Suspensión Selectiva USB = DESACTIVADA y PCIe Link State Power Management (ASPM) = DESACTIVADO."

# Configurar ~/.android/adb_usb.ini
$androidDir = Join-Path $env:USERPROFILE ".android"
if (-not (Test-Path $androidDir)) { New-Item -ItemType Directory -Path $androidDir -Force | Out-Null }
$adbUsbIni = Join-Path $androidDir "adb_usb.ini"
@("0x18D1", "0x1B8E", "0x19D2") | Set-Content -Path $adbUsbIni -Encoding ASCII -Force

# PASO 7: Verificar y desactivar Integridad de Memoria (HVCI) para que Windows permita cargar libusb0.sys
Write-Host ""
Write-Host "[7/7] Verificando Integridad de Memoria (HVCI) y re-escaneando puertos USB de la B550..." -ForegroundColor Green
$hvciPath = 'HKLM:\SYSTEM\CurrentControlSet\Control\DeviceGuard\Scenarios\HypervisorEnforcedCodeIntegrity'
$needReboot = $false
if (Test-Path $hvciPath) {
    $hvci = Get-ItemProperty $hvciPath -ErrorAction SilentlyContinue
    if ($hvci.Enabled -eq 1) {
        Set-ItemProperty -Path $hvciPath -Name 'Enabled' -Value 0 -Type DWord -ErrorAction SilentlyContinue
        $needReboot = $true
        Write-Host "  [!] Se desactivó 'Integridad de Memoria (HVCI)' (estaba bloqueando libusb0.sys)." -ForegroundColor Yellow
    }
}

pnputil.exe /scan-devices | Out-Null

Write-Host ""
Write-Host "==============================================================================" -ForegroundColor Green
Write-Host " [LISTO] CONFIGURACION ESPECIAL ASUS ROG STRIX B550-A GAMING APLICADA CON EXITO" -ForegroundColor Green
Write-Host "==============================================================================" -ForegroundColor Green
Write-Host ""
Write-Host " >>> EN QUE PUERTO FISICO DE TU ASUS ROG STRIX B550-A GAMING CONECTAR EL CABLE <<<" -ForegroundColor Yellow
Write-Host "  1. En la parte trasera superior de tu placa hay 2 puertos USB 2.0 NEGROS." -ForegroundColor White
Write-Host "  2. Uno tiene el recuadro 'BIOS' (BIOS FlashBack) y el de al lado es USB 2.0 normal." -ForegroundColor White
Write-Host "  3. Conecta el cable Macho-a-Macho en el PUERTO USB 2.0 NEGRO QUE NO DICE 'BIOS'" -ForegroundColor Cyan
Write-Host "     (o en un puerto USB 2.0 del panel frontal del gabinete)." -ForegroundColor Cyan
Write-Host "  4. NO uses el puerto rojo (10 Gbps) ni los azules superiores (van directo al CPU Ryzen)." -ForegroundColor White
if ($needReboot) {
    Write-Host ""
    Write-Host "  [IMPORTANTE] Reinicia tu PC una vez para que surta efecto el cambio de HVCI y usbflags." -ForegroundColor Red
}
Write-Host "==============================================================================" -ForegroundColor Green
Write-Host ""
Pause
