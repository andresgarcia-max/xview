# Enciclopedia Técnica: Xview+ / ZTE ZXV10 Serie B866 y Variantes (2019–2027)

Este documento recopila la información técnica obtenida de repositorios de **GitHub**, bases de datos de hardware (**AndroidTV-Guide**, **ZTE STB Docs**), y foros internacionales (**XDA Developers**, **4PDA**, **FreakTab**, **Capa9**, **Reddit**) sobre la familia de decodificadores **ZTE ZXV10 B866** utilizados en **Megacable Xview+** y otros operadores globales (Claro, Viettel, Movistar, Flow, Bitel, MTS).

---

## 1. Arquitectura Interna del Modelo Base: ZTE ZXV10 B866V2

Datos verificados directamente contra el hardware (`ro.board.platform=g12a`, placa codename **`franklin`** / referencia Amlogic **`u212`**):

| Componente | Especificación Técnica |
| :--- | :--- |
| **Fabricante y Modelo** | ZTE Corporation — `ZXV10 B866V2` |
| **Nombre en clave de placa (Board)** | `franklin` (Referencia Amlogic `u212`) |
| **SoC (Procesador)** | **Amlogic S905X2** (`meson-g12a`), 12 nm, Quad-Core ARM Cortex-A53 @ 1.8 GHz |
| **GPU** | ARM Mali-G31 MP2 (Soporte OpenGL ES 3.2, Vulkan 1.1) |
| **Memoria RAM** | 2 GB DDR3 / DDR4 (`1,984,568 kB` totales visibles por el kernel) |
| **Almacenamiento** | 8 GB eMMC 5.1 (Particionado dinámico `super` en Android 12) |
| **Decodificación por Hardware** | `H.265 / HEVC` Main10 hasta **4096×2160 @ 60fps** (`OMX.amlogic.hevc.decoder.awesome2`), `VP9` Profile 2 4K@60fps, `H.264 / AVC` hasta 4K@30fps (`OMX.amlogic.avc.decoder.awesome2`) |
| **DRM y Certificaciones** | Google Widevine L1 + Microsoft PlayReady |
| **Chip Wi-Fi / Bluetooth** | **MediaTek MT7661LSN** (Driver Linux `mt7663` + controlador BT USB/SDIO; requiere firmware `mt7663` específico) |
| **Conectividad** | Wi-Fi 5 (802.11a/b/g/n/ac 2T2R Dual-Band), Bluetooth 4.2/5.0, Ethernet RJ45 10/100 Mbps, 2× USB 2.0 (1 Host + 1 OTG/Burn), HDMI 2.1 (TMDS 18 Gbps) |

---

## 2. Evolución Completa de Variantes (2019 a Modelos 2026–2027)

A lo largo de las generaciones de **Xview+** y despliegues internacionales de ZTE, la serie **B866** ha evolucionado en 5 generaciones principales de silicio Amlogic:

