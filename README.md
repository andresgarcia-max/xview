# Proyecto de Investigación y Documentación Técnica: Xview+ / ZTE ZXV10 B866V2 (2019–2027)

Repositorio completo con el análisis de hardware, firmware (`DMC.v2.25` Android 12 y Android 9 `V82815415.0083`), configuraciones de sistema (`/vendor/etc`, `/system/etc`, `/product/etc`), árboles de dispositivos (`.dts` / `.dtb`), planos de placa base (PCB), pinout ISP/UART, capturas forenses UART TTL en vivo, imágenes de partición e investigación de variantes desde el **ZTE ZXV10 B866V2 (Amlogic S905X2)** hasta la generación **2026–2027 (`B866V6` con Amlogic S905X5M / S905Y5 en 6 nm)**.

---

## Estructura del Repositorio

### 1. [`experimento/`](./experimento) — Extracción Completa, Planos, Capturas UART y Documentación
* **[`01_info_sistema/`](./experimento/01_info_sistema):** Volcados directos de propiedades (`getprop`), tabla de particiones eMMC (`/dev/block/by-name/*`), CPU/Kernel/Memoria, configuración HDMI/EDID (`SurfaceFlinger`, `dumpsys display`) y paquetes del sistema.
* **[`02_propiedades_build/`](./experimento/02_propiedades_build):** Archivos `build.prop` de las particiones `product` y `system_ext`.
* **[`03_configuraciones_etc/`](./experimento/03_configuraciones_etc):** Configuraciones completas extraídas de `/vendor/etc`, `/system/etc`, `/product/etc`, `/odm/etc` y scripts de arranque `init` (`.rc`), incluyendo perfiles de códecs 4K (`media_codecs.xml`), audio (`audio_policy_configuration.xml`) y tabla de montaje (`fstab.amlogic`).
* **[`04_apks_dispositivo/`](./experimento/04_apks_dispositivo):** Aplicaciones de sistema ZTE, DroidLogic/Amlogic, Launcher y utilidades extraídas del dispositivo.
* **[`06_internet_y_repositorios/`](./experimento/06_internet_y_repositorios):**
  * **[`ENCICLOPEDIA_XVIEW_ZTE_B866_Y_VARIANTES_2027.md`](./experimento/06_internet_y_repositorios/ENCICLOPEDIA_XVIEW_ZTE_B866_Y_VARIANTES_2027.md):** Enciclopedia técnica con especificaciones de `B866V2`, `B866V2-H`, `B866V2F` (AV1), `B866V5` y los modelos **2026–2027 (`B866V6` / `S905X5M`)**.
  * **[`github_repos/`](./experimento/06_internet_y_repositorios/github_repos):** Repositorios recopilados (`mirko-geno/b866v2-debian`, `eryzxsd/B866F-GTV-Project`, `superna9999/pyamlboot`, `An-rus/ZTE-ZXV10-B826C-A12-STB-Security-Research`, `ophub/amlogic-s9xxx-armbian`, etc.).
* **[`07_planos_diagramas_y_esquematicos/`](./experimento/07_planos_diagramas_y_esquematicos):**
  * **[`PLANO_PCB_ZTE_B866V2_PINOUT_ISP_UART.svg`](./experimento/07_planos_diagramas_y_esquematicos/PLANO_PCB_ZTE_B866V2_PINOUT_ISP_UART.svg):** Plano técnico de la placa `franklin` (`u212`) con puntos ISP eMMC (`DAT0`, `CLK`, `CMD`, `VCCQ 1.8V`, `VCC 3.3V`, `GND`), modo MaskROM y puerto serial UART (`115200 8N1`).
  * **[`DIAGRAMA_ARQUITECTURA_VIDEO_Y_PARTICIONES.svg`](./experimento/07_planos_diagramas_y_esquematicos/DIAGRAMA_ARQUITECTURA_VIDEO_Y_PARTICIONES.svg):** Mapa de particiones eMMC, cadena de arranque (`BL1` → `BL2` → `BL31` → `U-Boot` → `Kernel`) y pipeline de video.
  * **[`VISOR_INTERACTIVO_PLANOS_Y_DIAGRAMAS.html`](./experimento/07_planos_diagramas_y_esquematicos/VISOR_INTERACTIVO_PLANOS_Y_DIAGRAMAS.html):** Visor interactivo HTML de planos y tablas de voltajes.
