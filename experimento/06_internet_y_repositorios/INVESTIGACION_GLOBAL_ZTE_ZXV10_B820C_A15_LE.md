# Investigación Global Exhaustiva: ZTE ZXV10 B820C-A15 LE (Hybrid Android TV STB)

Documento técnico recopilado a partir de registros regulatorios **FCC (`Q78-ZXV10B820CA15`)**, bases de datos de certificación de Google (**Android TV Guide / `1001tvs`**), repositorios de **GitHub**, foros internacionales (**XDA Developers, 4PDA, Capa9, AVPasión, YoReparo**) y documentación técnica de operadores (**izzi telecom** en México y **Entel** en Chile), incluyendo comparativa directa de arquitectura contra el **ZTE ZXV10 B866V2 (Xview+)**.

---

## 1. Identidad del Dispositivo y Significado de la Nomenclatura

| Campo | Valor Oficial / Técnico |
| :--- | :--- |
| **Fabricante** | ZTE Corporation |
| **Familia / Serie** | `ZXV10` (Línea de Set-Top Boxes IP/Híbridos de ZTE) |
| **Modelo Comercial** | **ZXV10 B820C-A15** / **B820C-A15 LE** |
| **Codename en Android (`ro.product.model`)** | `B820C-A15_ZTE` / `B820C-A15` |
| **FCC ID** | **`Q78-ZXV10B820CA15`** (también registrado bajo `Q78-B820CA15`, certificado el 18 de marzo de 2020) |
| **Operador Principal** | **izzi telecom** (México — comercializado como **izzitv Smart**) / variantes regionales en Latinoamérica (**Entel** Chile / Claro) |
| **Tipo de Equipo** | **Decodificador Híbrido (Hybrid Set-Top Box)**: **DVB-C (QAM por Cable Coaxial RF) + IPTV/OTT (Android TV)** |

### Desglose del código `ZXV10 B820C-A15 LE`:
* **`B8xx`**: Generación de STB UHD 4K de 64 bits de ZTE (misma generación que `B860H`, `B826C` y `B866V2`).
* **`20C` / `26C`**: La letra **`C`** indica sintonizador **Cable (DVB-C / QAM por puerto coaxial RF)** integrado en placa, a diferencia de la serie **`B866`** que es puramente **OTT/IPTV** (sin entrada coaxial).
* **`-A15`**: Subvariante de hardware y diseño de placa específica para el cliente (izzi telecom) con **SoC Amlogic S905X2** (a diferencia del `B826C-A12` de Megacable que emplea **HiSilicon Hi3798M**).
* **`LE`**: Sufijo de etiqueta física de manufactura de ZTE (*Lead-Free / RoHS Compliant Edition* o revisión de lote *Lite/Low-Energy* en fuente de 12V).

---

## 2. Especificaciones Técnicas de Hardware (SoC, Memoria, RF y Conectividad)

A pesar de tener chasis con sintonizador coaxial híbrido, el corazón digital del **ZTE ZXV10 B820C-A15 LE** comparte el **mismo SoC Amlogic S905X2 (`meson-g12a`)** que el **ZTE ZXV10 B866V2 (Xview+)**, pero con diferencias críticas en **RAM (DDR3 vs DDR4)**, **almacenamiento eMMC** y **periféricos RF**:

| Componente | Especificación en el ZTE ZXV10 B820C-A15 LE | Comparativa vs. ZTE ZXV10 B866V2 (Xview+) |
| :--- | :--- | :--- |
| **SoC (Procesador)** | **Amlogic S905X2** (`meson-g12a`, 12 nm) | **Idéntico** (`Amlogic S905X2`) |
| **CPU** | Quad-Core **ARM Cortex-A53** @ hasta **1.8 GHz** (64-bit `aarch64`, 18,400 DMIPS) | **Idéntico** |
| **GPU** | **ARM Mali-G31 MP2** (hasta 850 MHz, OpenGL ES 3.2, Vulkan 1.1) | **Idéntico** |
| **Memoria RAM** | **2 GB DDR3** | **2 GB DDR4** (`UnilC` en rev `STBAR_C` / `CXMT` en rev `STBAR_J`) |
| **Almacenamiento Flash** | **4 GB eMMC** (en lotes iniciales izzi) / **8 GB eMMC** (según Android TV Guide) | **8 GB eMMC** (`SanDisk` o `Samsung KLM8G1GETF`) |
| **Sintonizador Híbrido** | **Entrada Coaxial RF (Tipo F)** para TV lineal digital por cable (**DVB-C / ITU-T J.83 Annex B QAM**) | **No tiene** (100% IPTV/OTT) |
| **Wi-Fi** | **Wi-Fi 5 (802.11 a/b/g/n/ac) Dual-Band (2.4 GHz / 5.0 GHz) 2x2 MIMO** (20/40/80 MHz con soporte DFS/TPC en bandas UNII) | Wi-Fi 5 Dual-Band 2x2 (`MediaTek MT7668RSN` / `MT7661RSN`) |
| **Bluetooth** | **Bluetooth 4.2 + BLE** (2402–2480 MHz, para control remoto por voz y mandos) | Bluetooth 4.2 / 5.0 (`MT7668RSN` / `MT7661RSN`) |
| **Salidas de Video/Audio** | **1x HDMI 2.1 (TMDS 4K@60fps 10-bit HDR10/HLG)** + **1x Salida Mini-AV analógica (3.5 mm)** + Audio **Dolby Digital** | 1x HDMI 2.1 + 1x Mini-AV / SPDIF |
| **Puertos Físicos** | **1x USB 2.0**, **1x RJ-45 Ethernet (10/100 Mbps)**, **1x RF In (Coaxial)**, **Botón físico de encendido**, Entrada DC **12V** | **2x USB (1x USB 2.0 OTG + 1x USB Host)**, 1x MicroSD, 1x RJ-45, 1x Reset |

