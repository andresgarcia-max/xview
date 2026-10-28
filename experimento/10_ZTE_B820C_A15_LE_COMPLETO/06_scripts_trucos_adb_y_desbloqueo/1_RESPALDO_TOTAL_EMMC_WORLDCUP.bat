@echo off
chcp 65001 >nul
title RESPALDO COMPLETO DE PARTICIONES eMMC - ZTE S905X2 (B866V2 / B820C-A15 LE)
echo ============================================================================
echo   VOLCADO FORENSE COMPLETO DE PARTICIONES eMMC VÍA AMLOGIC WORLDCUP USB
echo   Compatible con: ZTE ZXV10 B866V2 (Xview+) y ZTE ZXV10 B820C-A15 LE (izzi)
echo ============================================================================
echo.

setlocal EnableDelayedExpansion
set "UPDATER=%~dp0..\..\02_repositorios_github_y_herramientas\tohenk__b860hv5-os10-root\bin\aml-flash-tool\update.exe"
if not exist "%UPDATER%" (
    set "UPDATER=%~dp0..\..\..\V2\B866v2 Android 12\B866v2 Android 12\B866V2\update.exe"
)

if not exist "%UPDATER%" (
    echo [ERROR] No se encontro update.exe. Verifica la ruta del repositorio.
    pause
    exit /b 1
)

echo [1] Buscando dispositivo Amlogic en modo WorldCup / MaskROM USB...
"%UPDATER%" scan > "%TEMP%\aml_scan.txt" 2>&1
type "%TEMP%\aml_scan.txt"
findstr /i "port" "%TEMP%\aml_scan.txt" >nul
if not %ERRORLEVEL%==0 (
    echo.
    echo [!] No se detecto el decodificador en modo USB Burning / WorldCup.
    echo     1. Conecta el cable USB-A Macho a Macho en el puerto USB OTG.
    echo     2. Entra en modo MaskROM (manteniendo Reset o puenteando CLK a GND al encender,
    echo        o ejecutando 'update' desde consola UART).
    echo     3. Si Windows no reconoce el driver WorldCup_Device, instala el driver incluido en:
    echo        02_repositorios_github_y_herramientas\ewwink__driver-usb-vcom-stb-b860h-760h-amlogic-mediatek
    echo.
    pause
    exit /b 1
)

set "STAMP=%DATE:~-4%%DATE:~3,2%%DATE:~0,2%_%TIME:~0,2%%TIME:~3,2%"
set "STAMP=%STAMP: =0%"
set "OUTDIR=%~dp0backup_emmc_%STAMP%"
mkdir "%OUTDIR%"

echo.
echo [2] Dispositivo detectado. Guardando respaldo de particiones en:
echo     %OUTDIR%
echo.

for %%P in (
    "bootloader:0x400000"
    "reserved:0x4000000"
    "env:0x800000"
    "logo:0x800000"
    "recovery:0x1800000"
    "misc:0x800000"
    "conf:0x400000"
    "dtbo:0x800000"
    "cri_data:0x800000"
    "param:0x1000000"
    "boot:0x1000000"
    "oem:0x1000000"
    "metadata:0x1000000"
    "vbmeta:0x200000"
    "tee:0x2000000"
    "factory:0x800000"
) do (
    for /f "tokens=1,2 delims=:" %%a in (%%P) do (
        echo [*] Respaldando particion %%a ^(tamano %%b^)...
        "%UPDATER%" mread store %%a normal %%b "%OUTDIR%\%%a.img"
    )
)

echo.
echo ============================================================================
echo [OK] Respaldo de las 16 particiones criticas completado con exito en:
echo      %OUTDIR%
echo      (Incluye bootloader DDR3/DDR4 nativo, conf, factory Widevine L1 y cri_data)
echo ============================================================================
pause
