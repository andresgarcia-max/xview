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

### Prueba 7: Pérdida de comillas dobles en `node -e '...'` desde PowerShell 5.1
- **Acción:** Se ejecutó `node -e 'const r = JSON.parse(fs.readFileSync("...", "utf8"))'` para filtrar las evidencias de las APKs de ZTE.
- **Error obtenido:** `SyntaxError: missing ) after argument list` porque PowerShell 5.1 elimina las comillas dobles internas no escapadas antes de pasar el argumento al proceso `node.exe`.
- **Solución:** Se estandarizó la creación de scripts modulares `.mjs` en el directorio de trabajo temporal (`scratch/`) ejecutándolos mediante `node "<script.mjs>"`.

### Prueba 8: Truncamiento prematuro de clases DEX en `com.zte.adb.apk`
- **Acción:** En la primera inspección de `classes.dex` dentro de `com.zte.adb.apk`, se tomó `dexData.classes.slice(0, 60)` antes de filtrar por nombre de paquete.
- **Error obtenido:** El listado de clases de `com.zte.adb.apk` parecía vacío (`zteClasses: []`) porque las primeras 60 entradas de la tabla de clases correspondían a librerías ofuscadas de lectura QR (`Lcom/a/a/*` de ZXing).
- **Solución:** Se aplicó el filtro `/Lcom\/zte\/|Lcom\/android\/settings\/porting\//` antes del `slice()`, recuperando todas las clases `Lcom/zte/adb/*` (`AdbSetting`, `AdbSetting_amx`, `AdbSetting_atv9`, `Bizz`) y `Lcom/zte/android/productcfg/*` (`License_RSA_AES`, `ProductCfgService`).

### Prueba 9: Desalineación de los primeros 32 bytes al desencriptar archivos `.eini` de ZTE
- **Intento 1 (Parcial):** Al identificar el encabezado mágico `"45509440"` (8 bytes) y las constantes `k1 = 61851d4ed786ff73e100b61f36504786` y `k2 = d3bd0a6cf761bb9639bf5a2bf9c61fc6` en `.rodata` de `lib_einifile.so`, se desencriptó el payload a partir del byte `8`. El texto plano emergió desde el bloque 2, pero los primeros 32 bytes aparecían corruptos.
- **Causa raíz:** En el formato `.eini` de ZTE (`lib_einifile.so`), los bytes `0..7` son el magic `"45509440"`, los bytes **`8..23` (16 bytes) son el hash `MD5(ciphertext)`**, y el texto cifrado AES-128-CBC real comienza en el **byte `24`**. Como CBC se auto-sincroniza tras un bloque, incluir los 16 bytes del MD5 como si fueran el Bloque 0 corrompía los primeros 32 bytes.
- **Solución definitiva:** Se inició la desencriptación `AES-128-CBC` en el offset `24` (`buf.subarray(24)`), recuperando el `100%` del texto plano desde el byte 0 (`[Common]...`) y verificando que `MD5(buf.subarray(24)) === buf.subarray(8, 24)` en todos los archivos `.eini`.

### Prueba 10: Decodificación de `assets/license/key.dat` y desencriptación de `conf.img` (`MultiplexConfigFile`)
- **Hallazgo en `key.dat`:** Al restar `0x11` a cada byte hexadecimal de `key.dat`, los caracteres Base64 `'/'` (`0x2F`), `'O'` (`0x4F`) y `'o'` (`0x6F`) aparecían corruptos como `0x1F`, `0x3F` (`'?'`) y `0x5F` (`'_'`).
  - **Causa y solución:** El ofuscador de ZTE suma `1` a cada nibble hexadecimal de forma independiente sin acarreo (`(nibble + 1) & 0xF`), por lo que el nibble bajo `0xF` pasa a `0x0` sin incrementar el nibble alto. Al decodificar cada nibble mediante `(nibble - 1) & 0xF`, las 4 llaves RSA-1024 (`PrivateKey1`, `PrivateKey2`, `PublicKey1`, `PublicKey2`) se recuperaron íntegras en formato PEM.
