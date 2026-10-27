# Reporte de Ingeniería Inversa con REA: App Xuper (`Hydra` vs `Netxxus/XSairec`) y Firmware Xview (`ZTE B866V2` Build `20231106`)

Este documento contiene los resultados verificados mediante **REA (`rea-agents@6.8.0`)** e inspección binaria sobre los artefactos locales de tu carpeta `xview`.

---

## PARTE 1: Análisis de la App Xuper (`xuper hydra.apk` vs `com.xuper.netxxus.apk`)

En tu proyecto existen **dos variantes** del mismo paquete (`com.xuper.netxxus`, versión `4.34.4`, `versionCode: 43404`), basadas internamente en el motor **`com.interactive.brasiliptv` / `org.coolx.tvframework`** (la misma arquitectura de MagisTV / BrasilIPTV / CoolX):

1. **[`xuper hydra.apk`](../xuper%20hydra.apk)** (`35,936,833` bytes) — Versión base **"Xuper Hydra / Xuper TV"**.
2. **[`com.xuper.netxxus.apk`](./experimento/04_apks_dispositivo/com.xuper.netxxus.apk)** (`34,429,505` bytes) — Versión modificada y renombrada como **"XSairec / XSairec TV"** extraída directamente de `/data/app/.../com.xuper.netxxus.../base.apk` en tu caja Xview.

### 1.1 Identidad Criptográfica y Firmas APK (`inspect_apk_signature`)

| Propiedad | `xuper hydra.apk` (Original) | `com.xuper.netxxus.apk` (Modificada en Xview) |
| :--- | :--- | :--- |
| **ID de Evidencia REA** | `ev_db7b021929c44d44face50d205ff943ef84fa36b686bdcadc721f102704dc353` | `ev_45801760f8030197f26126f8fb3e6e8806e4ea766906240a970521cfa93dfc63` |
| **SHA-256 del APK** | `afa10a15c212d088a7b7f9148ddb679b1feb154d898db35274e832a347a0aeb5` | `d95c1f0f5be0d8928587484b742a1145af1c03797d4389461ae75dfe6508c738` |
| **Esquemas de Firma** | **v1 + v2 + v3** (`min_sdk: 24`) | **v1 + v2** (se eliminó el bloque `v3` al re-firmar) |
| **Archivo RSA (`META-INF`)** | `META-INF/NETXXUS.RSA` / `NETXXUS.SF` | `META-INF/XSAIREC.RSA` / `XSAIREC.SF` |
| **Sujeto / Emisor X.509** | `C=Netxxus, ST=Netxxus, L=Netxxus, O=Netxxus, OU=Netxxus, CN=Netxxus` | `C=MX, ST=CDMX, L=CDMX, O=XSairec, OU=Media, CN=XSairec` |
| **Serial del Certificado** | `7B55C26F06058A55` | `F75BA2EB65336AD4` |
| **Huella SHA-256 Cert** | `de4d40197b71dbb30d7632c5c0e6bce3033b2caa568999a414321e8ba54001bf` | `56ad56d58dc38735a1fd0e1887a4b3e826afa16996b1478b130af078e34a9d04` |
| **Integridad (`matches_apk`)** | `true` (`self_consistent: true`) | `true` (`self_consistent: true`) |

---

### 1.2 Desencriptación de `PORTAL_KEY` y Dominios Ocultos (`Lfc/b;->a` / Triple-DES)

Al desensamblar `classes.dex` (`Lfc/b;->a` en `codeOff=6908740`) y `classes2.dex` (`Lib/c` en `codeOff=2952804`), encontramos el algoritmo exacto con el que la app cifra sus servidores y su clave de portal:
1. Convierte la cadena hexadecimal a texto Base64 (`Lfc/a;->b`).
2. Decodifica el Base64 usando una tabla propia clonada de `sun.misc.BASE64Decoder` (`Ldc/a` / `Lec/c;->e`).
3. Desencripta con **`DESede/ECB/PKCS5Padding` (Triple-DES)** usando la llave maestra embebida en `Lib/c` (`offset=2952990`):
   - **Llave maestra 3DES:** `*&@!6d5d-c483-4720-bb29-785b8f321c^%`
   - **PORTAL_KEY en `AndroidManifest.xml`:** `346f53476b586674612b4b66324e497a6f726863434832554f6c37597433615275667a582f6e556b634f4b3538304f2b3748567573673d3d`
   - **`PORTAL_KEY` desencriptada:** **`568e194f-d170-4369-96e8-4a457f641880`** (`PROGRAM_TYPE = 26`, `coolx_type = "live"`).

