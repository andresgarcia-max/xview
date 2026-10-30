# Bitácora Maestra de Pruebas, Opciones Válidas y Errores Descartados — Xview+ (`ZTE ZXV10 B866V2`)

> [!IMPORTANT]
> **Propósito de este documento (Regla de Memoria Persistente):**
> Este archivo registra **todas las pruebas reales en hardware, hallazgos criptográficos, opciones sin abrir la caja, aciertos y errores ya resueltos** sobre el decodificador **Megacable Xview+ (`ZTE ZXV10 B866V2`, SoC Amlogic S905X2 `g12a`, Android 9 `V82815415.0083` y Android 12 `V82815417.2033` / `V84711310.1024`)**, con el fin de **aprender de cada prueba, no volver a proponer opciones descartadas y no repetir errores conocidos**.

---

## 1. Opciones Descartadas y Errores Conocidos (NO Repetir)

### ❌ 1.1. Crear o modificar un archivo `ota.zip` / `update.zip` en memoria USB (`D:\ZTESTBUSB\ota.zip`) para instalar desde Recovery
- **Estado:** **DESCARTADO MATEMÁTICAMENTE** (Ver [`REPORTE_VALIDACION_FIRMWARE_Y_OTA_ZTE.md`](./experimento/11_VALIDACION_OTA_Y_FIRMWARE_ZTE/REPORTE_VALIDACION_FIRMWARE_Y_OTA_ZTE.md)).
- **Causa exacta:** El binario [`system/bin/recovery`](./experimento/11_VALIDACION_OTA_Y_FIRMWARE_ZTE/system_bin_recovery) ejecuta `verify_package()` validando una firma asimétrica **RSA-2048 PKCS#7 de archivo completo** contra el certificado público [`releasekey.x509.pem`](./experimento/11_VALIDACION_OTA_Y_FIRMWARE_ZTE/releasekey.x509.pem) (`SHA-256: 8c08c7c8538f0352bc46ab85a41f909af3f13c03da0863ed9bc86aacac9b9e78`) dentro de `/system/etc/security/otacerts.zip`.
- **Por qué no se puede falsificar:** En el firmware solo existe la llave pública de verificación; la llave privada RSA-2048 reside únicamente en los servidores internos de ZTE (Shenzhen). Cualquier `.zip` modificado falla la firma antes de descomprimirse.

### ❌ 1.2. Desbloquear ADB mediante el código de reto-respuesta de `com.zte.adb.apk` (`libencryCode.so`)
- **Estado:** **DESCARTADO SIN SERVIDOR ZTE** (Ver [`REPORTE_FORENSE_APKS_ZTE_IZZI_Y_CONF.md`](./experimento/10_ZTE_B820C_A15_LE_COMPLETO/07_analisis_apks_zte_hibrido/REPORTE_FORENSE_APKS_ZTE_IZZI_Y_CONF.md)).
- **Causa exacta:** Para `MarketCode = 4301` y `OperatorsCode = 5` (Megacable), [`lib_armeabi_libencryCode.so`](./experimento/10_ZTE_B820C_A15_LE_COMPLETO/07_analisis_apks_zte_hibrido/lib_armeabi_libencryCode.so) (`generateRandom` / `generateCheckData`) utiliza criptografía asimétrica RSA-2048 sobre un número aleatorio de sesión para enviar `UtilsInstance.getADbCmd("open")` al socket nativo `zxdb`.

