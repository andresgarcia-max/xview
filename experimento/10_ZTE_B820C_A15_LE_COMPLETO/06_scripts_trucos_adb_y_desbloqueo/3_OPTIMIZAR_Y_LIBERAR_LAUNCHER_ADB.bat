@echo off
chcp 65001 >nul
title LIBERACION Y OPTIMIZACION ADB - ZTE B820C-A15 LE (izzi) / B866V2 (Xview+)
echo ============================================================================
echo   SCRIPT DE OPTIMIZACION, DESBLOQUEO DE LAUNCHER Y DEBLOAT VÍA ADB
echo   Compatible con ZTE ZXV10 B820C-A15 LE (izzi) y ZTE ZXV10 B866V2 (Xview+)
echo ============================================================================
echo.

set "ADB=%~dp0..\..\..\V2\B866v2 Android 12\B866v2 Android 12\B866V2\adb.exe"
if not exist "%ADB%" (
    set "ADB=%~dp0..\..\02_repositorios_github_y_herramientas\tohenk__b860hv5-os10-root\bin\Minimal ADB and Fastboot\adb.exe"
)

set /p TARGET_IP="Ingresa la IP del decodificador (o presiona ENTER si esta por cable USB): "
if not "%TARGET_IP%"=="" (
    echo Conectando a %TARGET_IP%:5555...
    "%ADB%" connect %TARGET_IP%:5555
    set "ADB_TARGET=-s %TARGET_IP%:5555"
) else (
    set "ADB_TARGET="
)

echo.
echo [1] Dispositivos ADB detectados:
"%ADB%" devices -l

echo.
echo [2] Extrayendo informacion de modelo, SoC y firmware actual...
"%ADB%" %ADB_TARGET% shell "getprop ro.product.model; getprop ro.build.description; getprop ro.build.fingerprint; getprop ro.hardware"

echo.
echo [3] Listando paquetes del operador (ZTE / Mirada / izzi / Xview)...
"%ADB%" %ADB_TARGET% shell "pm list packages | grep -iE 'zte|mirada|izzi|nagra|xview|droidlogic|upgrade'"

echo.
echo [4] Deshabilitando telemetria, actualizaciones ZTE y agentes de bloqueo de launcher...
for %%K in (
    tv.upgrade.service
    com.zte.tv.act
    com.ztestb.uploaddiagno
    com.zte.factorytesttool
    com.ztestb.LauncherCustomize
    com.zte.android.productcfg
    com.zte.ums.oes
    com.zte.ota
    com.dotscreen.megacable.atv
    tv.mirada.iris
    com.mirada.iris
) do (
    echo    -> Desactivando %%K ^(si existe^)...
    "%ADB%" %ADB_TARGET% shell "am force-stop %%K; pm disable-user --user 0 %%K" 2>nul
)

echo.
echo [5] Quitando el banner "No se pudo instalar la actualizacion del sistema"...
"%ADB%" %ADB_TARGET% shell "settings put global ota_disable_automatic_update 1" 2>nul
"%ADB%" %ADB_TARGET% shell "cmd notification snooze --for 315360000000 '0|com.google.android.gms|1|com.google.android.gms.update.control.NotificationControl|10049'" 2>nul
"%ADB%" %ADB_TARGET% shell "cmd appops set com.google.android.gms POST_NOTIFICATION ignore" 2>nul
"%ADB%" %ADB_TARGET% shell "am force-stop com.google.android.gms" 2>nul

echo.
echo [6] Habilitando instalacion de fuentes desconocidas y optimizando HDMI/UI...
"%ADB%" %ADB_TARGET% shell settings put global install_non_market_apps 1
"%ADB%" %ADB_TARGET% shell settings put global adb_enabled 1
"%ADB%" %ADB_TARGET% shell settings put global window_animation_scale 0.5
"%ADB%" %ADB_TARGET% shell settings put global transition_animation_scale 0.5
"%ADB%" %ADB_TARGET% shell settings put global animator_duration_scale 0.5
"%ADB%" %ADB_TARGET% shell settings put system key_hdmi_selfadaption 1

echo.
echo ============================================================================
echo [OK] Optimizacion, eliminacion de banner OTA y desactivacion de bloatware lista.
echo ============================================================================
pause
