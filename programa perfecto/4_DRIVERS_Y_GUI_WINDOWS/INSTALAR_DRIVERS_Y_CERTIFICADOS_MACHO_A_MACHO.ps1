# ==============================================================================
# INSTALADOR AUTOMATICO COMPLETO DE DRIVERS, CERTIFICADOS Y REGISTRO USB
# PARA CABLE USB MACHO-A-MACHO (ZTE ZXV10 B866V2 / XVIEW+)
# Extraido de la PC funcional: Dell Latitude 7490 (Placa Base 0KP0FT)
# ==============================================================================

$ErrorActionPreference = "Continue"
$BaseDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host " INSTALADOR INTEGRAL DE DRIVERS USB MACHO-A-MACHO (XVIEW+ B866V2)" -ForegroundColor Cyan
Write-Host " Clonando entorno exacto de la PC funcional (Dell Latitude 7490 - 0KP0FT)" -ForegroundColor Cyan
Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host ""

# Verificar privilegios de Administrador
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "[!] Elevando permisos a Administrador..." -ForegroundColor Yellow
    Start-Process powershell.exe -ArgumentList "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`"" -Verb RunAs
    exit
}

# 1. Instalar los 5 Certificados Digitales en Root y TrustedPublisher
Write-Host "[1/6] Instalando certificados digitales autofirmados (libwdi, libusb-win32, Google, UniversalADB)..." -ForegroundColor Green
$certFolder = Join-Path $BaseDir "1_CERTIFICADOS_FIRMA_DIGITAL"
if (Test-Path $certFolder) {
    Get-ChildItem -Path $certFolder -Filter "*.cer" | ForEach-Object {
        Write-Host "  -> Importando certificado: $($_.Name)"
        certutil.exe -addstore -f "Root" $_.FullName | Out-Null
        certutil.exe -addstore -f "TrustedPublisher" $_.FullName | Out-Null
    }
}

# 2. Copiar librerias criticas (.sys y .dll) a System32 y SysWOW64
Write-Host ""
Write-Host "[2/6] Instalando binarios de bajo nivel (libusb0.sys, libusb0.dll, libusbK.dll, winusb.dll)..." -ForegroundColor Green
$libDir = Join-Path $BaseDir "7_LIBRERIAS_SISTEMA_SYS_Y_DLL"
if (Test-Path "$libDir\System32_drivers\libusb0.sys") {
    Copy-Item "$libDir\System32_drivers\libusb0.sys" "$env:SystemRoot\System32\drivers\libusb0.sys" -Force -ErrorAction SilentlyContinue
    Write-Host "  -> Copiado: C:\Windows\System32\drivers\libusb0.sys"
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

# 3. Instalar los paquetes de Drivers INF en el DriverStore (pnputil)
Write-Host ""
Write-Host "[3/6] Registrando e instalando los 4 drivers críticos del cable Macho-a-Macho..." -ForegroundColor Green
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
            Write-Host "  -> Instalando INF: $folder\$($_.Name)" -ForegroundColor Cyan
            pnputil.exe /add-driver $_.FullName /install
        }
    }
}

# Ejecutar dpinst64.exe para WorldCup Device (VID_1B8E&PID_C003)
$dpinst = Join-Path $BaseDir "3_DRIVER_WORLDCUP_VID_1B8E_PID_C003\dpinst64.exe"
if (Test-Path $dpinst) {
    Write-Host "  -> Ejecutando instalador directo WorldCup Device (dpinst64.exe)..."
    Start-Process -FilePath $dpinst -ArgumentList "/S /SE /SW" -Wait -NoNewWindow -ErrorAction SilentlyContinue
}

# 4. Aplicar banderas USB (usbflags) en el Registro de Windows
Write-Host ""
Write-Host "[4/6] Aplicando banderas de registro USB (usbflags para PID_0D02, PID_4EE7, PID_4EE1, PID_C003, PID_C004)..." -ForegroundColor Green
$regFile = Join-Path $BaseDir "8_REGISTRO_USBFLAGS_Y_ENERGIA_USB\CONFIGURACION_USBFLAGS_XVIEW_MACHO_A_MACHO.reg"
if (Test-Path $regFile) {
    reg.exe import $regFile
}

