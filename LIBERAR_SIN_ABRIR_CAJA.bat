@echo off
chcp 65001 >nul
title LIBERAR XVIEW+ (ZTE B866V2) SIN ABRIR LA CAJA (ADB USB / RED -> LAUNCHER O FASTBOOT)
color 0B

set "ADB=C:\Users\CORSAIR\Desktop\xview\V2\B866v2 Android 12\B866v2 Android 12\B866V2\adb.exe"
set "FASTBOOT=C:\Users\CORSAIR\Desktop\xview\V2\B866v2 Android 12\B866v2 Android 12\B866V2\fastboot.exe"
set "APK_AUTOSTART=C:\Users\CORSAIR\Desktop\xview\experimento\09_analisis_archivos_usuario\autostart.apk"
set "FLASH_SEGURO=C:\Users\CORSAIR\Desktop\xview\experimento\11_VALIDACION_OTA_Y_FIRMWARE_ZTE\FLASHEAR_B866V2_ANDROID12_SEGURO_SIN_PERDER_WIDEVINE.bat"

echo ============================================================================
echo   KIT DE LIBERACION SIN ABRIR LA CAJA - MEGACABLE XVIEW+ (ZTE B866V2)
echo ============================================================================
echo.
echo Requisitos previos (sin abrir la caja):
echo   1. Inserta la memoria USB FAT32 con QADEBUG.ini y QADEBUGTOOL.ini, o
echo      abre Ajustes en la caja (con teclado USB presionando Win+I o por voz
echo      con el control remoto diciendo "Abrir Configuracion").
echo   2. En Ajustes -^> Preferencias del dispositivo -^> Informacion -^> Compilacion,
echo      presiona 7 veces OK y activa "Depuracion por USB" en Opciones de programador.
echo   3. Conecta el cable USB Macho-A a Tipo-C en el puerto USB 2.0 (junto a MicroSD)
echo      o manten la caja en la misma red Wi-Fi / Ethernet.
echo.
echo [1] Buscando caja Xview por cable USB...
"%ADB%" devices
echo.
set /p "IP_CAJA=Si la conectas por Red Wi-Fi/Ethernet escribe su IP (o presiona ENTER si usas cable USB): "
if not "%IP_CAJA%"=="" (
    echo [+] Conectando a %IP_CAJA%:5555...
    "%ADB%" connect %IP_CAJA%:5555
)

echo.
echo ============================================================================
echo   SELECCIONA QUE DESEAS HACER SIN ABRIR LA CAJA:
echo ============================================================================
echo   [1] Liberar directamente por ADB (Desactivar Megacable + Instalar Launcher)
echo   [2] Meter la caja cerrada en modo FASTBOOT y flashear Android 12 Completo
echo       (conservando Widevine L1 original)
echo ============================================================================
set /p "OPCION=Elige 1 o 2: "

if "%OPCION%"=="2" (
    echo.
    echo [+] Enviando comando 'adb reboot bootloader' para entrar a Fastboot sin abrir la caja...
    "%ADB%" reboot bootloader
    timeout /t 6 /nobreak >nul
    echo [+] Verificando conexion Fastboot en el puerto USB 2.0...
    "%FASTBOOT%" devices
    echo.
    echo [+] Iniciando flasheo seguro de Android 12 (sin borrar factory ni cri_data)...
    call "%FLASH_SEGURO%"
    exit /b 0
)

echo.
echo [+] Instalando aplicacion de inicio y liberando restricciones de operador...
if exist "%APK_AUTOSTART%" "%ADB%" install -r "%APK_AUTOSTART%"
"%ADB%" shell pm disable-user --user 0 com.dotscreen.megacable.atv 2>nul
"%ADB%" shell pm disable-user --user 0 com.ztestb.LauncherCustomize 2>nul
"%ADB%" shell pm disable-user --user 0 com.zte.ums.oes 2>nul
"%ADB%" shell pm disable-user --user 0 com.zte.ota 2>nul
"%ADB%" shell settings put global adb_enabled 1
"%ADB%" shell setprop persist.service.adb.enable 1
"%ADB%" shell setprop service.adb.tcp.port 5555

echo.
echo ============================================================================
echo   [OK] Caja liberada sin abrir la carcasa.
echo ============================================================================
pause
