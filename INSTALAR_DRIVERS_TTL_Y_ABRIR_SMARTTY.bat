@echo off
chcp 65001 >nul
title Instalador de Driver CP2102 USB-TTL y Configurador SmarTTY (Xview ZTE B866V2)
color 0A

:: Verificar permisos de Administrador y auto-elevar si es necesario
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo ============================================================================
    echo   Solicitando permisos de Administrador para instalar el driver CP2102...
    echo   Por favor haz clic en "Si" en la ventana de Windows.
    echo ============================================================================
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Start-Process -FilePath '%~f0' -Verb RunAs"
    exit /b
)

cd /d "C:\Users\CORSAIR\Desktop\xview"

echo ============================================================================
echo   1. CERRANDO SMARTTY ANTERIOR E INSTALANDO DRIVER SILICON LABS CP2102
echo ============================================================================
taskkill /f /im SmarTTY.exe >nul 2>&1
echo.
echo [+] Instalando driver oficial firmado Silicon Labs CP210x (VID_10C4 PID_EA60)...
pnputil /add-driver "C:\Users\CORSAIR\Desktop\xview\Drivers_TTL\CP210x\silabser.inf" /install

echo.
echo [+] Refrescando dispositivos USB Plug and Play...
pnputil /scan-devices >nul 2>&1
timeout /t 3 /nobreak >nul

echo.
echo ============================================================================
echo   2. DETECTANDO PUERTO COM Y CONFIGURANDO SMARTTY (115200 8N1)
echo ============================================================================
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ports = [System.IO.Ports.SerialPort]::GetPortNames(); " ^
    "Get-PnpDevice -Class Ports -PresentOnly -ErrorAction SilentlyContinue | ForEach-Object { Write-Host ('  -> PUERTO ACTIVO DETECTADO: ' + $_.FriendlyName) -ForegroundColor Cyan }; " ^
    "$asm = [System.Reflection.Assembly]::LoadFile('C:\Users\CORSAIR\Desktop\xview\SmarTTY.exe'); " ^
    "$rawType = $asm.GetType('VisualTTY.DeviceTerminal.RawConnectionSettings'); " ^
    "$devType = $asm.GetType('VisualTTY.DeviceTerminal.DeviceTerminalSettings'); " ^
    "$serType = $asm.GetType('VisualTTY.DeviceTerminal.DeviceTerminalSettings+ConnectionSettings+Serial'); " ^
    "$advType = $asm.GetType('VisualTTY.DeviceTerminal.DeviceTerminalSettings+ConnectionSettings+Serial+AdvancedSerialPortSettings'); " ^
    "$xs = New-Object System.Xml.Serialization.XmlSerializer($rawType); " ^
    "$saveDir = 'C:\Users\CORSAIR\AppData\Roaming\SmarTTY\SavedConnections'; " ^
    "New-Item -ItemType Directory -Path $saveDir -Force | Out-Null; " ^
    "Get-ChildItem -Path $saveDir -Filter '*.rawconnection' -ErrorAction SilentlyContinue | Remove-Item -Force; " ^
    "$allPorts = @('COM1','COM2','COM3','COM4','COM5','COM6','COM7','COM8','COM9','COM10'); " ^
    "foreach ($p in $allPorts) { " ^
    "  $raw = [Activator]::CreateInstance($rawType); " ^
    "  $raw.Settings = [Activator]::CreateInstance($devType); " ^
    "  $ser = [Activator]::CreateInstance($serType); " ^
    "  $ser.ComPortName = $p; " ^
    "  $ser.DeviceDescription = 'Silicon Labs CP210x USB to UART Bridge'; " ^
    "  $adv = [Activator]::CreateInstance($advType); " ^
    "  $adv.BaudRate = 115200; $adv.DataBits = 8; " ^
    "  $adv.Parity = [System.IO.Ports.Parity]::None; " ^
    "  $adv.StopBits = [System.IO.Ports.StopBits]::One; " ^
    "  $adv.FlowControl = [System.IO.Ports.Handshake]::None; " ^
    "  $ser.AdvancedSettings = $adv; " ^
    "  $raw.Settings.Connection = $ser; " ^
    "  $raw.Settings.LastConnectionTime = [DateTime]::Now.Ticks; " ^
    "  $raw.Icon = 'SerialPort.png'; " ^
    "  $fp = Join-Path $saveDir ($p + '_CP2102_115200.rawconnection'); " ^
    "  $fs = [System.IO.File]::Create($fp); $xs.Serialize($fs, $raw); $fs.Close(); " ^
    "}; " ^
    "Write-Host '  [+] Velocidad 115200 baudios (8N1) preconfigurada para Silicon Labs CP210x.' -ForegroundColor Green"

echo.
echo ============================================================================
echo   3. ABRIENDO SMARTTY CON TU ADAPTADOR CP2102 LISTO...
echo ============================================================================
start "" "C:\Users\CORSAIR\Desktop\xview\SmarTTY.exe"
echo.
echo Listo. En la ventana de SmarTTY ya te aparecera:
echo   "Silicon Labs CP210x USB to UART Bridge (COMx)" a 115200 baudios.
echo Solo haz doble clic sobre el o presiona el boton "Connect".
echo.
pause