* **[`08_device_trees_uboot_y_configs_firmware/`](./experimento/08_device_trees_uboot_y_configs_firmware):** Archivos fuente de Device Tree (`.dts` / `.dtsi`) para Amlogic `meson-g12a` (`S905X2`) y `meson-s4` (`S905Y4`), configuración de entorno U-Boot (`uboot_env_franklin_b866v2.cfg`), plantilla de empaquetado (`aml_upgrade_package.conf`) y script de optimización ADB.
* **[`09_analisis_archivos_usuario/`](./experimento/09_analisis_archivos_usuario):** Scripts desofuscados (`B866V2-FlashATV12_Evoca_DESOFUSCADO.bat`) y configuraciones extraídas para auditoría.
* **[`10_ZTE_B820C_A15_LE_COMPLETO/`](./experimento/10_ZTE_B820C_A15_LE_COMPLETO):** Investigación completa y análisis forense de APKs híbridas ZTE (`B820C-A15 LE` / `B866V2`) y desbloqueo de partición `conf`.
* **[`11_VALIDACION_OTA_Y_FIRMWARE_ZTE/`](./experimento/11_VALIDACION_OTA_Y_FIRMWARE_ZTE):** Validación criptográfica (`AVB 1.0` y `otacerts.zip` RSA-2048) de cada partición del firmware `B866V2 Android 12`, imagen `cache_ext4.img` pre-formateada y script blindado Anti-Watchdog [`FLASHEAR_B866V2_ANDROID12_SEGURO_SIN_PERDER_WIDEVINE.bat`](./experimento/11_VALIDACION_OTA_Y_FIRMWARE_ZTE/FLASHEAR_B866V2_ANDROID12_SEGURO_SIN_PERDER_WIDEVINE.bat).
* **[`12_KIT_USB_QADEBUG_Y_EXTRACCION/`](./experimento/12_KIT_USB_QADEBUG_Y_EXTRACCION):** Kit automatizado para preparar memorias USB FAT32 con llaves multi-versión (`QADEBUG.ini`, `QADEBUGTOOL.ini`, `logconfig.ini`) y desencriptar los logs volcados por la caja (`2_ANALIZAR_Y_DESENCRIPTAR_LOGS_EXTRAIDOS.bat`).
* **[`13_CAPTURA_UART_TTL_SMARTTY/`](./experimento/13_CAPTURA_UART_TTL_SMARTTY):** Volcado e [`INFORME_FORENSE_UART_TTL_SMARTTY_REA.md`](./experimento/13_CAPTURA_UART_TTL_SMARTTY/INFORME_FORENSE_UART_TTL_SMARTTY_REA.md) de la secuencia de arranque completa (`BL1` $\rightarrow$ `BL2` $\rightarrow$ `BL31/32` $\rightarrow$ `U-Boot` $\rightarrow$ `Kernel`), diagnóstico de `GPIOAO_3 == 0`, ventana `InUsbBurn`, verificación AVB con `testkey` y mapa de las 21 particiones eMMC.
* **[`14_MONITOREO_EN_VIVO_SMARTTY/`](./experimento/14_MONITOREO_EN_VIVO_SMARTTY):** Monitor en tiempo real de memoria virtual (`monitor_smartty.ps1`), captura de los 5 ciclos de arranque en vivo (`U-Boot 2015.01-g220e992`), [`INFORME_SESION_FASTBOOT_WATCHDOG_Y_SOLUCION.md`](./experimento/14_MONITOREO_EN_VIVO_SMARTTY/INFORME_SESION_FASTBOOT_WATCHDOG_Y_SOLUCION.md) y script [`RETENER_FASTBOOT_ANTI_WATCHDOG.bat`](./experimento/14_MONITOREO_EN_VIVO_SMARTTY/RETENER_FASTBOOT_ANTI_WATCHDOG.bat) contra el reinicio por Watchdog (`reboot_mode:::: watchdog_reboot` $\rightarrow$ `ztebsp: bootcmd=bootsys`).