- **Hallazgo en `conf.img`:** Al intentar desencriptar los sectores de `conf.img` (`MultiplexConfigFile` en `0x5800`) con la llave estática `k1`, los bloques no contenían texto ASCII.
  - **Causa y solución:** Se localizó y extrajo `/vendor/lib/lib_emmcfs.so` desde `super.img` (`0x419b6000`, `Evidence: ev_204a4307fddc2806bcba34f6a686af6e20933113b6918c8dcdb2a7c6423990fc`). Al desensamblar `FfsRead` (`0x2878`) y `__Flash_cid_read` (`0x27f4`), se demostró que mientras `lib_einifile.so` usa la llave estática `k1`, `/vendor/lib/lib_emmcfs.so` **vincula la llave AES-128-CBC al hardware físico de cada caja** leyendo los 16 bytes del registro `CID` de la memoria eMMC (`/sys/class/mmc_host/emmc/emmc:0001/cid`) como `Key` y manteniendo `k2` (`d3bd0a6cf761bb9639bf5a2bf9c61fc6`) como `IV`.

---

## PARTE 4: Análisis con REA de Llaves Criptográficas y Configuraciones ZTE / Megacable (`4301_5`)

### 4.1. Firma y Evidencia REA de las 8 APKs del Sistema ZTE y Librerías Nativas (`.so`)

Todas las aplicaciones de sistema ZTE extraídas en `experimento/04_apks_dispositivo/` están firmadas con el certificado maestro de plataforma ZTE:
- **Sujeto / Emisor:** `C=CN, ST=Shanghai, L=Shanghai, O=ZTE, OU=ZTE, CN=ZTE, emailAddress=android@zte.com.cn`
- **Certificate SHA-256:** `8363c8368a9bf6a9328c818c881efe5cf42adc674432c35d720283467d440e59`

| Artefacto (APK / Librería ELF) | SHA-256 | REA `evidence_id` | Rol en el ecosistema ZTE / Megacable |
| :--- | :--- | :--- | :--- |
| **`com.zte.adb.apk`** | `de826ad03d70dfaa7355f4842b12cea54f18a5cc57d067531847cb8398c5506e` | `ev_a7d3d02e04f93ba656eb86b29c3a386d21698d9580e85f66d47868727c1d878a` | Control y desbloqueo de ADB por desafío-respuesta RSA (`sys.zte.adb.random`) |
| **`com.zte.android.productcfg.apk`** | `1bd5bb05116cc5f299c0531c4959326ef3603caaf9a875bc6ffa9c01d58a209d` | `ev_3aaf1c291f9a875bc6ffa9c01d58a209d7b85ad05fb921ab7c779897` | Servicio AIDL de configuración de producto y validación RSA/AES de `unifykeys` |
| **`com.ztestb.basecomp.apk`** | `2c88c2e867761139df9b0c29e37eb11eb6a2f5de5f29004ab4cc32cb81d8d4a0` | `ev_a9998c7f110058d939cf7e79ec7fb100fceba38e0b88da4c2f9666b8696b695e` | Almacén de perfiles encriptados `.eini` por operador (`4301_5` Megacable) y `SECRETKEY` |
| **`com.ztestb.LauncherCustomize.apk`** | `fdd3ac91b642df4c373df1eb17313d87d04e03f267972ab63f76c6d99e850bc9` | `ev_eb2a7c9d281d50e6da3b4bc9ac4c8bcbdbf10c4028885f235d08edc3d5b30c5d` | Personalización por `MarketCode` / `OperatorCode` (`MarketUtil`) |
| **`com.zte.tv.act.apk`** | `c2a43e97125ab085cfcfab0926167d0b9b24ee86c576784718caee35d73eb5b7` | `ev_96d82b6b953e340cd063dcee3aedd53e3996984cb2824dffd0a7405de5b6260a` | Herramienta de diagnóstico de red, ping y pruebas HTTP |
| **`com.zte.factorytesttool.apk`** | `5dbe75dd52725216f45fb01c22f4b86b868582684eb12cb96fd7c72f713fb0fd` | `ev_63ff59e736d7310cb066d9d5766654934e8001465d681faed908bc10ec3e0917` | Pruebas de fábrica ZTE (USB, LED, Bluetooth, llave privada `b860h`) |
| **`com.zte.netmanager.apk`** | `afdc9bd384f60fc4e6fcbe77d810df720c28bb819cf3e2667a8a492e15a560b4` | `ev_cb65c953b0f29fe0d357414346dcebf6cfe9dfc08f304c55796b59a8adf34ab8` | Gestor de interfaces Ethernet / PPPoE / Wi-Fi / VLAN |
| **`com.ztestb.uploaddiagno.apk`** | `5e49e6b89ef1e77c976eb86f3cd181054db267cb0831db9ff51e84ef11a4f78c` | `ev_70e4b44cd9fbfbdd07a817539fd1b78f73dfdb28ba29fe81d5e813a831c13ab0` | Subida de diagnósticos y capturas a servidor ACS/TFTP |
| **`libencryCode.so`** (`com.zte.adb`) | `8916b7dc9c18a21e0f41f57e0f773b214957e5086425b780a835a9c927d9cf5a` | `ev_591ec81d5c4e15f958bc9272b821bf58e6248e74e4be36d6cf9358e07ee41be2` | Validador JNI del código de desbloqueo ADB con llave pública RSA para México (`4301`) |
| **`libzteaidlproductcfg.so`** (`productcfg`) | `4ec3b408126be9f396d54d409e3d5195fd2ae4258c9b5922170ee529dc85fb2b` | `ev_687312e58d26cb7db76d7253e63ab025216f14f4bd96eb1494957a293a23a3ab` | Motor nativo de configuración (`aes_key`, `encode_magic`, `FfsDecryption`) |
| **`lib_einifile.so`** (`basecomp`) | `de4bc96df8efcc7d68d6820abdc065debf4516e2bb9a7a8204fb7ec9666e1185` | `ev_bafabe6fb91d930cb5ef3e7c71eb3b8d9a5b699e08d6c1bd5b28b921dc2b549a` | Lector y desencriptador de archivos `.eini` (`FfsDecryption` + `MD5`) |
| **`libgetkey.so`** (`basecomp`) | `a27e4ff5295ca7e1581bf5fa93db11bd1d7c0b724de2ecfa77978e233900b695` | `ev_cb837cb2011f40423cc8983eb174208e16420b4bbab98c538b55d3dc6934bc81` | Gestor de `SECRETKEY` (`InitSecretKey`, `EncWithAes`, `DecWithAes`) |
| **`lib_emmcfs_fake.so`** (`basecomp`) | `b30431bf3ab4377c89f87285ca8fa12ed7667028f7eb0b4c145f643547be1479` | `ev_dc324cbe55cd1bf454c0c207cf82cb2859326bc9cf3606debaee8c824bc30a5a` | Emulador de sistema de archivos seguro sobre `/data/data/com.ztestb.basecomp/files/ztecfg/` |
| **`/vendor/lib/lib_emmcfs.so`** (`super.img`) | `62b17ba04e5fec8f6515f544cc45580dc5b4adf53c562b742f7554bb9c614199` | `ev_204a4307fddc2806bcba34f6a686af6e20933113b6918c8dcdb2a7c6423990fc` | Driver real de `/dev/block/conf` (`conf.img`) vinculado al `CID` de la eMMC |

