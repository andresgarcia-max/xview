# Proyecto de Investigación y Documentación Técnica: Xview+ / ZTE ZXV10 B866V2 (2019–2027)

Repositorio completo con el análisis de hardware, firmware (`DMC.v2.25` Android 12), configuraciones de sistema (`/vendor/etc`, `/system/etc`, `/product/etc`), árboles de dispositivos (`.dts` / `.dtb`), planos de placa base (PCB), pinout ISP/UART, imágenes de partición e investigación de variantes desde el **ZTE ZXV10 B866V2 (Amlogic S905X2)** hasta la generación **2026–2027 (`B866V6` con Amlogic S905X5M / S905Y5 en 6 nm)**.

---

## Estructura del Repositorio

### 1. [`experimento/`](./experimento) — Extracción Completa, Planos y Documentación
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

### 2. [`diagramas/`](./diagramas) — Fotografías Reales de Placas Base y Análisis Comparativo (`STBAR_C` vs `STBAR_J`)
* **[`ANALISIS_COMPARATIVO_PLACAS_STBAR_C_VS_STBAR_J.md`](./diagramas/ANALISIS_COMPARATIVO_PLACAS_STBAR_C_VS_STBAR_J.md):** Análisis forense comparando cara superior e inferior de las dos revisiones físicas de placa base ZTE (`71162`):
  * **Revisión `STBAR_C` (`190602` / Junio 2019):** 4 chips RAM UnilC (`SCB13H4G160AF-11M`, 2 en cada cara), eMMC SanDisk (`SDINBDG4-8G`), chip Wi-Fi/BT MediaTek `MT7668RSN` soldado directo al PCB con antena coaxial externa y disipador de aluminio con aletas.
  * **Revisión `STBAR_J` (`200400` / `ASKPCB 2212` / Marzo 2022):** 2 chips RAM DDR4 `CXMT` (`CXDQ3BFAM-CG`) solo en cara superior (bancos inferiores vacíos), eMMC Samsung (`KLM8G1GETF-B041`), módulo Wi-Fi/BT almenado MediaTek `MT7661RSN` con antenas integradas en PCB y disipador plano negro.

### 3. [`V2/`](./V2) — Imágenes de Partición y Herramientas Fastboot (`B866V2 Android 12`)
* Incluye `boot.img`, `bootloader.img`, `dtb.img` (`g12a_u212_2g`), `dtbo.img`, `factory.img` (`KEYBOX PARTFAT12`), `logo.img`, `param.img`, `conf.img`, `cri_data.img`, `oem.img` y utilidades de plataforma.

### 4. Librerías Nativas Extraídas (`.so`) y APK Base
* Librerías nativas ARM64 (`libranger-jni.so`, `libijkffmpeg.so`, `libijkplayer.so`, `libijksdl.so`, `libtnet-3.1.14.so`, etc.) y `xuper hydra.apk`.

### 5. Ingeniería Inversa con REA ([`rea/`](./rea) y [`ANALISIS_REA_XUPER_Y_FIRMWARE_XVIEW.md`](./ANALISIS_REA_XUPER_Y_FIRMWARE_XVIEW.md))
* **[`ANALISIS_REA_XUPER_Y_FIRMWARE_XVIEW.md`](./ANALISIS_REA_XUPER_Y_FIRMWARE_XVIEW.md):** Reporte forense completo con REA (`rea-agents@6.8.0`), incluyendo comparación binaria entre `xuper hydra.apk` y `com.xuper.netxxus.apk` (`XSairec`), desencriptación Triple-DES (`DESede/ECB/PKCS5Padding`) de la `PORTAL_KEY` y los 13 dominios ocultos en `resources.arsc`, parches Dalvik en `classes2.dex`, análisis de las 10 imágenes de partición del firmware `B866V2` Android 12 (`20231106`) y bitácora detallada de pruebas, errores y soluciones.
* **[`rea/`](./rea):** Copia local completa del framework de ingeniería inversa `morluto/rea` ([documentación en español](./rea/docs/readme/README_es.md)).

