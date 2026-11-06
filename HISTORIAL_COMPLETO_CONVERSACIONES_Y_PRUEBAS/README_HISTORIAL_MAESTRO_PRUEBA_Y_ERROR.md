# Historial Completo de Prueba y Error, Conversaciones y Programa Perfecto v4.1 — ZTE ZXV10 B866V2 (Megacable Xview+)

Este directorio preserva **hasta el último byte** de todas las sesiones, transcripciones completas (`.jsonl` crudos y versiones Markdown legibles), capturas de pantalla, bitácoras de ejecución en vivo (`BITACORA_EN_VIVO_EXE.log`, `SESION_FASTBOOT_B866V2.log`, `CAPTURA_EN_VIVO_SMARTTY.log`), ingeniería inversa de binarios y evolución del **[`PROGRAMA_PERFECTO_XVIEW_B866V2.exe`](../programa%20perfecto/PROGRAMA_PERFECTO_XVIEW_B866V2.exe)**.

---

## 1. Estructura de Conversaciones y Transcripciones Byte-por-Byte

Cada subcarpeta `CONVERSACION_<UUID>` contiene:
- **`TRANSCRIPCION_COMPLETA_LEIBLE.md`**: Exportación íntegra paso a paso de toda la conversación (mensajes del usuario, bloques de razonamiento interno `thinking`, llamadas a herramientas `tool_calls` y salidas completas).
- **`.system_generated/logs/transcript_full.jsonl`**: Archivo JSONL original sin truncar con absolutamente cada byte de entrada/salida de la sesión.
- **`.system_generated/logs/transcript.jsonl`**: Archivo JSONL compacto de la sesión.
- **`.system_generated/tasks/` y `.system_generated/steps/`**: Todos los logs de comandos ejecutados en vivo en PowerShell, Fastboot, ADB, ADNL y compilador C#.
- **`.user_uploaded/`**: Todas las fotografías y capturas de pantalla enviadas durante las pruebas reales con el decodificador y la TV.
- **`scratch/`**: Scripts y pruebas auxiliares en C# y PowerShell creados durante la investigación.

### Índice de Sesiones Incluidas:
1. **[`CONVERSACION_5ecd42aa-1b9a-4dda-aec7-a32c9730884f`](./CONVERSACION_5ecd42aa-1b9a-4dda-aec7-a32c9730884f/TRANSCRIPCION_COMPLETA_LEIBLE.md)** *(Sesión Principal — 32.38 MB)*:
   - Ingeniería inversa de `com.zte.adb` (`libencryCode.so` / reto QR RSA-2048), `ControlDevice.apk` (`libztebasecomp_jni.so`), `recovery.img` y `B866V2-FlashATV12 - Evoca.bat`.
   - Desarrollo y compilación de **`PROGRAMA_PERFECTO_XVIEW_B866V2.exe` (`v4.1`)** en [`programa perfecto/1_HERRAMIENTAS_MOTOR/ProgramaPerfecto.cs`](../programa%20perfecto/1_HERRAMIENTAS_MOTOR/ProgramaPerfecto.cs).
   - Auditoría en vivo del codificador liberado de referencia (`192.168.0.47:5555`, `MAC: 9C:2F:4E:44:3E:50`, `ro.build.display.id = DMC.v2.25`, `ViettelBox_B866V2`).
   - Prueba en vivo sobre caja `Z41221040106995`: salto externo de `fastbootd` (`PID_4EE7`) a `U-Boot Fastboot` (`PID_0D02`) sin abrir tornillos ni usar UART TTL, inyección completa de las particiones Android 12 + `super.img` (`1.88 GB`).
   - Diagnóstico y solución de causa raíz del foco verde sin HDMI post-flasheo (`loadaddr=0x1080000` vs `loadaddr=0x03080000` tras `ee_gate_off` en `bootm` y rescate por protocolo **`Amlogic DNL [TPL]` `USB\VID_1B8E&PID_C004`** mediante [`adnl.exe`](../programa%20perfecto/1_HERRAMIENTAS_MOTOR/adnl.exe)).