---

### 4.2. Códigos de Mercado y Operador (`MarketCode` / `OperatorCode`) para México y Megacable

En `com.ztestb.LauncherCustomize.apk` (`Lcom/ztestb/util/MarketUtil;`) y `com.ztestb.basecomp.apk`, el sistema lee las propiedades `config.MarketCode` y `config.OperatorCode` (almacenadas en `/sys/class/unifykeys/` bajo los nombres `Market` y `Operator`):

| Código (`MarketCode_OperatorCode`) | Operador / País | Constante en `MarketUtil` | Cadena de Identificación de Producto |
| :--- | :--- | :--- | :--- |
| **`4301_5`** (`4301 5`) | **Megacable México (Xview+)** | `isMEXICO_MEGACABLE_OTT` | `"Megacable+Xview++B866V2_ZTE+B866V2_ZTE"` |
| **`4301_4`** (`4301 4`) | **Izzi México** | `isMEXICO_IZZI` | Perfil `assets/zte/4301_4/config.eini` |
| **`4301_6`** (`4301 6`) | **América Móvil / Claro Video México** | `isMEXICO_AMCO` | Perfil `assets/zte/4301_6/config.eini` |
| **`4301_3`** (`4301 3`) | **Totalplay México** | Perfil `4301_3` | Perfil `assets/zte/4301_3/config.eini` |
| **`4301_2`** (`4301 2`) | **México (Perfil genérico `4301_2`)** | Perfil `4301_2` | Perfil `assets/zte/4301_2/config.eini` |
| **`1301_2`** (`1301 2`) | **Viettel TV360 (Vietnam)** | `isViettel_OTT_12` | Perfil `assets/zte/1301_2/config.eini` |
| **`9999_1`** (`9999 1`) | **Internacional Genérico** | `isGeneralInternational` | Perfil base `assets/zte/config.eini` |