#### Los 13 Dominios de Servidores Desencriptados de `resources.arsc`
Al aplicar el mismo descifrado `DESede/ECB/PKCS5Padding` sobre el pool de strings de `resources.arsc` (offsets `38457..39193`), descubrimos todos los servidores reales y cómo fueron bloqueados en la versión `XSairec`:

| # | Cadena Hex Cifrada (en `xuper hydra.apk`) | Dominio Real Desencriptado (`Hydra`) | Estado en `com.xuper.netxxus.apk` (`XSairec`) |
| :-: | :--- | :--- | :--- |
| 1 | `3170695a7437...` | **`zxiws.tcgwhnvym.com`** | Activo (`zxiws.tcgwhnvym.com`) |
| 2 | `45526d656c39...` | **`ernsm.prxmnvhcy.com`** | Reemplazado por **`0.0.0.0.invalid.000`** |
| 3 | `4d4161356c67...` | **`jetsb.hgwtqrpmo.com`** | Reemplazado por **`0.0.0.0.invalid.000`** |
| 4 | `4d512f325931...` | **`qkijv.rucvzndwa.com`** | Reemplazado por **`0.0.0.0.invalid.000`** |
| 5 | `53747a5a6372...` | **`vgwbm.uwfyobivh.com`** | Activo (`vgwbm.uwfyobivh.com`) |
| 6 | `566256575268...` | **`nxiqj.jgrqyxupl.com`** | Activo (`nxiqj.jgrqyxupl.com`) |
| 7 | `63766b6b7463...` | **`rokbd.ysrkwctjg.com`** | Activo (`rokbd.ysrkwctjg.com`) |
| 8 | `644a786e3543...` | **`eskna.ucpjdhivl.com`** | Reemplazado por **`0.0.0.0.invalid.000`** |
| 9 | `6641466b4332...` | **`zerif.qwcjdungi.com`** | Reemplazado por **`0.0.0.0.invalid.000`** |
| 10 | `6b6673704d4f...` | **`mptec.dhkrxuzcy.com`** | Activo (`mptec.dhkrxuzcy.com`) |
| 11 | `717a4d65706b...` | **`yvhcn.hxjebagrv.com`** | Reemplazado por **`0.0.0.0.invalid.000`** |
| 12 | `7351502b4a6f...` | **`fuxok.nguvmqhpk.com`** | Activo (`fuxok.nguvmqhpk.com`) |
| 13 | `795066316266...` | **`dfcsq.divqohamz.com`** | Activo (`dfcsq.divqohamz.com`) |

---

### 1.3 Parche Binario en `classes2.dex` (`Hydra` vs `Netxxus/XSairec`)

De los 2,371 archivos dentro del APK:
- `classes.dex` (`CRC: e2f783bd`), `classes3.dex` (`CRC: 82836114`), todos los `assets/` y las **25 librerías nativas `.so`** son **100% idénticos byte por byte** entre ambos APKs.
- En el código Java/Dalvik, **solo se modificaron 6 instrucciones de 2 bytes dentro de `classes2.dex`** en el método `Lib/d;-><clinit>` (`codeOff=2953472`), reemplazando el índice de string por `0x0262` (`"-"`):

| Offset en `classes2.dex` | Bytecode en `xuper hydra.apk` | Bytecode en `com.xuper.netxxus.apk` | Efecto del Parche |
| :--- | :--- | :--- | :--- |
| `2953496` | `1a 02 0a 59` (`"upgrade_main"`) | `1a 02 62 02` (`"-"`) | **Desactiva actualización automática principal** |
| `2953520` | `1a 02 09 59` (`"upgrade_backup"`) | `1a 02 62 02` (`"-"`) | **Desactiva actualización automática secundaria** |
| `2954002` | `1a 02 2d 29` (`"ad_main"`) | `1a 02 62 02` (`"-"`) | **Desactiva servidor de anuncios principal** |
| `2954028` | `1a 02 29 29` (`"ad_backup"`) | `1a 02 62 02` (`"-"`) | **Desactiva servidor de anuncios secundario** |
| `2954158` | `1a 02 91 2b` (`"bigbee_main"`) | `1a 02 62 02` (`"-"`) | **Desactiva telemetría/rastreo BigBee principal** |
| `2954184` | `1a 02 90 2b` (`"bigbee_backup"`) | `1a 02 62 02` (`"-"`) | **Desactiva telemetría/rastreo BigBee secundario** |