### 2. [`diagramas/`](./diagramas) — Fotografías Reales de Placas Base y Análisis Comparativo (`STBAR_C` vs `STBAR_J`)
* **[`ANALISIS_COMPARATIVO_PLACAS_STBAR_C_VS_STBAR_J.md`](./diagramas/ANALISIS_COMPARATIVO_PLACAS_STBAR_C_VS_STBAR_J.md):** Análisis forense comparando cara superior e inferior de las dos revisiones físicas de placa base ZTE (`71162`).

### 3. [`V2/`](./V2) — Imágenes de Partición y Herramientas Fastboot (`B866V2 Android 12`)
* Incluye `boot.img`, `bootloader.img`, `dtb.img` (`g12a_u212_2g`), `dtbo.img`, `factory.img` (`KEYBOX PARTFAT12`), `logo.img`, `param.img`, `conf.img`, `cri_data.img`, `oem.img` y utilidades de plataforma.

### 4. Librerías Nativas Extraídas (`.so`) y APK Base
* Librerías nativas ARM64 (`libranger-jni.so`, `libijkffmpeg.so`, `libijkplayer.so`, `libijksdl.so`, `libtnet-3.1.14.so`, etc.) y `xuper hydra.apk`.

### 5. Ingeniería Inversa con REA ([`rea/`](./rea) y [`ANALISIS_REA_XUPER_Y_FIRMWARE_XVIEW.md`](./ANALISIS_REA_XUPER_Y_FIRMWARE_XVIEW.md))
* **[`ANALISIS_REA_XUPER_Y_FIRMWARE_XVIEW.md`](./ANALISIS_REA_XUPER_Y_FIRMWARE_XVIEW.md):** Reporte forense completo con REA (`rea-agents@6.8.0`).
* **[`rea/`](./rea):** Copia local completa del framework de ingeniería inversa `morluto/rea` ([documentación en español](./rea/docs/readme/README_es.md)).

### 6. Bitácora Maestra de Pruebas, Aciertos, Opciones Sin Abrir la Caja y Errores Descartados
* **[`BITACORA_PRUEBAS_OPCIONES_Y_ERRORES_XVIEW.md`](./BITACORA_PRUEBAS_OPCIONES_Y_ERRORES_XVIEW.md):** Registro maestro persistente de todas las pruebas reales, aciertos, opciones descartadas y soluciones verificadas (incluyendo blindaje Anti-Watchdog en Fastboot/Recovery, drivers TTL firmados y protección de `Widevine L1`).
* **[`LIBERAR_SIN_ABRIR_CAJA.bat`](./LIBERAR_SIN_ABRIR_CAJA.bat):** Script automatizado para liberar una caja cerrada por ADB (USB o Red) o enviarla directamente a modo `Fastboot` (`adb reboot bootloader`) sin abrir la carcasa.
* **[`QADEBUG.ini`](./QADEBUG.ini) y [`QADEBUGTOOL.ini`](./QADEBUGTOOL.ini):** Archivos cifrados de `138 bytes` válidos para `V82815417.2033` y `V84711310.1024`.
* **[`INSTALAR_DRIVERS_TTL_Y_ABRIR_SMARTTY.bat`](./INSTALAR_DRIVERS_TTL_Y_ABRIR_SMARTTY.bat) y [`Drivers_TTL/`](./Drivers_TTL):** Instalador firmado y configurador automático (`115200 8N1`) para el adaptador USB-TTL `Silicon Labs CP2102` y [`SmarTTY.exe`](./SmarTTY.exe).