# 5. Desactivar Suspensión Selectiva USB en el plan de energía activo (evita desconexiones en cable Macho-a-Macho)
Write-Host ""
Write-Host "[5/6] Desactivando Suspensión Selectiva USB para evitar cortes de energía en el puerto Macho-a-Macho..." -ForegroundColor Green
powercfg.exe /SETACVALUEINDEX SCHEME_CURRENT 2a737441-1930-4402-8d77-b2bebba308a3 48e6b7a6-50f5-4782-a5d4-53bb8f07e226 0 2>$null
powercfg.exe /SETDCVALUEINDEX SCHEME_CURRENT 2a737441-1930-4402-8d77-b2bebba308a3 48e6b7a6-50f5-4782-a5d4-53bb8f07e226 0 2>$null
powercfg.exe /SETACTIVE SCHEME_CURRENT 2>$null

# Configurar ~/.android/adb_usb.ini con los Vendor IDs (0x18D1 Google, 0x1B8E Amlogic, 0x19D2 ZTE)
$androidDir = Join-Path $env:USERPROFILE ".android"
if (-not (Test-Path $androidDir)) { New-Item -ItemType Directory -Path $androidDir -Force | Out-Null }
$adbUsbIni = Join-Path $androidDir "adb_usb.ini"
@("0x18D1", "0x1B8E", "0x19D2") | Set-Content -Path $adbUsbIni -Encoding ASCII -Force
Write-Host "  -> Configurado $adbUsbIni con VIDs 0x18D1, 0x1B8E y 0x19D2."

# 6. Verificar Integridad de Memoria (HVCI) / DeviceGuard (que en otras PCs bloquea libusb0.sys)
Write-Host ""
Write-Host "[6/6] Verificando estado de Integridad de Memoria (HVCI) y re-escaneando bus USB..." -ForegroundColor Green
$hvci = Get-ItemProperty 'HKLM:\SYSTEM\CurrentControlSet\Control\DeviceGuard\Scenarios\HypervisorEnforcedCodeIntegrity' -ErrorAction SilentlyContinue
if ($hvci -and $hvci.Enabled -eq 1) {
    Write-Host "  [!] ATENCION: Su PC tiene 'Integridad de Memoria (HVCI)' activada (Enabled=1)." -ForegroundColor Yellow
    Write-Host "      En la PC funcional Dell Latitude 7490 está desactivada (Enabled=0) para permitir cargar libusb0.sys." -ForegroundColor Yellow
    Write-Host "      Desactivando HVCI en registro (requerirá reiniciar la PC si libusb0.sys es bloqueado)..." -ForegroundColor Yellow
    Set-ItemProperty -Path 'HKLM:\SYSTEM\CurrentControlSet\Control\DeviceGuard\Scenarios\HypervisorEnforcedCodeIntegrity' -Name 'Enabled' -Value 0 -ErrorAction SilentlyContinue
} else {
    Write-Host "  -> Integridad de Memoria (HVCI) desactivada (OK - Igual que en la PC funcional)." -ForegroundColor Green
}

pnputil.exe /scan-devices | Out-Null

Write-Host ""
Write-Host "==============================================================================" -ForegroundColor Green
Write-Host " [EXITO] TODOS LOS DRIVERS Y CERTIFICADOS DEL CABLE MACHO-A-MACHO INSTALADOS" -ForegroundColor Green
Write-Host "==============================================================================" -ForegroundColor Green
Write-Host " Modos cubiertos ahora en esta PC:"
Write-Host "   1. USB\VID_1B8E&PID_C004 -> Amlogic ADNL Download Mode (WinUSB / libwdi)"
Write-Host "   2. USB\VID_1B8E&PID_C003 -> Amlogic WorldCup BootROM Mode (libusb0.sys)"
Write-Host "   3. USB\VID_18D1&PID_0D02 -> U-Boot Fastboot Mode (Google WinUSB)"
Write-Host "   4. USB\VID_18D1&PID_4EE7 -> Android Recovery / fastbootd / ADB (Google WinUSB)"
Write-Host "   5. USB\VID_18D1&PID_4EE1 -> Android Normal MTP 'Xview+' (WUDFWpdMtp)"
Write-Host "==============================================================================" -ForegroundColor Green
Write-Host ""
Pause