> [!WARNING]
> **Incompatibilidad directa de `bootloader.img` (BL2) entre B820C-A15 y B866V2:**
> Aunque ambos usan el SoC **Amlogic S905X2**, el **B820C-A15 LE** utiliza memoria **DDR3**, mientras que el **B866V2** usa **DDR4** (`g12a_u212_2g`). Flashear directamente el `bootloader.img` de un `B866V2` sobre un `B820C-A15` provocará fallo de inicialización de RAM en `BL2` (brick recuperable únicamente por puente **MaskROM / ISP**).

---

## 3. Ecosistema de Software, Middleware y Seguridad

1. **Sistema Operativo y Evolución de Compilaciones**:
   * Lanzado originalmente con **Android TV 9.0 (Pie)** en 2020 y actualizado vía OTA por el operador a **Android TV 10** y **Android TV 12** (compilaciones conocidas como `2056` y `2060`).
2. **Middleware de Operador (`Mirada Iris` + `Nagra` + `Widevine L1`)**:
   * A diferencia de Megacable (que utiliza plataforma Xview+ / ZTE IPTV), **izzi telecom** integra sobre Android TV el middleware **Mirada Iris** junto con seguridad condicional **Nagra** y **Widevine Level 1** (certificado por Google, Netflix y Amazon Prime Video).
   * El flujo de video híbrido combina:
     * **Canales en vivo (Live TV)**: Entran por el cable coaxial RF (**QAM/DVB-C**) desencriptados por hardware/CAS, sin consumir ancho de banda de internet.
     * **VOD, Replay, StartOver y Apps (Netflix, YouTube, Prime Video, Vix)**: Entran por IP (**Ethernet / Wi-Fi**) usando el reproductor ExoPlayer/Codec2 de Amlogic con **Widevine L1**.
3. **Bloqueos Impuestos en el Firmware de izzi**:
   * **Bloqueo de Opciones de Desarrollador**: En las compilaciones OTA recientes (`2056`/`2060`), al pulsar 7 veces sobre *Compilación (Build)* en *Ajustes → Preferencias del dispositivo → Información*, el sistema muestra que **la cuenta/perfil del usuario está restringido por el administrador** o simplemente ignora la acción.
   * **Launcher Propietario Forzado**: Si el decodificador no detecta aprovisionamiento activo en la red del operador al iniciar, el launcher de izzi/Mirada intenta bloquear la pantalla principal pidiendo activación o conexión coaxial/IP.
   * **Restricción en el Puerto USB**: El firmware limita la enumeración de ciertos hubs USB o adaptadores de almacenamiento/red, aunque **sí reconoce teclados USB HID estándar** (indispensables para el modo Recovery).

---

## 4. Hallazgos en GitHub, Foros (XDA, 4PDA, Capa9) y YouTube

### A. GitHub (`An-rus/ZTE-ZXV10-B826C-A12-STB-Security-Research` y repositorios Amlogic)
* En GitHub no existe un repositorio exclusivo con el nombre `B820C-A15`, pero sí está documentada su arquitectura hermana en dos vertientes (ambas incluidas localmente en [`experimento/06_internet_y_repositorios/github_repos/`](./github_repos)):
  1. **Arquitectura de bloqueo ZTE Híbrido (`An-rus/ZTE-ZXV10-B826C-A12-STB-Security-Research`)**: Documenta que los decodificadores híbridos `ZXV10 B82xC` aplican el bloqueo principalmente en la capa de software (`build.prop` con `ro.adb.secure=1`, launcher personalizado y desactivación de Settings) y expone los puntos de prueba **UART a `115200 8N1` (3.3V)** en la placa base para interceptar **U-Boot**.
  2. **Arquitectura de SoC Amlogic S905X2 (`mirko-geno/b866v2-debian` y `superna9999/pyamlboot`)**: Al llevar el SoC **Amlogic S905X2 (`meson-g12a`)**, el `B820C-A15` posee en su BootROM el modo **WorldCup / MaskROM USB (`VID_1B8E&PID_C003`)**, accesible puenteando el pin `CLK` de la memoria eMMC a `GND` durante el encendido.