| Modelo / Variante | Año / Generación | Procesador (SoC) | Arquitectura / Nodo | Códec AV1 | GPU | Notas y Cambios Clave |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **ZXV10 B866V2** (Clásico) | 2019–2022 | **Amlogic S905X2** (`g12a`) | 4× Cortex-A53 (12 nm) | ❌ No | Mali-G31 MP2 | Primera generación masiva Xview+. Muy fácil de modificar por USB Burning Tool en lotes iniciales. |
| **ZXV10 B866V2-H / B866V4** | 2021–2023 | **Amlogic S905X3 / S905X4** (`sm1` / `sc2`) | 4× Cortex-A55 (12 nm) | ✔️ (en X4) | Mali-G31 MP2 | Revisión con bootloader firmado bloqueado por efuse. Requiere método ISP eMMC para cambio de ROM. |
| **ZXV10 B866V2F / V2FA / V2FAS** | 2022–2025 | **Amlogic S905Y4** (`s4`) | 4× Cortex-A35 @ 2.0 GHz (12 nm) | ✔️ Sí (4K@120fps) | Mali-G31 MP2 | Llamado comercialmente *"Xview+ AV1"*. Menor consumo térmico y soporte obligatorio de AV1 para YouTube/Netflix. |
| **ZXV10 B866V5 / B866V5.1** | 2024–2025 | **Amlogic S905Y4 / S905X4** | 4× Cortex-A35 / A55 | ✔️ Sí | Mali-G31 MP2 | Rediseño de PCB con mejoras en módulo Wi-Fi/BT integrado y Android TV 11/12 de fábrica. |
| **ZXV10 B866V6 / B866V6N / W12M** | **2026–2027** | **Amlogic S905X5M / S905Y5** (`s7` / `s7d`) | 4× Cortex-A55 @ 2.5 GHz (**6 nm**) | ✔️ Sí + H.266 | **Mali-G310 V2** | **Generación 2026–2027:** Salto de arquitectura a 6 nm, GPU 3 veces más rápida, **Interfaz de Usuario (UI) renderizada en 4K nativo**, **AI-SR (Super Resolution por IA)** para reescalar canales 1080p a 4K por hardware, HDMI 2.1a con QMS/VRR y Wi-Fi 6. |

### Otros Decodificadores Equivalentes en el Ecosistema Xview+ (Megacable)
Además de ZTE, la plataforma Xview+ utiliza cajas hermanas con la misma arquitectura Amlogic:
1. **Skyworth HP46A / HP46AV2 / HC8E00:** Basados en **Amlogic S905Y4 / S905X4** (2 GB RAM, 8/16 GB eMMC, Android TV 11/12).
2. **ZTE B826C-A12:** Variante híbrida con sintonizador digital **ATSC** + Android TV OTT (analizada en el repositorio `An-rus/ZTE-ZXV10-B826C-A12-STB-Security-Research`).
3. **SEI Robotics / Broadcom (`UIW4059MEG` / `UCW4046MEG`):** Variantes para redes HFC/FTTH.

---

## 3. Panorama de Firmwares y Custom ROMs (XDA / 4PDA / Comunidad)

### A. ROM `DMC.v2.25` (Base Viettel TV360 — Instalada actualmente en tu equipo)
* **Huella digital (`fingerprint`):** `Viettel/ViettelBox_B866V2/MediaBox_B866V2:12/SC/V84711310.1024:user/release-keys`
* **Compilación:** `franklin-user 12 SC eng.ztestb.20231106.184649 release-keys`
* **Características:** Portada a partir del firmware oficial de **Viettel (Vietnam)** para el `B866V2` con Android 12, modificada con el overlay `com.slimbox.tvlauncher.overlay` (`TVWall.apk`) y el launcher oficial de Android TV sin el bloqueo de operador de Claro/Megacable.
* **Limitación conocida:** Viene compilada en modo `user` (sin binario `su` root preinstalado) y mantiene el lienzo de interfaz (`vendor.display-size`) en `1920x1080`.

### B. SlimBoxTV (`sbox`) y Aidan's ROM
* Desarrolladas en los foros rusos **4PDA** y en **XDA Developers** para procesadores Amlogic `S905X2` (`u212` / `g12a`).
* Incluyen acceso **Root (Magisk/SuperSU)** opcional, ajustes avanzados de **Amlogic DroidSettings** (control manual de HDR→SDR, espacio de color RGB/YCbCr 4:4:4, auto-framerate y control CEC avanzado).
* **Advertencia de foros (4PDA/XDA):** Flashear una imagen genérica de S905X2 sin el `dtb` (Device Tree Blob) específico de la placa `franklin` del ZTE B866V2 desactiva el chip Wi-Fi/Bluetooth (`nanosic` / Realtek) o el control remoto Bluetooth.

