@echo off
title INSTALADOR COMPLETO DE DRIVERS USB MACHO-A-MACHO (XVIEW+ B866V2)
color 0B
echo ===============================================================================
echo   INSTALANDO TODOS LOS DRIVERS, CERTIFICADOS Y REGISTRO DEL CABLE MACHO A MACHO
echo   Extraidos de la PC funcional (Dell Latitude 7490 - Placa Base 0KP0FT)
echo ===============================================================================
echo.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0INSTALAR_DRIVERS_Y_CERTIFICADOS_MACHO_A_MACHO.ps1"