### ❌ 1.3. Intentar seleccionar `Reboot to bootloader`, `Enter fastboot` o `Apply update from ADB` en el menú Recovery de la TV
- **Estado:** **OCULTO POR FIRMWARE EN `MarketCode 4301`**.
- **Evidencia capturada en vivo desde `/tmp/recovery.log` (`V82815417.2033`):**
  ```text
  MarketCode ==> 4301
  OperatorsCode ==> 5
  amlogic_ui.cpp init menu begin, market=[4301], operators=[5]
  checkCustomMenuActions: menuItem=[Reboot system now], false!
  checkCustomMenuActions: menuItem=[Reboot to bootloader], true!
  checkCustomMenuActions: menuItem=[Enter fastboot], true!
  checkCustomMenuActions: menuItem=[Apply update from ADB], true!
  checkCustomMenuActions: menuItem=[Apply update from SD card], false!
  checkCustomMenuActions: menuItem=[Apply update from Udisk], false!
  checkCustomMenuActions: menuItem=[Wipe data/factory reset], false!
  checkCustomMenuActions: menuItem=[Wipe cache partition], false!
  checkCustomMenuActions: menuItem=[Wipe param partition], true!
  checkCustomMenuActions: menuItem=[Mount /system], true!
  checkCustomMenuActions: menuItem=[Power off], false!
  checkCustomMenuActions: menuItem=[Set USB host], false!
  checkCustomMenuActions: menuItem=[Set USB device], false!
  ```
  *(Nota: `true!` en `checkCustomMenuActions` significa que `librecovery_ui_ext.so` oculta ese elemento del menú en pantalla).*

### ❌ 1.4. Conectar un cable USB Macho-a-Macho (o Macho-A a Tipo-C) directamente cuando ya está en el menú de Recovery (`otg_device=0`)
- **Estado:** **NO DETECTA NADA EN LA PC CUANDO EL KERNEL LINUX YA CARGÓ RECOVERY**.
- **Causa exacta comprobada en vivo por UART ([`INFORME_FORENSE_UART_TTL_SMARTTY_REA.md`](./experimento/13_CAPTURA_UART_TTL_SMARTTY/INFORME_FORENSE_UART_TTL_SMARTTY_REA.md)):**
  1. **Puerto físico:** El puerto **USB 3.0 (azul)** de la caja Xview+ es exclusivamente *Host*. Solo el **puerto USB 2.0 (blanco/negro, junto a la ranura MicroSD)** está conectado al controlador OTG `ff400000.dwc2_a`.
  2. **Forzado a Host en el Kernel (`otg_device=0`):** En el `cmdline` que U-Boot pasa al Kernel Linux (`otg_device=0`), el driver `dwc3` / `dwc_otg` imprime al segundo `5.30`:
     ```text
     [    5.295811@1] dwc3 ff500000.dwc3: Configuration mismatch. dr_mode forced to host
     [    5.301989@0] dwc_otg_driver_probe host only, not probe usb_otg!!!
     ```
  3. **Únicas ventanas donde el puerto USB 2.0 actúa como Device (Esclavo hacia la PC):**
     - En U-Boot durante la ventana de **1.5 segundos de `InUsbBurn`** (cuando `GPIOAO_3 == 0` al encender y espera paquetes `SOF` del driver *Amlogic WorldCup Device*),
     - Cuando U-Boot entra en modo **Fastboot** (`bl31 reboot reason: 0x7` $\rightarrow$ `g_dnl_register: g_dnl_driver.name = usb_dnl_fastboot`), o
     - Cuando Android normal tiene activada la **Depuración por USB (ADB)**.

### ❌ 1.5. Errores de `SmarTTY` y Driver USB-TTL `CP2102` (`USB\VID_10C4&PID_EA60\0001`) Ya Resueltos
1. **Error de `pnputil`: *"El hash para el archivo no está presente en el archivo de catálogo especificado"*:**
   - **Causa:** Al clonar o descargar [`Drivers_TTL/CP210x/silabser.inf`](./Drivers_TTL/CP210x/silabser.inf) con saltos de línea Linux (`LF`), cambia su hash SHA-1 y no coincide con el catálogo firmado [`silabser.cat`](./Drivers_TTL/CP210x/silabser.cat).
   - **Solución aplicada:** Se restauró `silabser.inf` a formato Windows `CRLF` (`10,453 bytes`, hash `CD5610A5B20340323714AD1BA203E97E99E80C9A` verificado con `CryptCATAdminCalcHashFromFileHandle` = `InCat: True`) y se protegió en [`.gitattributes`](./.gitattributes) como binario para que Git jamás altere sus saltos de línea.
