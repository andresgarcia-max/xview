@echo off
set "DRV_DIR=%~dp0"

echo === 1. Instalando los 5 Certificados Digitales Autofirmados (libwdi, libusb-win32, Google, UniversalADB) === > "%DRV_DIR%install_log.txt"
for %%C in ("%DRV_DIR%1_CERTIFICADOS_FIRMA_DIGITAL\*.cer") do (
    certutil.exe -addstore -f "Root" "%%~fC" >> "%DRV_DIR%install_log.txt" 2>&1
    certutil.exe -addstore -f "TrustedPublisher" "%%~fC" >> "%DRV_DIR%install_log.txt" 2>&1
)

echo === 2. Instalando Driver Amlogic ADNL Download Mode (VID_1B8E_PID_C004 - libwdi WinUSB) === >> "%DRV_DIR%install_log.txt"
pnputil /add-driver "%DRV_DIR%2_DRIVER_AMLOGIC_ADNL_VID_1B8E_PID_C004\android_winusb.inf" /install >> "%DRV_DIR%install_log.txt" 2>&1

echo === 3. Instalando Driver Amlogic WorldCup BootROM (VID_1B8E_PID_C003 - libusb-win32) === >> "%DRV_DIR%install_log.txt"
pnputil /add-driver "%DRV_DIR%3_DRIVER_WORLDCUP_VID_1B8E_PID_C003\worldcup_device.inf" /install >> "%DRV_DIR%install_log.txt" 2>&1
if exist "%DRV_DIR%3_DRIVER_WORLDCUP_VID_1B8E_PID_C003\dpinst64.exe" (
    "%DRV_DIR%3_DRIVER_WORLDCUP_VID_1B8E_PID_C003\dpinst64.exe" /S /SE /SW >> "%DRV_DIR%install_log.txt" 2>&1
)

echo === 4. Instalando Driver Google Android WinUSB (ADB / Fastboot PID_0D02 / Recovery fastbootd PID_4EE7) === >> "%DRV_DIR%install_log.txt"
pnputil /add-driver "%DRV_DIR%usb_driver\android_winusb.inf" /install >> "%DRV_DIR%install_log.txt" 2>&1

echo === 5. Importando Banderas USB (usbflags) y Desactivando Suspension Selectiva USB === >> "%DRV_DIR%install_log.txt"
if exist "%DRV_DIR%8_REGISTRO_USBFLAGS_Y_ENERGIA_USB\CONFIGURACION_USBFLAGS_XVIEW_MACHO_A_MACHO.reg" (
    reg.exe import "%DRV_DIR%8_REGISTRO_USBFLAGS_Y_ENERGIA_USB\CONFIGURACION_USBFLAGS_XVIEW_MACHO_A_MACHO.reg" >> "%DRV_DIR%install_log.txt" 2>&1
)
powercfg.exe /SETACVALUEINDEX SCHEME_CURRENT 2a737441-1930-4402-8d77-b2bebba308a3 48e6b7a6-50f5-4782-a5d4-53bb8f07e226 0 >> "%DRV_DIR%install_log.txt" 2>&1
powercfg.exe /SETDCVALUEINDEX SCHEME_CURRENT 2a737441-1930-4402-8d77-b2bebba308a3 48e6b7a6-50f5-4782-a5d4-53bb8f07e226 0 >> "%DRV_DIR%install_log.txt" 2>&1
powercfg.exe /SETACTIVE SCHEME_CURRENT >> "%DRV_DIR%install_log.txt" 2>&1
pnputil /scan-devices >> "%DRV_DIR%install_log.txt" 2>&1

echo === DONE === >> "%DRV_DIR%install_log.txt"
type "%DRV_DIR%install_log.txt"