2. **[`CONVERSACION_68310553-cc6d-4e0b-9b90-04621bb444b3`](./CONVERSACION_68310553-cc6d-4e0b-9b90-04621bb444b3/TRANSCRIPCION_COMPLETA_LEIBLE.md)**:
   - Análisis de procesadores Amlogic (`S905X2` G12A vs `S905Y4` S4), protección contra brick por `bootloader.img` incompatible y preservación de `factory.img` (Widevine L1) y `cri_data.img` (MAC/Serie).
3. **[`CONVERSACION_40af6952-ad1f-4838-a11f-e3373e47a6f5`](./CONVERSACION_40af6952-ad1f-4838-a11f-e3373e47a6f5/TRANSCRIPCION_COMPLETA_LEIBLE.md)**:
   - Diagnóstico comparativo de controladoras USB xHCI entre Dell Latitude 7490 (`0KP0FT`) y ASUS ROG STRIX B550-A GAMING (`SkipBOSDescriptorQuery` / `usbflags`).
4. **[`CONVERSACION_5c0ba28a-4d92-4331-9ab9-ce864064c015`](./CONVERSACION_5c0ba28a-4d92-4331-9ab9-ce864064c015/TRANSCRIPCION_COMPLETA_LEIBLE.md)**:
   - Recopilación de repositorios, herramientas Amlogic (`pyamlboot`, `ADNL`, `Optimus`, `WorldCup`), device trees (`meson-g12a`) y firmwares de referencia.

---

## 2. Resumen Técnico de Hallazgos Clave (Prueba y Error)

### A. Salto de `Recovery fastbootd` (`PID_4EE7`) a `U-Boot Fastboot` (`PID_0D02`) 100% Sin TTL
- Cuando la pantalla azul `fastbootd` (letras rosas) está activa en la TV y el cable USB Macho-a-Macho está en el **puerto USB 2 trasero** (junto a MicroSD y HDMI), el equipo enumera como `USB\VID_18D1&PID_4EE7` (`is-userspace: yes`).
- Como `fastbootd` en espacio de usuario tiene `ro.boot.verifiedbootstate=green` (`locked`), rechaza `fastboot flash` sobre particiones físicas de arranque (`boot`, `bootloader`, `vbmeta`, `recovery`).
- Sin embargo, al enviar `fastboot reboot bootloader` (o seleccionar `Reboot to bootloader` en pantalla), `fastbootd` escribe `bootonce-bootloader` en el BCB (`misc`) y reinicia directamente hacia **U-Boot Fastboot (`USB\VID_18D1&PID_0D02`, `is-userspace: no`)**.
- En U-Boot Fastboot (`PID_0D02`), el comando `fastboot oem 'setenv lock 10100000;save` desbloquea el bootloader en caliente y permite escribir todas las particiones de Android 12 (`DMC.v2.25`), incluyendo `super.img` (`1.88 GB` en 11 bloques sparse).

### B. Estructura Exacta del Codificador Liberado (`DMC.v2.25`)
Validada contra el decodificador liberado de referencia (`192.168.0.47:5555`):
- **Propiedades de Sistema**:
  - `ro.build.display.id` = `DMC.v2.25`
  - `ro.product.name` = `ViettelBox_B866V2`
  - `ro.product.model` = `MediaBox_B866V2`
  - `ro.build.fingerprint` = `Viettel/ViettelBox_B866V2/MediaBox_B866V2:12/SC/V84711310.1024:user/release-keys`
  - `ro.oem.update.disabled` = `true` (Bloqueo total de OTA)