---

### 1.4 Librerías Nativas `.so` Analizadas con REA (`inspect_elf_notes` y `inspect_native_exports`)

| Librería Nativa | Arquitectura / Tamaño | IDs de Evidencia REA (`notes` / `exports`) | Hallazgos Clave de REA |
| :--- | :--- | :--- | :--- |
| **[`libranger-jni.so`](../libranger-jni.so)** | `ELF64` ARM64 (`8.23 MB`) | `ev_e65e101e...` / `ev_4d19c805...` | Motor nativo de streaming P2P/HTTP/HLS (`12HttpTsClient`, `9HlsClient`, `N3net4http9MixClientE`), parser JSON/XML (`yyjson`, `tinyxml2`), bundle propio de certificados raíz Mozilla NSS y pila TLS. Registra métodos vía `JNI_OnLoad` (`_Z4CallP7_JNIEnvP8_jobjectP8_jstringS4_`). |
| **[`libijkplayer.so`](../libijkplayer.so)** | `ELF64` ARM64 (`389 KB`) | `ev_07d016fa...` / `ev_8926fae5...` | `624` símbolos exportados (`ijkmp_*`, `ffpipeline_set_surface`, `ffpipenode_init_decoder_from_android_mediacodec`, `convert_hevc_nal_units`, `SoundTouch`). Conecta el reproductor con los decodificadores por hardware Amlogic MediaCodec. |
| **[`libtnet-3.1.14.so`](../libtnet-3.1.14.so)** | `ELF64` ARM64 (`362 KB`) | `ev_55c160f2...` / `ev_e338e720...` | `26` símbolos exportados (`NAL_session_Create`, `NAL_session_SubmitRequest`, `NAL_set_slightssl_get_publicKey_cb`). Motor de red SPDY/QUIC/HTTP2 de Alibaba/Taobao ACCS usado para push y control. |
| **[`libumeng-spy.so`](../libumeng-spy.so)** | `ELF64` ARM64 (`464 KB`) | `ev_679faca3...` / `ev_6ffe724e...` | `381` exportaciones totales, incluyendo 3 funciones JNI directas de fingerprinting de dispositivo: `Java_com_umeng_umzid_Spy_getNativeID`, `getNativeTag`, `getNativeLibraryVersion`. |
| **[`libcrashsdk.so`](../libcrashsdk.so)** | `ELF64` ARM64 (`647 KB`) | `ev_bed4ef89...` / `ev_114d7b64...` | `18` exportaciones (`UC CrashSDK` v3.2.3, linker `GNU gold 1.11`, Build-ID `ec31be11f4492f44e3061badd61b21552a2eeac9`). |
| **[`libboost_multidex.so`](../libboost_multidex.so)** | `ELF32` ARMv7 (`13.8 KB`) | `ev_8c51c0f3...` / `ev_fad70d4c...` | Librería de ByteDance (`BoostMultiDex`) con 5 exportaciones JNI (`Java_com_bytedance_boost_1multidex_BoostNative_loadDirectDex`, etc.). |

---

## PARTE 2: Análisis del Firmware Xview (`ZTE ZXV10 B866V2` Android 12 — Compilación `20231106`)

*(Nota: la fecha real de compilación en el firmware es **`2023`** — `eng.ztestb.20231106.184649` del `6 de noviembre de 2023`, con parche de seguridad `2023-10`)*.

### 2.1 Identificación del Sistema (`product_build.prop` y `dtbo.img`)
- **Dispositivo / Placa:** `ZTE ZXV10 B866V2` / Placa `franklin` (Referencia Amlogic `g12a_u212_2g`, SoC **Amlogic S905X2** Quad-Core Cortex-A53 + GPU Mali-G31 MP2).
- **Base de la ROM:** `DMC.v2.25` portada desde el firmware oficial **Viettel TV360 Android 12 (SDK 31)**:
  - **Fingerprint:** `Viettel/ViettelBox_B866V2/MediaBox_B866V2:12/SC/V84711310.1024:user/release-keys`
  - **Incremental:** `eng.ztestb.20231106.184649` (`Mon Nov 6 10:45:13 UTC 2023`)