2. **Error en `SmarTTY`: *"No connections found"* o velocidad distinta a `115200`:**
   - **Causa:** `SmarTTY.GUI.ConnectionSelectionWindow+ControllerImpl` solo muestra puertos `COM` activos en Windows y cruza `_KnownSerialPortSettings` mediante `SerialPortKey(Description, Number)`.
   - **Solución aplicada:** En [`INSTALAR_DRIVERS_TTL_Y_ABRIR_SMARTTY.bat`](./INSTALAR_DRIVERS_TTL_Y_ABRIR_SMARTTY.bat) se asigna `DeviceDescription = "Silicon Labs CP210x USB to UART Bridge"` y `BaudRate = 115200`.
3. **Caracteres basura `???????` y `gpio_keypad_down_cnt: 60` en la terminal UART:**
   - **Causa:** Falso contacto en el pin `GND` (Tierra) o botón físico de Reset presionado.
   - **Regla:** Conectar firmemente solo **3 pines**: `GND` $\leftrightarrow$ `GND`, `TX` $\leftrightarrow$ `RX`, `RX` $\leftrightarrow$ `TX` (jamás conectar `3.3V` ni `5V`).
4. **Lectura de `SmarTTY` en vivo sin desconectar ni bloquear el puerto `COM` (`COM3` / `COM8`):**
   - **Causa:** Un puerto serie `COM` en Windows es de acceso exclusivo; si otro proceso intenta abrir `COM8` mientras `SmarTTY.exe` está conectado, da error de acceso denegado.
   - **Acierto / Solución aplicada:** Se desarrolló [`experimento/14_MONITOREO_EN_VIVO_SMARTTY/monitor_smartty.ps1`](./experimento/14_MONITOREO_EN_VIVO_SMARTTY/monitor_smartty.ps1), que lee directamente la memoria virtual (`VirtualQueryEx` + `ReadProcessMemory`) del proceso `SmarTTY.exe` en tiempo real sin tocar el puerto `COM`.

### ❌ 1.6. Peligro de perder `Widevine L1` (Netflix/Disney+ HD) y MAC/Serial al ejecutar `B866V2-FlashATV12 - Evoca.bat` sin modificar
- **Causa:** El script original [`B866V2-FlashATV12 - Evoca.bat`](./V2/B866v2%20Android%2012/B866v2%20Android%2012/B866V2-FlashATV12%20-%20Evoca.bat) incluye:
  ```bat
  fastboot flash factory factory.img
  fastboot flash cri_data cri_data.img
  ```
- **Regla obligatoria:** **NUNCA** flashear `factory.img` ni `cri_data.img` sobre una caja sin respaldarlos antes, ya que `factory` (`KEYBOX PART` FAT12) guarda las claves DRM únicas (`Widevine L1` / `HDCP`) y `cri_data` / `conf` guardan la MAC (`C4:74:1E:...`) y número de serie (`SN`). Usar siempre [`FLASHEAR_B866V2_ANDROID12_SEGURO_SIN_PERDER_WIDEVINE.bat`](./experimento/11_VALIDACION_OTA_Y_FIRMWARE_ZTE/FLASHEAR_B866V2_ANDROID12_SEGURO_SIN_PERDER_WIDEVINE.bat).

### ❌ 1.7. Por qué una caja entra SIEMPRE sola a Recovery al encender aunque `/cache` esté sana (`GPIOAO_3 == 0`)
- **Evidencia capturada en UART (`U-Boot`):**
  ```text
  gpio: pin GPIOAO_3 (gpio 3) value is 0
  detect upgrade key
  InUsbBurn
  noSof
  sof timeout, reset usb phy tuning
  system = recovery,set system_mode=recovery
  ```
- **Causa exacta:** El pin `GPIOAO_3` (`gpio 3`) es el botón físico de `RESET / UPGRADE` (activo en bajo: `0` = presionado, `1` = suelto). Si el micro-switch físico está trabado/presionado al encender, U-Boot dispara `detect upgrade key` y fuerza siempre la entrada a Recovery. Al soltar/destrabar el botón, `GPIOAO_3` lee `value is 1`.

