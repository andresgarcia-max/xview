@echo off
title CONFIGURACION ESPECIAL ASUS ROG STRIX B550-A GAMING PARA XVIEW+ (MACHO A MACHO)
color 0B
echo ===============================================================================
echo   CONFIGURADOR ESPECIAL PARA TARJETA MADRE: ASUS ROG STRIX B550-A GAMING
echo   Corrige el controlador AMD B550 xHCI + Instala Certificados y Drivers Xview+
echo ===============================================================================
echo.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0CONFIGURAR_ASUS_ROG_STRIX_B550_PARA_XVIEW.ps1"