---

### 4.3. Catálogo Maestro de Llaves Criptográficas ZTE / Megacable Descubiertas

#### 1. Llave AES-128-CBC de Archivos `.eini` y Sistema de Configuración (`FfsDecryption` / `FfsEncryption`)
Localizada en la sección `.rodata` de `lib_einifile.so` (`0x5280`), `lib_emmcfs_fake.so` (`0x31a0`) y `libzteaidlproductcfg.so` (`0x7b08..0x7b28`):
- **Algoritmo:** `AES-128-CBC` (sin auto-padding estándar; relleno con bytes nulos `0x00` hasta múltiplo de 16).
- **AES-128 Key (`k1`, 16 bytes hex):**
  ```text
  61851d4ed786ff73e100b61f36504786
  ```
- **AES-128 IV (`k2`, 16 bytes hex):**
  ```text
  d3bd0a6cf761bb9639bf5a2bf9c61fc6
  ```
- **Estructura del archivo `.eini`:**
  - `Bytes 0..7` (8 bytes ASCII): Magic Header **`45509440`** (`3435353039343430`).
  - `Bytes 8..23` (16 bytes binarios): Checksum **`MD5(ciphertext)`** calculado sobre `Bytes 24..fin`.
  - `Bytes 24..fin`: Cifrado `AES-128-CBC` con `Key = 61851d4ed786ff73e100b61f36504786` e `IV = d3bd0a6cf761bb9639bf5a2bf9c61fc6`.

#### 2. Llaves Secretas y Contraseñas de Sistema en `assets/basecfg/data.eini` (`libgetkey.so`)
Al desencriptar `assets/basecfg/data.eini` (`216` bytes) dentro de `com.ztestb.basecomp.apk`, se obtuvieron las llaves maestras que `libgetkey.so` (`InitSecretKey`) carga en memoria para encriptar/desencriptar credenciales del STB:
```ini
[Common]
STBCFG_INIT_SYSPWD=654321
SystemPwd=12001
SECRETKEY=|!jFa8o4
pcKey=20_jD9!2y-dW8
UCap_Upper_TranslateMap=RSTUVWXYZPONMLKEDCBAQJIHGF
UCap_Lower_TranslateMap=rstuvwxyzponmlkedcbaqjihgf
```
- **`SECRETKEY` (`libgetkey.so`):** **`|!jFa8o4`**
- **`pcKey` (ZTE STB Configuration Tool):** **`20_jD9!2y-dW8`**
- **`SystemPwd` (Contraseña de configuración del sistema ZTE):** **`12001`**
- **`STBCFG_INIT_SYSPWD` (Contraseña inicial de fábrica):** **`654321`**

#### 3. Llave AES-128 de Campos de Configuración (`aes_key` en `libzteaidlproductcfg.so`)
Localizada en la tabla de símbolos dinámicos (`.dynsym` / `.data` @ `0x1062c`) de `libzteaidlproductcfg.so` y utilizada por las funciones `enc_string` (`0xf550`) y `dec_string` (`0xf614`):
- **Símbolo ELF:** `aes_key` (`st_value = 0x1262c`, `fileOff = 0x1062c`, `st_size = 16`)
- **Valor ASCII (16 bytes):** **`zte_stb_860av1.1`** (`7a74655f7374625f3836306176312e31`)
- **Símbolo `encode_magic` (`fileOff = 0x79c8`):** **`45509440`** (`0034353530393434`)

#### 4. Llave AES-128 y Llaves Privadas RSA-2048 de Licencia (`unifykeys`) en `com.zte.android.productcfg.apk`
En la clase `Lcom/zte/android/productcfg/License_RSA_AES;` (`<init>` @ `codeOff = 11644`), el servicio verifica la partición de llaves de hardware Amlogic (`/sys/class/unifykeys/{name,exist,read,write}` para `Licensekey`, `mac_en`, `stbid_en`, `Operator`, `Market`):
- **Llave AES-128 (`KEY`, `AES/ECB/PKCS5Padding`):** **`abcdefghij987654`**
- **Llave Pública RSA-2048 (`public_key_zte`):**
  ```text
  MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA01sdf1ZgfOZ3sImXR0PKZQtVdFE8g1l9NHI183NpYXdKmd3n/f229uk9v3wJ5c/wBff5cUVTuE75/eaM5bW0X/wG/wD/wH/wJ/wL/wN/wP...IDAQAB
  ```
