@echo off
chcp 65001 >nul
title Paso 1 - Preparar Memoria USB con Kit Poliglota QADEBUG + UDiskLog ZTE
cls
echo ================================================================================
echo   PASO 1: PREPARAR MEMORIA USB (FAT32) CON LLAVES QADEBUG Y EXTRACTOR UDISKLOG
echo ================================================================================
echo.
set "SRC=%~dp0ARCHIVOS_PARA_RAIZ_USB"
set "USB_DRV=D:"

if not exist "%USB_DRV%\" (
    set /p USB_DRV="Ingresa la letra de tu memoria USB (ejemplo D: o E:): "
)

echo Copiando archivos cifrados y configuracion de extraccion a %USB_DRV%\ ...
xcopy /E /Y /I "%SRC%\*" "%USB_DRV%\" >nul

echo.
echo [ARCHIVOS INSTALADOS EN %USB_DRV%\]
echo   1. %USB_DRV%\QADEBUG.ini              (Poliglota: Byte-1 [0..95] + UCap [96..193])
echo   2. %USB_DRV%\QADEBUGTOOL.ini          (Cifrado UCap_TranslateMap para FactoryTestTool)
echo   3. %USB_DRV%\logconfig.ini            (Activa volcado automatico de configs y logs)
echo   4. %USB_DRV%\LOG_ZTESTB\logconfig.ini (Ruta secundaria de UDiskLog en libztebasecomp_jni.so)
echo   5. %USB_DRV%\usb_left.txt y usb_right.txt
echo.
echo [QUE HACER AHORA PASO A PASO]
echo   1. Extrae la memoria USB de la PC.
echo   2. Enciende tu caja Xview normalmente en la TV y espera a que cargue la pantalla principal.
echo   3. Inserta la memoria USB en el puerto USB de la caja y dejala conectada 2 a 3 minutos.
echo   4. Retira la memoria USB, vuelve a conectarla a esta PC y ejecuta:
echo      2_ANALIZAR_Y_DESENCRIPTAR_LOGS_EXTRAIDOS_DEL_USB.bat
echo ================================================================================
pause
