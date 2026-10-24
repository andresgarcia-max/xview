@echo off
chcp 65001 >nul
echo ============================================================================
echo   HERRAMIENTA DE CONFIGURACION Y OPTIMIZACION ADB - ZTE ZXV10 B866V2 (XVIEW)
echo ============================================================================
set IP_XVIEW=192.168.1.20:5555

echo Conectando a %IP_XVIEW%...
adb connect %IP_XVIEW%
echo.

echo [1] Activando auto-adaptacion HDMI (key_hdmi_selfadaption=1)...
adb -s %IP_XVIEW% shell settings put system key_hdmi_selfadaption 1

echo [2] Verificando modos HDMI y EDID reportados por el televisor...
adb -s %IP_XVIEW% shell "dumpsys display | grep -iE 'DisplayDeviceInfo|mActiveModeId'"

echo [3] Verificando resolucion y densidad actuales...
adb -s %IP_XVIEW% shell wm size
adb -s %IP_XVIEW% shell wm density

echo.
echo ============================================================================
echo COMANDOS OPCIONALES DE RESOLUCION (Descomentar solo si se usa en TV 4K):
echo   Forzar lienzo 4K:       adb -s %IP_XVIEW% shell wm size 3840x2160
echo                           adb -s %IP_XVIEW% shell wm density 640
echo   Restaurar 1080p nativo: adb -s %IP_XVIEW% shell wm size reset
echo                           adb -s %IP_XVIEW% shell wm density reset
echo ============================================================================
pause
