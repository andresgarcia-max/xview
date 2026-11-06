# Programa Perfecto — Megacable Xview+ (`ZTE ZXV10 B866V2`)

Esta carpeta concentra **únicamente las herramientas, llaves, APKs y firmware 100% compatibles y seguros** para el modelo **`ZTE ZXV10 B866V2` (SoC Amlogic S905X2 `g12a`)**, descartando por completo los archivos de `ZTE B866F` (`S905Y4`) que causaban riesgo de brick y protegiendo siempre tus particiones `factory` (Widevine L1) y `cri_data` (MAC y Número de Serie).

---

## Cómo ejecutar el programa
Haz doble clic en:
- **`PROGRAMA_PERFECTO_XVIEW_B866V2.bat`**

---

## Estructura de Carpetas Filtradas y Fusionadas

1. **`PROGRAMA_PERFECTO_XVIEW_B866V2.bat`**
   - Lanzador principal con **Radar USB/ADB/Fastboot en Tiempo Real** y 8 módulos integrados.
2. **`1_HERRAMIENTAS_MOTOR\`**
   - `MOTOR_PROGRAMA_PERFECTO.ps1`: Motor maestro que fusiona `LIBERAR_SIN_ABRIR_CAJA.bat`, `adb comandos.txt`, `Comandos.txt`, `Tools cek slot .bat`, `MULTI TOOLS googletv.cmd` (`adnl.exe` / `update.exe`) y el descifrador `UCap_TranslateMap` de logs ZTE.
   - `adb.exe`, `fastboot_moderno.exe` (soporta `fastbootd` `PID_4EE7` y `U-Boot Fastboot` `PID_0D02`), `adnl.exe` (`v2.6.3`), `update.exe` y `mke2fs.exe`.
3. **`2_APKS_LIBERACION_Y_TV\`**
   - Launchers y utilidades de liberación offline:
     - `Home_Screen_Launcher_1.1.3.apk` (`dev.dworks.apps.alauncher`)
     - `TVLauncher_4_GoogleTV.apk`
     - `Button_Mapper_Pro_2.54.apk` (`flar2.homebutton` — reasignación del botón Home)
     - `autostart.apk`, `Filemanager.apk`, `Downloader_2.0.4.apk`
   - Apps adicionales de TV rescatadas de la memoria:
     - `signMagisTV_APS-MUTO.apk`, `xuper_hydra.apk`, `Hdplay.apk`, `DLStore.apk`, `AppDMC.apk`
4. **`3_LLAVES_USB_PARA_LA_CAJA\`**
   - `QADEBUG.ini` (verificado activo en firmware `V82815417.2033`), `QADEBUGTOOL.ini`, `logconfig.ini`, `factory_update_param.aml`, `usb_left.txt`, `usb_right.txt`.
5. **`4_DRIVERS_Y_GUI_WINDOWS\`**
   - `ADBAppControl-1.8.6-setup.exe` (Interfaz visual para gestionar apps por ADB).
   - `WorldCup_Driver_VID_1B8E\InstallDriver.exe` (Driver oficial Amlogic WorldCup `VID_1B8E`).
6. **`5_FIRMWARE_SEGURO_B866V2\`**
   - Imágenes verificadas para `ZTE B866V2` (`S905X2` `g12a`) + `cache_ext4.img` + `recovery.img` desbloqueado (`MarketCode 4301`), **sin `factory.img` ni `cri_data.img`** para que tus claves DRM Widevine L1 y tu MAC jamás puedan ser sobrescritas.