### ❌ 1.8. Por qué la placa se sale sola de Fastboot y carga Android normal (`watchdog_reboot` + `bootonce-bootloader` $\rightarrow$ `ztebsp: bootcmd=bootsys`) y Rechazo `lock state: 10001000`
- **Evidencia capturada en vivo en [`INFORME_SESION_FASTBOOT_WATCHDOG_Y_SOLUCION.md`](./experimento/14_MONITOREO_EN_VIVO_SMARTTY/INFORME_SESION_FASTBOOT_WATCHDOG_Y_SOLUCION.md):**
  1. **Motivo A (Rechazo por `lock state: 10001000`):** En `U-Boot 2015.01-g220e992 (Sep 05 2023)`, si `cb_oem_cmd()` falla al ejecutar `setenv lock 10100000;save`, la placa queda con `lock state: 10001000` (`info->lock_state = 1`). Todos los comandos `fastboot flash` son rechazados en milisegundos con `ERROR: device is locked, can not run this cmd.Please flashing unlock & flashing unlock_critical` y el script llega inmediatamente al `fastboot reboot` final.
  2. **Motivo B (Watchdog de hardware activo en Fastboot):** Mientras la placa está en `U-Boot 2015.01-g220e992` / Fastboot sin actividad USB (por ejemplo, esperando confirmación `S/N` o durante `timeout /t 2` y `timeout /t 4`), el temporizador Watchdog de hardware reinicia el SoC (`reboot_mode:::: watchdog_reboot`). Eso borra el registro volátil `bl31 reboot reason: 0x7`, y como en la partición `misc` solo había `bootonce-bootloader` (`command mark(uboot-command) not match bootonce-bootloader,don't execute.`) y no `boot-recovery` (`read boot-recovery from misc failed.`), U-Boot fuerza **`ztebsp: bootcmd=bootsys`** y carga Android normal.
- **Solución definitiva aplicada (Triple Blindaje Anti-Watchdog):**
  1. **En `SmarTTY` (consola `#` de Recovery antes de saltar a Fastboot):** Ejecutar:
     ```sh
     echo -n "boot-recovery" > /dev/block/misc ; sync ; reboot bootloader
     ```
     Así `BL31` mete la placa a Fastboot (`0x7`), pero en `/dev/block/misc` queda grabado `boot-recovery` como red de seguridad: si el Watchdog reinicia la placa, U-Boot lee `boot-recovery` y regresa a la consola root `#` de Recovery en lugar de cargar `bootsys`.
  2. **En [`FLASHEAR_B866V2_ANDROID12_SEGURO_SIN_PERDER_WIDEVINE.bat`](./experimento/11_VALIDACION_OTA_Y_FIRMWARE_ZTE/FLASHEAR_B866V2_ANDROID12_SEGURO_SIN_PERDER_WIDEVINE.bat):** Se eliminaron todos los `timeout` y confirmaciones posteriores; el script valida primero los archivos y espera en un bucle rápido `:WAIT_FASTBOOT` que engancha el dispositivo en `< 200 ms`, envía `fastboot getvar version`, `fastboot oem "watchdog off"`, y ejecuta tanto `setenv lock 10100000;save` como `fastboot flashing unlock_critical` y `fastboot flashing unlock`.
  3. **Script de retención indefinida [`RETENER_FASTBOOT_ANTI_WATCHDOG.bat`](./experimento/14_MONITOREO_EN_VIVO_SMARTTY/RETENER_FASTBOOT_ANTI_WATCHDOG.bat):** Mantiene la placa estacionada en Fastboot enviando pulsos USB `fastboot getvar version` cada 2 segundos para impedir que el Watchdog expire por inactividad.

---

## 2. Opciones Válidas para Entrar al Sistema SIN Abrir Ninguna Caja Xview (`ZTE B866V2`)