- **Personalizaciones preinstaladas en el firmware:**
  - `com.slimbox.tvlauncher.overlay` (`/product/overlay/TVWall.apk`)
  - `org.cyanogenmod.appdrawer` (`/system/app/AppDrawer/AppDMC.apk`)
  - `net.itvplus.appstore` (`Hdplay.apk`), `com.dinhlap.dlstore` (`DLStore.apk`), `tv.upgrade.service` (`AppUpgradeService.apk`)
  - Servicios ZTE conservados: `com.zte.adb` (`ADBSetting.apk`), `com.zte.tv.act`, `com.zte.factorytesttool`, `com.zte.netmanager`, `com.nanosic.zte` (driver de control remoto por voz Bluetooth Nanosic).

---

### 2.2 Inspección Binaria de las 10 Imágenes de Partición en [`V2/`](./V2/B866v2%20Android%2012/B866v2%20Android%2012/B866V2)

| Imagen | Tamaño | SHA-256 | Formato y Contenido Interno Verificado |
| :--- | :--- | :--- | :--- |
| **`boot.img`** | `16.0 MB` (`16,777,216` B) | `80ea01a3cd790586...` | **Android Boot Image v2** (`ANDROID!`):<br>• **Kernel:** `9,571,516` B @ `0x01080000`<br>• **Ramdisk:** `1,128,607` B @ `0x01000000`<br>• **Second stage (DTB):** `174,080` B<br>• **OS Version:** `12.0.0` (Parche `2023-10`)<br>• **Cmdline:** `androidboot.dynamic_partitions=true androidboot.dtbo_idx=0 androidboot.boot_devices=ffe07000.emmc buildvariant=user` |
| **`bootloader.img`** | `1.77 MB` (`1,857,536` B) | `5e2d75b503898c22...` | **Imagen FIP / U-Boot cifrada/firmada de Amlogic `g12a`** (`BL2 + BL30 + BL31 + BL32 + BL33`). Se escribe también en `bootloader0` y `bootloader1`. |
| **`dtb.img`** | `256 KB` (`262,144` B) | `da5d342d327dea07...` | **Flattened Device Tree (`0xd00dfeed`)**:<br>• ID de placa: **`g12a_u212_2g`** (`amlogic, g12a`, `arm,cortex-a53`, `arm,mali-midgard`)<br>• Tablas DVFS de reloj (`125` a `850 MHz`) y 12 mapas de control remoto IR (`amlogic-remote-1..12`). |
| **`dtbo.img`** | `8.0 MB` (`8,388,608` B) | `446040aa48db036f...` | **DTBO (`0xd7b7ab1e`, firmado con `avbtool 1.2.0`)**:<br>• Contiene los overlays `dummy-battery` y `dummy-charger` y la huella `Viettel/ViettelBox_B866V2/MediaBox_B866V2:12/SC/V84711310.1024:user/release-keys`. |
| **`factory.img`** | `8.0 MB` (`8,388,608` B) | `4e94bc4a31f30ff1...` | **Partición `FAT12` (`mkfs.fat`, etiqueta `KEYBOX PART`)** montada en `/mnt/vendor/factory`:<br>• Contiene las llaves DRM de fábrica: **`WIDEVINE` (Widevine L1)**, **`PLAYREADY` (`PLAYRE~1/2`)**, **`HDCP_TX` (`HDCP_T~1/2`)** y **`KEYMASTER` (`KEYMAS~1`)**. *(¡Crítico: nunca borrar sin respaldo porque se pierde Widevine L1!)*. |
| **`param.img`** | `16.0 MB` (`16,777,216` B) | `048594b9fb17d123...` | **Sistema de archivos `ext4`** (`4096` bloques de `4 KB`, montado en `/mnt/vendor/param`, contexto SELinux `u:object_r:param_tv_file:s0`):<br>• Almacena calibración de video Amlogic (`SSMHandler`, `Bri_Con_HDR_Contrast_RefData`, `CM2_MPEG_HD`, `DEBLOCK_CVBS_PAL`). |
| **`conf.img`** | `4.0 MB` (`4,194,304` B) | `62f3dbf4d1402d03...` | Imagen de configuración ZTE (`MultiplexConfigFile`). |
| **`cri_data.img`** | `8.0 MB` (`8,388,608` B) | `f0bd6feb51ddecef...` | Partición de datos críticos de ZTE (MAC/Serial/parámetros de fábrica). |
| **`oem.img`** | `16.0 MB` (`16,777,216` B) | `080acf35a507ac98...` | Imagen en blanco (rellena de ceros `0x00`) usada para limpiar la partición `oem` del operador anterior durante el flasheo. |
| **`logo.img`** | `281 KB` (`287,800` B) | `2f6f6636f2e64aec...` | **Empaquetado de recursos de arranque Amlogic (`Ufw.jpeg`)**:<br>• Contiene el splash screen editado en `Adobe Photoshop 21.2 (Windows)` el `2024:02:24 11:26:46`. |

