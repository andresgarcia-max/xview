@echo off
chcp 65001 >nul
title Paso 2 - Analizar y Desencriptar Datos Extraidos de la Caja Xview
cls
powershell -ExecutionPolicy Bypass -File "%~dp0desencriptar_logs_usb.ps1" %*
echo.
pause