- **Aplicaciones Integradas en `super.img`**:
  - `/product/priv-app/TVLauncher/TVLauncher.apk` (`com.google.android.tvlauncher`)
  - `/product/overlay/TVWall.apk` (`com.slimbox.tvlauncher.overlay` — Tema visual Slimbox TVWall)
  - `/product/priv-app/Tubesky/Tubesky.apk` (`com.android.vending` — Google Play Store Android TV)
  - `/system/app/AppDrawer/AppDMC.apk` (`org.cyanogenmod.appdrawer` — AppDMC)
  - `/system/app/DLStore.apk` (`com.dinhlap.dlstore` — DLStore)
  - `/system/app/Hdplay/Hdplay.apk` (`net.itvplus.appstore` — HDPlay Store)
  - `/system/app/Filemanager.apk` (`com.hitv.explore` — Filemanager)
  - `/system/system_ext/priv-app/ControlDevice/ControlDevice.apk` (`com.control.device` — abre ADB TCP `:5555` en cada encendido)
- **Aplicaciones en `/data/app/`**:
  - `com.xuper.netxxus` (`com.xuper.netxxus.apk`)
  - `tv.mxlmovies.app` (`tv.mxlmovies.app.apk`)

### C. Causa Raíz del Foco Verde Sin Señal HDMI y Rescate por `ADNL / DNL` (`VID_1B8E&PID_C004`)
- **Causa**: En U-Boot de ZTE (`U-Boot 2015.01-g220e992`), el comando nativo de arranque `bootsys` ejecuta:
  `imgread kernel boot ${loadaddr}; bootm ${loadaddr}`
  donde la dirección nativa de ZTE es **`loadaddr=0x03080000`**, ya que el kernel comprimido (gzip) dentro de `boot.img` se descomprime hacia la dirección física `0x01080000` (`kernel loaded at 0x01080000, end = 0x028bb008`).
- Cuando se escribió una imagen `env_unlocked.img` sintética con `loadaddr=0x1080000` (`0x01080000`), `bootm 0x1080000` ejecutó primero `ee_gate_off ...` (apagando el reloj de video HDMI esperando que el kernel de Linux lo reactivara) y enseguida intentó descomprimir el kernel de `0x01080000` sobre sí mismo en `0x01080000`, corrompiendo el flujo gzip (`inflate() returned -3`), abortando el arranque de Linux y entrando automáticamente en **`update 1000` -> `DNL mode [TPL]` (`USB\VID_1B8E&PID_C004`)**.
- **Solución implementada en [`ProgramaPerfecto.cs`](../programa%20perfecto/1_HERRAMIENTAS_MOTOR/ProgramaPerfecto.cs)**:
  1. Se incorporó [`TryCatchAdnlDnlMode()`](../programa%20perfecto/1_HERRAMIENTAS_MOTOR/ProgramaPerfecto.cs#L477-L512) usando [`adnl.exe`](../programa%20perfecto/1_HERRAMIENTAS_MOTOR/adnl.exe) (`Amlogic USB DNL tool V2.6.3`) para detectar `VID_1B8E&PID_C004` (`DNL mode [TPL]`) en 1 ms, restaurar `loadaddr=0x03080000`, `bootcmd=bootsys`, `lock=10100000`, `saveenv` y reiniciar la caja hacia `DMC.v2.25`.
  2. Se actualizó [`env_unlocked.img`](../programa%20perfecto/5_FIRMWARE_SEGURO_B866V2/env_unlocked.img) con `loadaddr=0x03080000` y `bootcmd=bootsys` (`CRC32: 0xA7BDF97B`).
  3. En el `[PASO 3/4]` de Fastboot, se eliminó la sobreescritura sintética de `env` y `cache_ext4.img`, utilizando `fastboot oem "setenv loadaddr 0x03080000;setenv bootcmd bootsys;setenv lock 10100000;save"` + las 10 particiones base + `super.img`, idéntico al flujo seguro de `B866V2-FlashATV12 - Evoca.bat` (pero protegiendo siempre `factory.img` y `cri_data.img`).