---

### 2.3 Herramientas de Flasheo Analizadas con REA (`inspect_pe_imports`, `inspect_pe_certificates`, `inspect_native_exports`)

REA analizó los binarios PE de 32 bits incluidos en el paquete de flasheo (`B866V2-FlashATV12 - Evoca.bat`):

1. **`adnl.exe`** (`2.43 MB`, `PE32 x86`, SHA-256: `74eff82e...`, Evidence ID: `ev_68b0b3dd...`):
   - Herramienta **Amlogic ADNL (Amlogic Download Protocol)** sin firma Authenticode (`certCount: 0`, `imphash: 55acd4792241160d501595793bfa9028`).
   - Importa 12 funciones de `AdbWinApi.dll` (`AdbOpenDefaultBulkReadEndpoint`, `AdbOpenDefaultBulkWriteEndpoint`, `AdbReadEndpointSync`, `AdbWriteEndpointSync`) para comunicarse con el SoC Amlogic por USB.
2. **`fastboot.exe`** (`1.77 MB`, `PE32 x86`, SHA-256: `c0b34133...`, Evidence ID: `ev_351f6644...` / `ev_b43158d6...`):
   - Exporta `941` símbolos (incluyendo primitivas criptográficas BoringSSL/OpenSSL `AES_*`, `BIO_*`) e importa `AdbWinApi.dll` y `WS2_32.dll`.
3. **`AmlLibusb.dll`** (Evidence ID: `ev_3f4495d7...`) y **`AmlUsbScanX3.dll`** (Evidence ID: `ev_1753d5d2...`):
   - Exportan las primitivas de bajo nivel del modo **MaskROM / WorldCup Device** de Amlogic: `_Aml_Libusb_bl2_boot@16`, `_Aml_Libusb_Password@16`, `_Aml_Libusb_get_chipinfo@20`, `_Aml_Libusb_Ctrl_RdWr@24` y `_AmlScanUsbX3Devices@8`.


---

## PARTE 3: Bitácora de Pruebas, Errores y Soluciones Técnicas (Metodología Paso a Paso)

Siguiendo la regla de documentar cada prueba, error y solución aplicada durante la investigación:

### Prueba 1: Ejecución directa de `npx rea-agents@latest` en Windows con Node.js `v20.18.0`
- **Comando ejecutado:**
  ```powershell
  npx -y rea-agents@latest inspect-apk-signature "c:\Users\CORSAIR\Desktop\xview\xuper hydra.apk" --json
  ```
- **Error obtenido:**
  - `npm warn EBADENGINE Unsupported engine { package: 'rea-agents@6.8.0', required: { node: '^22.23.4 || ^24.22.1 || >=26.12.1' }, current: { node: 'v20.18.0' } }`
  - `SyntaxError: The requested module 'node:zlib' does not provide an export named 'zstdDecompress' at dist/cli-runtime/cli.js:95`
- **Causa raíz:** `rea-agents@6.8.0` importa `zstdDecompress` y `zstdDecompressSync` desde `node:zlib` (funciones añadidas en Node.js 22.15+), mientras que el sistema cuenta con `Node.js v20.18.0`.

### Prueba 2: Primer intento de Shim ESM (`module.register`) para `node:zlib`
- **Acción:** Se creó un loader ESM (`zlib-shim.mjs`) con `register()` e interceptor `load(url, context, nextLoad)` verificando `typeof result.source === "string"`.
- **Error obtenido:** El mismo `SyntaxError` seguía ocurriendo porque en Node.js `v20.18.0`, `nextLoad()` devuelve `result.source` como un `Buffer` / `Uint8Array` para módulos ESM en lugar de un `string`.
- **Solución:** Se convirtió `result.source` mediante `Buffer.from(result.source).toString("utf8")` antes de aplicar el reemplazo.

