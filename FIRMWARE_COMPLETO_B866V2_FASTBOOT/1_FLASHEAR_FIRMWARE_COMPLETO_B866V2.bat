@echo off
title INYECTADOR DE FIRMWARE FASTBOOT - ZTE ZXV10 B866V2 (XVIEW+)
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -STA -File "%~dp0MOTOR_FASTBOOT_B866V2.ps1"