- Además, `License_RSA_AES` contiene embebidas en texto plano dos llaves privadas RSA-2048 completas en Base64 PKCS#8 (`private_key_vp` que comienza con `MIIEogIBAAKCAQEA0iRBdhRiTpvNl4vfHmUQpCLRx8UuSGaSn3qqo9ZDbF4LCICM...` y `private_key_zte` que comienza con `MIIEowIBAAKCAQEA01sdf1ZgfOZ3sImXR0PKZQtVdFE8g1l9NHI183NpYXdKmd3n...`). Cuando la verificación falla, establece `sys.maincontrol.LICENSE_ERROR=1` y emite el broadcast `com.stbmc.maincontrol.LICENSE_ERROR.TO_UPGRADE`.

#### 5. Llave Pública RSA-2048 de Desbloqueo ADB para México / Megacable (`4301`) en `com.zte.adb.apk`
La aplicación `com.zte.adb.apk` (`Lcom/zte/adb/Bizz;` y `Lcom/zte/adb/AdbSetting;`) genera un número aleatorio en `sys.zte.adb.random` / `persist.sys.zte.adb.random` y lo combina con la MAC (`ro.product.mac`), `config.MarketCode` y `config.OperatorsCode`. La librería nativa `libencryCode.so` (`compare`) verifica la respuesta usando una llave pública RSA-2048 específica cuando `MarketCode == "4301"` (México - Megacable / Izzi / AMCO), ubicada en `.rodata` @ `0x133a` (justo después de la cadena `"4301"` en `0x1335`):
```text
-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAx3v2EWOA5oorr+I0wDHN
tnHQGCdDE4XBV+k84PCukFsNTOY9HQ7lPmBD517MNspogY17JKEijIbudmehmY6z
QsFMmlbdBvIX9SAOsVxg7Qu6zn7Kvr89GjL/pi5iKwC5bYvkl4mGMj9DlRD0CHCq
EbgEafGph0K6qSjn5e0eQ87MfLGa2QJzHZUXyI/WkReuxUqnhZ/5zTX+eiyYabn/
jj1mW+2zKtdRjgitDB7hBX36lAyHmU+i+l2i5jC24cys4yHCS0f76I90dBmgBnAo
dbyJTvSSZNp84BGWH3do/iz459jPQTd1UdmplUzw4UJEXnKLrEiHzbLMAg2DIFwY
uQIDAQAB
-----END PUBLIC KEY-----
```
- **Nota de arquitectura:** Al ser criptografía asimétrica RSA-2048, el APK sólo contiene la llave pública de verificación (`0x133a`); la llave privada correspondiente para firmar desafíos QR/ADB de `4301` reside únicamente en el generador interno de ZTE/Megacable. Sin embargo, una vez superada la validación (o mediante privilegios de sistema), `Lcom/zte/adb/q;->run` ejecuta:
  ```sh
  setprop service.adb.tcp.port 19555
  stop adbd
  start adbd
  setprop service.adb.tcp.port 5555
  ```
  Y en modo Fastboot, el script `B866V2-FlashATV12_Evoca_DESOFUSCADO.bat` desbloquea el bootloader mediante:
  ```bat
  fastboot oem 'setenv lock 10100000;save
  fastboot reboot bootloader
  ```

#### 6. Llaves Privadas y Públicas RSA-1024 en `assets/license/key.dat` (`com.ztestb.basecomp.apk`)
Decodificadas revirtiendo el desplazamiento de nibbles `(nibble - 1) & 0xF`:
- **`PublicKey1`:** `MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDh2K25w0jtSDu0KumJlN4ZXmRFIxcTe32xpbr7SkcsBRvo0bt/mB26fpzQacbk0+/hA2h+fp/U58eAgsQPeFu/KA34X8PJAgfPBG5AsizJ0pHPJ3UqBsp37eNokhBGgyWy2ifzwap5B9uCwTtX/SQ62h7rLUC1uZH4YowXeKyo8wIDAQAB`
- **`PublicKey2`:** `MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCy07BKt4CeQ9UBaWD+JLHngLZsQQZk7yLlam9YInay+kPq4USzztUivcvhVLn9TCrMumdbfQo2lNf4+gd/eZZF3L7Ur9Ijt4Jdg9SIdVs2gOajdDFXUNiWCosVeU532tfrHEqU/JTWg7CU8BKuXbS346UIdbQmxx2daRj24VRvhQIDAQAB`
- Incluye además sus respectivas llaves privadas RSA-1024 completas (`PrivateKey1` y `PrivateKey2`) junto con el archivo de licencia `assets/license/mutilsdk.lic` (`2561` bytes).

