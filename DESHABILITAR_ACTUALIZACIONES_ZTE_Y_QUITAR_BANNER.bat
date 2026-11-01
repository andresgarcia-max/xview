@echo off
chcp 65001 >nul
title DESHABILITAR TELEMETRIA/OTA ZTE Y QUITAR BANNER DE ACTUALIZACION (XVIEW B866V2)
color 0A

set "ADB=%~dp0xview\V2\B866v2 Android 12\B866v2 Android 12\B866V2\adb.exe"
if not exist "%ADB%" set "ADB=%~dp0V2\B866v2 Android 12\B866v2 Android 12\B866V2\adb.exe"
if not exist "%ADB%" set "ADB=C:\Users\CORSAIR\Desktop\xview\V2\B866v2 Android 12\B866v2 Android 12\B866V2\adb.exe"

echo ============================================================================
echo   DESHABILITAR ACTUALIZACIONES/TELEMETRIA ZTE Y QUITAR BANNER DE SISTEMA
echo   Paquetes: tv.upgrade.service, com.zte.tv.act, com.ztestb.uploaddiagno,
echo             com.zte.factorytesttool + Banner Google Play Services OTA
echo ============================================================================
echo.
set /p "TARGET_IP=Ingresa la IP de la caja Xview (ejemplo 192.168.0.47 o ENTER si esta por USB): "
if not "%TARGET_IP%"=="" (
    echo [+] Conectando a %TARGET_IP%:5555...
    "%ADB%" connect %TARGET_IP%:5555
    set "ADB_CMD="%ADB%" -s %TARGET_IP%:5555"
) else (
    set "ADB_CMD="%ADB%""
)

echo.
echo [1/3] Deteniendo y deshabilitando paquetes residuales de actualizacion/telemetria ZTE...
for %%P in (
    tv.upgrade.service
    com.zte.tv.act
    com.ztestb.uploaddiagno
    com.zte.factorytesttool
) do (
    echo   -^> Deshabilitando %%P...
    %ADB_CMD% shell "am force-stop %%P; pm disable-user --user 0 %%P"
)

echo.
echo [2/3] Quitando el banner "No se pudo instalar la actualizacion del sistema"...
%ADB_CMD% shell "settings put global ota_disable_automatic_update 1"
%ADB_CMD% shell "cmd notification snooze --for 315360000000 '0|com.google.android.gms|1|com.google.android.gms.update.control.NotificationControl|10049'" 2>nul
%ADB_CMD% shell "cmd appops set com.google.android.gms POST_NOTIFICATION ignore"
%ADB_CMD% shell "am force-stop com.google.android.gms"

echo.
echo [3/3] Verificando paquetes deshabilitados en el equipo:
%ADB_CMD% shell "pm list packages -d"

echo.
echo ============================================================================
echo   [OK] Proceso completado. Paquetes ZTE deshabilitados y banner eliminado.
echo ============================================================================
pause
