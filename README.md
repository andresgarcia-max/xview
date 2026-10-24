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

### 2. [`V2/`](./V2) — Imágenes de Partición y Herramientas Fastboot (`B866V2 Android 12`)
* Incluye `boot.img`, `bootloader.img`, `dtb.img` (`g12a_u212_2g`), `dtbo.img`, `factory.img` (`KEYBOX PARTFAT12`), `logo.img`, `param.img`, `conf.img`, `cri_data.img`, `oem.img` y utilidades de plataforma.

### 3. Librerías Nativas Extraídas (`.so`) y APK Base
* Librerías nativas ARM64 (`libranger-jni.so`, `libijkffmpeg.so`, `libijkplayer.so`, `libijksdl.so`, `libtnet-3.1.14.so`, etc.) y `xuper hydra.apk`.