#### 7. Encriptación Vinculada por Hardware de `conf.img` (`/dev/block/conf` -> `MultiplexConfigFile`)
El análisis de `/vendor/lib/lib_emmcfs.so` (`super.img @ 0x419b6000`) demostró cómo se protege la partición `conf.img`:
- `conf.img` implementa un sistema de archivos flash con tabla de índice en `0x20000` (`\xff\xfe\x00IDX`), entradas de directorio en `0x20800..0x26800` (`\xff\xfe\x00FIL` para `MultiplexConfigFile`, revisiones `0` a `12`) y bloques de datos en `0x0000..0x6000` precedidos por 2 bytes mágicos `\xff\xfe`.
- Al leer o escribir `MultiplexConfigFile` (`FfsRead` @ `0x2878`, `FfsWrite` @ `0x2bb0`), `/vendor/lib/lib_emmcfs.so` invoca `__Flash_cid_read` (`0x27f4`), que lee `/sys/class/mmc_host/emmc/emmc:0001/cid` (el identificador único de 16 bytes del chip eMMC físico de la caja) y lo usa como **Key AES-128-CBC**, combinándolo con el **IV estático** `d3bd0a6cf761bb9639bf5a2bf9c61fc6`.

---

### 4.4. Contenido Íntegro Desencriptado del Perfil de Megacable (`4301_5`) y Otros Operadores Mexicanos

Utilizando la llave `k1` (`61851d4ed786ff73e100b61f36504786`) y el IV `k2` (`d3bd0a6cf761bb9639bf5a2bf9c61fc6`) a partir del byte `24`, se desencriptaron todos los perfiles `.eini` incluidos en `com.ztestb.basecomp.apk`:

#### 1. Perfil Megacable Xview+ (`assets/zte/4301_5/config.eini`, 248 bytes, `MD5: 58714daef7ae666a8ebfaa0a11474da3`)
```ini
[Common]
AcceptCookie=99
ACSAddress=https://www.xviewacs.com/live/CPEManager/CPEs/genericTR69
StartupLog=1
[Common_ext]
SetFirstUpFlag=1
EmergencyServer=http://l3gm3g.com:12306/hfs_dir/
[Android]
UpdateType=1
```

#### 2. Perfil Izzi México (`assets/zte/4301_4/config.eini`, 184 bytes, `MD5: 7d374004dd20dfa559213c06293120d3`)
```ini
[Common]
AcceptCookie=0
ACSAddress=https://acs-atv.izzi.mx/cwmp/
[Android]
UpdateType=1
[Common_ext]
SetFirstUpFlag=1
[Other]
STBToolConnectFac=0
```

#### 3. Perfil Totalplay México (`assets/zte/4301_3/config.eini`, 232 bytes, `MD5: 585b8dbed6a073d4eb66bbe313374690`)
```ini
[Common]
EnableCloseCaption=1
EraseLastFrame=1
OTTDRMSupplier=3
[Vmdrm]
OTTCA_VCISVRPort=80
OTTCA_Company=totalplay
OTTCA_VCISVRAddr=pubcasdrm.totalplay.com.mx:80
[Other]
STBToolConnectFac=0 
```

#### 4. Perfil América Móvil / Claro Video (`assets/zte/4301_6/config.eini`, 216 bytes, `MD5: fda216bb0811f0cb1eabf535be746f4c`)
```ini
[Common]
ACSAddress=https://amx.ztesmp.com:8081/register/ztesmp?TenantID=tenant_3b54d698b75a4db59239327e5775927e
ACSUserName=cpe
ACSPassword=cpe
[Common_ext]
SetFirstUpFlag=1
```

#### 5. Perfil México Genérico (`assets/zte/4301_2/config.eini`, 104 bytes, `MD5: 9e7c5a9b31cdccab694b08aa4e0aed7a`)
```ini
[Common]
AcceptCookie=99
DisableHDMIAudio=0
LogoType=0
MTU=1452
```

#### 6. Perfil Base ZTE (`assets/zte/config.eini`, 56 bytes, `MD5: 478e5171614459cc8bfa53bc5bd00bfb`)
```ini
[Other]
STBToolConnectFac=0
```

