@echo off
chcp 65001 >nul
title Validador Criptografico de Firmware y Paquetes OTA ZTE (Xview+ B866V2)
cls
powershell -ExecutionPolicy Bypass -File "%~dp0VALIDAR_FIRMWARE_Y_OTA_ZTE.ps1" %*
echo.
pause
