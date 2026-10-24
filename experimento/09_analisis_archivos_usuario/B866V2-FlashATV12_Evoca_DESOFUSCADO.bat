
@shift /0
@echo off
@setlocal ENABLEDELAYEDEXPANSION
title ZTE-B866V2-0S12
chcp 65001
cls
cd B866V2
fastboot devices
timeout /t 2 /nobreak >nul
fastboot oem 'setenv lock 10100000;save
timeout /t 2 /nobreak >nul
fastboot reboot bootloader
timeout /t 2 /nobreak >nul
fastboot getvar unlocked
timeout /t 2 /nobreak >nul
fastboot flash boot boot.img
timeout /t 2 /nobreak >nul
fastboot flash bootloader bootloader.img
timeout /t 2 /nobreak >nul
fastboot flash _aml_dtb dtb.img
timeout /t 2 /nobreak >nul
fastboot flash dtbo dtbo.img
timeout /t 2 /nobreak >nul
fastboot flash factory factory.img
timeout /t 2 /nobreak >nul
fastboot flash logo logo.img
timeout /t 2 /nobreak >nul
fastboot flash param param.img
timeout /t 2 /nobreak >nul
fastboot flash cri_data cri_data.img
timeout /t 2 /nobreak >nul
fastboot flash conf conf.img
timeout /t 2 /nobreak >nul
fastboot flash oem oem.img
timeout /t 2 /nobreak >nul
fastboot flash super super.img
timeout /t 2 /nobreak >nul
fastboot flash vbmeta vbmeta.img
timeout /t 2 /nobreak >nul
fastboot flash recovery recovery.img
timeout /t 2 /nobreak >nul
fastboot erase userdata
echo.  
fastboot erase cache
timeout /t 2 /nobreak >nul
echo.
fastboot reboot
echo.
exit 
  