### Prueba 3: Segundo intento de Shim ESM con `const zstdDecompress`
- **Acción:** Se inyectaron `const zstdDecompress = ...` al final del módulo cargado.
- **Error obtenido:**
  - `ReferenceError: Cannot access 'zstdDecompress' before initialization at dist/cli-runtime/cli.js:1018 (unzstd = promisify(zstdDecompress))`
- **Causa raíz:** En JavaScript, las declaraciones `const` no tienen *hoisting* (elevación), por lo que al ejecutarse `init_GpuPayloadCodec` antes del final del archivo se producía una zona muerta temporal (*TDZ*).
- **Solución definitiva:** Se reemplazó `const zstdDecompress = ...` por declaraciones de función `function zstdDecompress() { ... }` y `function zstdDecompressSync() { ... }`, las cuales sí se elevan al inicio del ámbito del módulo. Con esto, `rea-agents@6.8.0` funcionó en `Node.js v20.18.0`.

### Prueba 4: Uso de `--detail summary` en CLI de REA sin `--evidence_output`
- **Comando ejecutado:**
  ```powershell
  node --import $shim $rea inspect-apk-signature "xuper hydra.apk" --detail summary --json
  ```
- **Error obtenido:**
  - `{"code":"invalid_request","category":"invalid_input","details":{"issues":[{"path":["evidence_output"],"reason":"invalid_value","message":"Summary delivery requires --evidence_output so complete source Evidence remains available."}]}}`
- **Solución:** Se añadió `--evidence_output <archivo.json>`, logrando conservar tanto el resumen como el paquete completo de evidencia (`Evidence`) verificable.

### Prueba 5: Redirección `>` de PowerShell 5.1 en archivos JSON de exportación
- **Acción:** Se redirigió la salida de `inspect-native-exports` con `> exports_libranger-jni.so.json`.
- **Error obtenido:** PowerShell 5.1 codifica por defecto la redirección `>` en `UTF-16LE` con BOM (`\uFEFF`), lo que impide abrir el archivo directamente como UTF-8 estándar.
- **Solución:** Se procesaron los archivos leyendo explícitamente en `utf16le` y eliminando el BOM (`replace(/^\uFEFF/, "")`), o escribiendo con `fs.writeFileSync(..., "utf8")` / `Set-Content -Encoding UTF8`.

### Prueba 6: Desencriptación de `PORTAL_KEY` y dominios de `resources.arsc`
- **Intento 1 (Fallido):** Al encontrar la cadena `*&@!6d5d-c483-4720-bb29-785b8f321c^%` en `Lib/c` (`classes2.dex`) y las cadenas `AES/CBC/PKCS5Padding` y `DES` en el pool de strings del DEX, se probó desencriptar `PORTAL_KEY` y los 13 bloques hexadecimales de `resources.arsc` con `DES-ECB`, `DES-CBC`, `AES-128-ECB` y `AES-128-CBC` (tanto con la llave directa como con su `MD5`). Todos devolvieron error de padding (`results: {}`).
- **Intento 2 (Rastreo de llamadas Dalvik):** Se desensamblaron las instrucciones `invoke-static` (`0x71`) dentro de `Lib/c` (`2952804..2953472`), descubriendo la cadena de llamadas:
  `Lib/c` $\rightarrow$ `Ljb/b;->a` (`classes2.dex` @ `2973904`) $\rightarrow$ `Lfc/b;->a` (`classes.dex` @ `6908740`).
- **Intento 3 (Solución y desencriptación exitosa):** Al inspeccionar `Lfc/b;->a`, se descubrió que:
  1. Primero convierte de Hex a texto ASCII Base64 (`Lfc/a;->b`).
  2. Luego pasa tanto el texto Base64 como la propia llave `*&@!6d5d-c483-4720-bb29-785b8f321c^%` por un decodificador Base64 tipo `sun.misc.BASE64Decoder` (`Ldc/a` / `Lec/c;->e`) que mapea caracteres fuera del alfabeto Base64 (`*`, `&`, `@`, `!`, `-`, `^`, `%`) a `-1` (`0xFF`) para construir los 24 bytes de `DESedeKeySpec`.
  3. Finalmente aplica **`DESede/ECB/PKCS5Padding` (`des-ede3`)**, desencriptando con éxito el `100%` de los dominios y la `PORTAL_KEY` (`568e194f-d170-4369-96e8-4a457f641880`).