### ✅ Opción A: Llaves Cifradas en Memoria USB FAT32 ([`QADEBUG.ini`](./QADEBUG.ini) y [`QADEBUGTOOL.ini`](./QADEBUGTOOL.ini))
Con la caja cerrada y encendida en Android:
1. Insertar una memoria USB formateada en **FAT32** que tenga en su raíz los archivos [`QADEBUG.ini`](./QADEBUG.ini) y [`QADEBUGTOOL.ini`](./QADEBUGTOOL.ini) (o usar el generador multi-versión en [`experimento/12_KIT_USB_QADEBUG_Y_EXTRACCION/`](./experimento/12_KIT_USB_QADEBUG_Y_EXTRACCION)).
2. **Funcionamiento interno verificado por ingeniería inversa:**
   - `com.ztestb.basecomp.apk` (`RemoteStbCfgService->check_QADEBUG_ini` y `transString`) y `com.zte.factorytesttool.apk` (`TestToolBroadcast->decodeQADEBUGTOOL`, `IsStartApkService` y `SecureUtil->Decrypt`) validan la versión de firmware y activan el servicio de diagnóstico/fábrica al montar la USB (`zte.intent.action.MEDIA_MOUNTED`), volcando además logs cifrados en `LOG_ZTESTB/` (desencriptables con [`2_ANALIZAR_Y_DESENCRIPTAR_LOGS_EXTRAIDOS.bat`](./experimento/12_KIT_USB_QADEBUG_Y_EXTRACCION/2_ANALIZAR_Y_DESENCRIPTAR_LOGS_EXTRAIDOS.bat)).

### ✅ Opción B: Activar ADB desde Ajustes (Teclado USB / Voz) + [`LIBERAR_SIN_ABRIR_CAJA.bat`](./LIBERAR_SIN_ABRIR_CAJA.bat)
Para liberar cualquier caja cerrada o meterla en **Fastboot** sin desarmarla:
1. Con la caja encendida, abrir **Ajustes de Android** (con teclado USB presionando **`Win + I`** o botón de micrófono diciendo **"Abrir Configuración"**).
2. Ir a **Preferencias del dispositivo $\rightarrow$ Información $\rightarrow$ Compilación** y presionar **7 veces `OK`** para habilitar **Opciones de programador** y activar **Depuración por USB**.
3. Conectar la caja a la PC por el **puerto USB 2.0** (junto a MicroSD) o por red local Wi-Fi/Ethernet (`5555`) y ejecutar [`LIBERAR_SIN_ABRIR_CAJA.bat`](./LIBERAR_SIN_ABRIR_CAJA.bat).

---

## 3. Procedimiento Verificado por UART/TTL (`COM3` / `COM8` a `115200 8N1`) y Radiografía de Cajas Reales

Comprobado en vivo sobre dos unidades físicas `ZTE ZXV10 B866V2`:
1. **Unidad 1 (`SN: Z41220110213792` — Android 9 Pie `V82815415.0083`):**
   - SoC `Amlogic G12A Rev C (28:C - 40:2)`, `BL2 Jun 17 2020`, `U-Boot 2015.01-gc7c0e9b (Jul 08 2021)`, `AVB Verified using testkey`, 21 particiones individuales (`mmcblk0p01` a `mmcblk0p21`), `/cache` (`1000 MB`) montando limpio en `EXT4`.
2. **Unidad 2 (`SN: Z41220090388172`, `MAC: C4:74:1E:F6:BE:19` — Android 12):**
   - SoC `Amlogic G12A Rev E (28:E - 40:2)`, `BL2 May 12 2022`, `BL31 Oct 8 2022`, `BL32 ATOS-V2.4.4 Jul 31 2023`, `U-Boot 2015.01-g220e992 (Sep 05 2023 - 04:13:01)`, 16 particiones con `super` dinámico y `Hit any key to stop autoboot: 1` (`set_tfuart_com_enable`).
3. **Secuencia blindada desde consola root (`#`) en Recovery para entrar a Fastboot sin riesgo de Watchdog:**
   ```sh
   echo -n "boot-recovery" > /dev/block/misc ; sync ; reboot bootloader
   ```
   Teniendo previamente en espera [`FLASHEAR_B866V2_ANDROID12_SEGURO_SIN_PERDER_WIDEVINE.bat`](./experimento/11_VALIDACION_OTA_Y_FIRMWARE_ZTE/FLASHEAR_B866V2_ANDROID12_SEGURO_SIN_PERDER_WIDEVINE.bat) o [`RETENER_FASTBOOT_ANTI_WATCHDOG.bat`](./experimento/14_MONITOREO_EN_VIVO_SMARTTY/RETENER_FASTBOOT_ANTI_WATCHDOG.bat).