### B. XDA Developers (Hilo: *"How to Activate Developer Options in Izzi zte zxv10 b820c-a15?"*)
* En el hilo principal de XDA (`t/4241699`), usuarios de México documentan que:
  * En firmwares iniciales (Android 9), pulsar 7 veces en *Compilación* aún permitía activar **Opciones de Desarrollador** y **Depuración USB / Red (`adb connect <IP>:5555`)**.
  * Tras las actualizaciones OTA de izzi (Android 10 / Android 12), el operador bloqueó el switch de *Opciones de Desarrollador* mediante políticas de dispositivo (`DevicePolicyManager` / `UserManager.DISALLOW_DEBUGGING_FEATURES`) y ocultó el acceso directo a Google Play Store desde el launcher de izzi.

### C. Procedimientos de Comunidad en YouTube y Foros (Capa9, YoReparo, AVPasión)

#### 1. Acceso al Menú Recovery (Hard Reset con Teclado USB)
Cuando el `B820C-A15 LE` se queda congelado en el logo, lento o bloqueado:
1. Desconectar el cable de corriente (**12V DC**).
2. Conectar un **teclado USB alámbrico** en el único puerto USB 2.0 trasero/lateral.
3. Mantener presionado el **botón físico de encendido (Power)** en el chasis del decodificador mientras se vuelve a enchufar el cable de corriente.
4. Seguir presionando el botón durante **~25 a 30 segundos** hasta que el LED frontal cambie de **rojo a verde/parpadeo** y aparezca en pantalla el menú **Android System Recovery**.
5. Como el control remoto Bluetooth/IR no responde en el Recovery nativo, usar las **flechas del teclado USB** y la tecla **Enter** para seleccionar:
   * `Wipe data / factory reset`
   * `Wipe cache partition`
   * `Reboot system now`

#### 2. Bypass del Launcher de Operador sin Root (Método Asistente de Voz / Cuenta Google)
En unidades donde el servicio de cable fue cancelado pero el equipo sigue funcionando por Wi-Fi:
* **Acceso a Google Play Store / Ajustes Nativos**: Usando el botón de **Google Assistant (Micrófono)** del control remoto (o presionando teclas de función / atajos desde un teclado USB con tecla `Windows+B` o `Settings`), se puede dictar *"Abrir Google Play Store"*, *"Abrir YouTube"* o *"Abrir Configuración"*.
* **Remapeo de Botones y Launcher Alternativo**: Instalando **Button Mapper** y un launcher ligero como **Projectivy Launcher** o **FLauncher** desde Play Store, se configura el servicio de **Accesibilidad** de Android TV para que al presionar el botón `Home` o `Guía` abra `Projectivy Launcher` en lugar de la interfaz bloqueada de izzi.
* **Desactivación de Bloatware vía ADB (si tiene firmware con ADB habilitado)**:
  ```bash
  adb connect <IP_DEL_DECO>:5555
  # Listar paquetes del operador (ZTE / Mirada / izzi)
  adb shell pm list packages | grep -iE "zte|mirada|izzi|nagra"
  # Deshabilitar launcher/agente del operador para el usuario 0
  adb shell pm disable-user --user 0 <nombre_del_paquete>
  ```

---

## 5. Resumen de Recursos y Enlaces de Referencia Global

* **Certificación FCC (`Q78-ZXV10B820CA15`)**: Fotos internas de PCB, fotos externas, manual de usuario y reportes RF 2.4/5 GHz + BLE registrados por ZTE Corporation (`https://fccid.io/Q78-ZXV10B820CA15` / `https://device.report/q78-zxv10b820ca15`).
* **Ficha en Android TV Guide**: Especificaciones certificadas por Google para **izzi TV (ZTE B820C-A15)** con SoC Amlogic S905X2, 2 GB RAM, Widevine L1 (`https://www.androidtv-guide.com/pay-tv-provider/izzi-tv-b820c-a15/`).
* **Soporte Oficial izzi**: Guía de puertos, indicadores LED y configuración del **ZTE B820C-A15** (`https://ayudaizzi.izzi.mx/hc/es-mx/articles/6936490765082-Conoce-tu-decodificador-ZTE-B820C-A15`).
* **Hilo Técnico en XDA Developers**: Discusión sobre activación de *Developer Options* y restricciones de firmware en el **ZTE ZXV10 B820C-A15** (`https://xdaforums.com/t/how-to-activate-developer-options-in-izzi-zte-zxv10-b820c-a15.4241699/`).