### C. Sistemas Linux Alternativos (Armbian / Debian / CoreELEC / EmuELEC)
* **Debian en B866V2 (`mirko-geno/b866v2-debian`):** Configuración documentada para arrancar Debian Linux mínimo en el ZTE B866V2 usando el DTB `meson-g12a-u200.dtb` / `meson-g12a-x96-max.dtb`.
* **Armbian (`ophub/amlogic-s9xxx-armbian`):** Soporta los procesadores `S905X2` (`g12a`) y `S905Y4` mediante arranque externo USB/SD card (`u-boot.ext`).

---

## 4. Métodos de Desbloqueo, Modo MaskROM y Pinout ISP

En los foros técnicos se documentan tres niveles de acceso al hardware del **ZTE B866V2**:

1. **Nivel 1 — ADB por Red / USB (Activo en tu caja):**
   * Permite extraer/instalar aplicaciones, ajustar parámetros de ventana (`wm size`, `wm density`), deshabilitar paquetes del operador (`pm disable-user --user 0`) y leer configuraciones de `/system` y `/vendor`.
2. **Nivel 2 — Modo USB Burn / MaskROM (`pyamlboot` y `Amlogic USB Burning Tool`):**
   * Utiliza el protocolo nativo de la ROM de arranque del chip Amlogic `g12a` (implementado en código abierto en el repositorio descargado `superna9999/pyamlboot` mediante el script `boot-g12.py`).
   * Para forzar el modo MaskROM cuando el bootloader está cerrado, se realiza un puente momentáneo (*test point*) entre el pin **DAT0 (o CLK) de la memoria eMMC y tierra (GND)** al conectar la alimentación mientras el cable USB-A a USB-A está conectado al puerto USB del B866V2.
3. **Nivel 3 — Programación directa eMMC por ISP (En variantes `B866V2-H` / `B866V5`):**
   * En placas con bloqueo por hardware del puerto USB, los técnicos conectan un lector eMMC ISP a las pistas **CLK, CMD, DAT0, VCC (3.3V), VCCQ (1.8V) y GND** de la placa para reescribir directamente las particiones `bootloader`, `boot` y `super`.

---

## 5. Repositorios Descargados en `06_internet_y_repositorios/github_repos/`

Dentro de esta misma carpeta se han descargado los siguientes repositorios completos de código abierto:

1. **`mirko-geno__b866v2-debian`:** Proyecto específico para ejecutar Debian Linux en el **ZTE B866V2** (incluye guía de particiones y arranque).
2. **`panda4477__ZTE_B866V2-H-TPL`:** Plantilla y recursos para la variante **ZTE B866V2-H**.
3. **`eryzxsd__B866F-GTV-Project`:** Proyecto de personalización de **Google TV para la serie ZTE B866F (`B866V2F`)** con scripts de debloat, launcher y optimización.
4. **`An-rus__ZTE-ZXV10-B826C-A12-STB-Security-Research`:** Investigación de seguridad y hardware sobre el decodificador hermano **ZTE ZXV10 B826C-A12**.
5. **`superna9999__pyamlboot`:** Librería y herramientas oficiales en Python para comunicación por protocolo USB Boot de Amlogic (`boot-g12.py` para el chip `g12a` S905X2 de tu Xview).
6. **`Ayx03__ChashCode`:** Algoritmos y herramientas para generadores de códigos ADB en decodificadores **ZTE ZXV10**.
7. **`Explorer9952__b600` y `Explorer9952__zte-b700v5-binder-devkit`:** Documentación y utilidades para decodificadores ZTE ZXV10.
8. **`7Ji__amlogic-s9xxx-archlinuxarm`:** Herramientas de arranque para procesadores Amlogic S9xxx (`g12a`).
9. **`bg4hkq__meson-gxl-s905mb-p211`:** Volcados de Device Tree (`.dts` / `.dtb`) y particiones de decodificadores ZTE ZXV10.
10. **`ophub__amlogic-s9xxx-armbian_docs`:** Base de datos oficial de modelos Amlogic (`model_database.conf`) y guías de compilación de kernel para TV Boxes Amlogic.
