# Bitácora Maestra de Pruebas, Opciones Válidas y Errores Descartados — Xview+ (`ZTE ZXV10 B866V2`)

> [!IMPORTANT]
> **Propósito de este documento (Regla de Memoria Persistente):**
> Este archivo registra **todas las pruebas reales en hardware, hallazgos criptográficos, opciones sin abrir la caja y errores ya resueltos** sobre el decodificador **Megacable Xview+ (`ZTE ZXV10 B866V2`, SoC Amlogic S905X2 `g12a`, Android 12 `V82815417.2033` / `V84711310.1024`)**, con el fin de **no volver a proponer opciones descartadas ni repetir errores conocidos**.

---

## 1. Opciones Descartadas y Errores Conocidos (NO Repetir)

### ❌ 1.1. Crear o modificar un archivo `ota.zip` / `update.zip` en memoria USB (`D:\ZTESTBUSB\ota.zip`) para instalar desde Recovery
- **Estado:** **DESCARTADO MATEMÁTICAMENTE** (Ver [`REPORTE_VALIDACION_FIRMWARE_Y_OTA_ZTE.md`](file:///c:/Users/CORSAIR/Desktop/xview/experimento/11_VALIDACION_OTA_Y_FIRMWARE_ZTE/REPORTE_VALIDACION_FIRMWARE_Y_OTA_ZTE.md)).
- **Causa exacta:** El binario [`system/bin/recovery`](file:///c:/Users/CORSAIR/Desktop/xview/experimento/11_VALIDACION_OTA_Y_FIRMWARE_ZTE/system_bin_recovery) ejecuta `verify_package()` validando una firma asimétrica **RSA-2048 PKCS#7 de archivo completo** contra el certificado público [`releasekey.x509.pem`](file:///c:/Users/CORSAIR/Desktop/xview/experimento/11_VALIDACION_OTA_Y_FIRMWARE_ZTE/releasekey.x509.pem) (`SHA-256: 8c08c7c8538f0352bc46ab85a41f909af3f13c03da0863ed9bc86aacac9b9e78`) dentro de `/system/etc/security/otacerts.zip`.
- **Por qué no se puede falsificar:** En el firmware solo existe la llave pública de verificación; la llave privada RSA-2048 reside únicamente en los servidores internos de ZTE (Shenzhen). Cualquier `.zip` modificado falla la firma antes de descomprimirse.

### ❌ 1.2. Desbloquear ADB mediante el código de reto-respuesta de `com.zte.adb.apk` (`libencryCode.so`)
- **Estado:** **DESCARTADO SIN SERVIDOR ZTE** (Ver [`REPORTE_FORENSE_APKS_ZTE_IZZI_Y_CONF.md`](file:///c:/Users/CORSAIR/Desktop/xview/experimento/10_ZTE_B820C_A15_LE_COMPLETO/07_analisis_apks_zte_hibrido/REPORTE_FORENSE_APKS_ZTE_IZZI_Y_CONF.md)).
- **Causa exacta:** Para `MarketCode = 4301` y `OperatorsCode = 5` (Megacable), [`lib_armeabi_libencryCode.so`](file:///c:/Users/CORSAIR/Desktop/xview/experimento/10_ZTE_B820C_A15_LE_COMPLETO/07_analisis_apks_zte_hibrido/lib_armeabi_libencryCode.so) (`generateRandom` / `generateCheckData`) utiliza criptografía asimétrica RSA-2048 sobre un número aleatorio de sesión para enviar `UtilsInstance.getADbCmd("open")` al socket nativo `zxdb`.

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

### ❌ 1.4. Conectar un cable USB Macho-a-Macho (o Macho-A a Tipo-C) directamente sin activar antes ADB o Fastboot
- **Estado:** **NO DETECTA NADA EN LA PC SI NO ESTÁ EN MODO DEVICE / FASTBOOT / ADB**.
- **Causa exacta comprobada en vivo:**
  1. **Puerto físico:** El puerto **USB 3.0 (azul)** de la caja Xview+ es exclusivamente *Host*. Solo el **puerto USB 2.0 (blanco/negro, junto a la ranura MicroSD)** está conectado al controlador OTG `ff400000.dwc2_a`.
  2. **Estado por defecto:** En arranque normal y en Recovery (`init.recovery.amlogic.rc`), `sys.usb.config` vale `none` y `sys.usb.controller` está vacío, por lo que la PC no detecta ningún dispositivo USB hasta que:
     - Se activa **Depuración USB (ADB)** en Android, o
     - La caja entra en modo **Fastboot (`bl31 reboot reason: 0x7`)** o **Amlogic WorldCup**.

### ❌ 1.5. Errores de `SmarTTY` y Driver USB-TTL `CP2102` (`USB\VID_10C4&PID_EA60\0001`) Ya Resueltos
1. **Error de `pnputil`: *"El hash para el archivo no está presente en el archivo de catálogo especificado"*:**
   - **Causa:** Al clonar o descargar [`Drivers_TTL/CP210x/silabser.inf`](file:///c:/Users/CORSAIR/Desktop/xview/Drivers_TTL/CP210x/silabser.inf) con saltos de línea Linux (`LF`), cambia su hash SHA-1 y no coincide con el catálogo firmado [`silabser.cat`](file:///c:/Users/CORSAIR/Desktop/xview/Drivers_TTL/CP210x/silabser.cat).
   - **Solución aplicada:** Se restauró `silabser.inf` a formato Windows `CRLF` (`10,453 bytes`, hash `CD5610A5B20340323714AD1BA203E97E99E80C9A` verificado con `CryptCATAdminCalcHashFromFileHandle` = `InCat: True`) y se protegió en [`.gitattributes`](file:///c:/Users/CORSAIR/Desktop/xview/.gitattributes) como binario para que Git jamás altere sus saltos de línea.
2. **Error en `SmarTTY`: *"No connections found"* o velocidad distinta a `115200`:**
   - **Causa:** `SmarTTY.GUI.ConnectionSelectionWindow+ControllerImpl` solo muestra puertos `COM` activos en Windows y cruza `_KnownSerialPortSettings` mediante `SerialPortKey(Description, Number)`.
   - **Solución aplicada:** En [`INSTALAR_DRIVERS_TTL_Y_ABRIR_SMARTTY.bat`](file:///c:/Users/CORSAIR/Desktop/xview/INSTALAR_DRIVERS_TTL_Y_ABRIR_SMARTTY.bat) se asigna `DeviceDescription = "Silicon Labs CP210x USB to UART Bridge"` y `BaudRate = 115200`.
3. **Caracteres basura `???????` y `gpio_keypad_down_cnt: 60` en la terminal UART:**
   - **Causa:** Falso contacto en el pin `GND` (Tierra) o contacto accidental con `VCC` / botón físico.
   - **Regla:** Conectar firmemente solo **3 pines**: `GND` $\leftrightarrow$ `GND`, `TX` $\leftrightarrow$ `RX`, `RX` $\leftrightarrow$ `TX` (jamás conectar `3.3V` ni `5V`).
4. **Puerto `COM3` bloqueado (`Se ha denegado el acceso al puerto 'COM3'`) tras cerrar la ventana de `SmarTTY`:**
   - **Causa:** Si `SmarTTY.exe` se inicia con permisos de Administrador, al cerrar su ventana puede quedar un proceso en segundo plano reteniendo el handle de `COM3`. Además, `Start-Process -Verb RunAs` ejecutado desde una consola en segundo plano no muestra la ventana UAC en el escritorio interactivo.
   - **Solución aplicada:** Invocar elevación a través del objeto COM del Explorador interactivo (`(New-Object -ComObject Shell.Application).Windows().Item(0).Document.Application.ShellExecute("taskkill.exe", "/f /im SmarTTY.exe", "", "runas", 1)`) y abrir `COM3` directamente desde PowerShell (`System.IO.Ports.SerialPort`, que no requiere permisos de Administrador).

### ❌ 1.6. Peligro de perder `Widevine L1` (Netflix/Disney+ HD) y MAC/Serial al ejecutar `B866V2-FlashATV12 - Evoca.bat` sin modificar
- **Causa:** El script original [`B866V2-FlashATV12 - Evoca.bat`](file:///c:/Users/CORSAIR/Desktop/xview/V2/B866v2%20Android%2012/B866v2%20Android%2012/B866V2-FlashATV12%20-%20Evoca.bat) incluye:
  ```bat
  fastboot flash factory factory.img
  fastboot flash cri_data cri_data.img
  ```
- **Regla obligatoria:** **NUNCA** flashear `factory.img` ni `cri_data.img` sobre una caja sin respaldarlos antes, ya que `factory` guarda las claves DRM únicas (`Widevine L1` / `HDCP`) y `cri_data` guarda la MAC y número de serie (`ro.boot.serialno`). Usar siempre [`FLASHEAR_B866V2_ANDROID12_SEGURO_SIN_PERDER_WIDEVINE.bat`](file:///c:/Users/CORSAIR/Desktop/xview/experimento/11_VALIDACION_OTA_Y_FIRMWARE_ZTE/FLASHEAR_B866V2_ANDROID12_SEGURO_SIN_PERDER_WIDEVINE.bat).

---

## 2. Opciones Válidas para Entrar al Sistema SIN Abrir Ninguna Caja Xview (`ZTE B866V2`)

### ✅ Opción A: Llaves Cifradas en Memoria USB FAT32 ([`QADEBUG.ini`](file:///c:/Users/CORSAIR/Desktop/xview/QADEBUG.ini) y [`QADEBUGTOOL.ini`](file:///c:/Users/CORSAIR/Desktop/xview/QADEBUGTOOL.ini))
Con la caja cerrada y encendida en Android:
1. Insertar una memoria USB formateada en **FAT32** que tenga en su raíz los archivos de `138 bytes` [`QADEBUG.ini`](file:///c:/Users/CORSAIR/Desktop/xview/QADEBUG.ini) y [`QADEBUGTOOL.ini`](file:///c:/Users/CORSAIR/Desktop/xview/QADEBUGTOOL.ini).
2. **Funcionamiento interno verificado por ingeniería inversa:**
   - `com.ztestb.basecomp.apk` (`RemoteStbCfgService->check_QADEBUG_ini` y `transString`) y `com.zte.factorytesttool.apk` (`TestToolBroadcast->decodeQADEBUGTOOL`, `IsStartApkService` y `SecureUtil->Decrypt`) leen los primeros `0x60` (`96`) bytes como cabecera y descifran el resto usando la tabla de sustitución simétrica de 52 caracteres:
     - Entrada: `ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz`
     - Salida:  `RSTUVWXYZPONMLKEDCBAQJIHGFrstuvwxyzponmlkedcbaqjihgf`
   - Ambos archivos contienen cifradas las dos versiones de firmware (`V84711310.1024` y `V82815417.2033`), por lo que `String.contains(ro.build.version.incremental)` devuelve `true` y activa el servicio de diagnóstico/fábrica al montar la USB (`zte.intent.action.MEDIA_MOUNTED`).

### ✅ Opción B: Activar ADB desde Ajustes (Teclado USB / Voz) + [`LIBERAR_SIN_ABRIR_CAJA.bat`](file:///c:/Users/CORSAIR/Desktop/xview/LIBERAR_SIN_ABRIR_CAJA.bat)
Para liberar cualquier caja cerrada o meterla en **Fastboot** sin desarmarla:
1. Con la caja encendida, abrir **Ajustes de Android**:
   - Conectando un **teclado USB** y presionando **`Win + I`** (o botón de Configuración), o
   - Usando el botón de **Google Assistant (micrófono)** del control remoto y diciendo **"Abrir Configuración"** / **"Ajustes"**.
2. Ir a **Preferencias del dispositivo $\rightarrow$ Información $\rightarrow$ Compilación** y presionar **7 veces `OK`** para habilitar **Opciones de programador** y activar **Depuración por USB**.
3. Conectar la caja a la PC mediante:
   - El cable **USB Macho-A a Tipo-C** en el **puerto USB 2.0** (junto a MicroSD), o
   - La misma red local **Wi-Fi / Ethernet** (puerto TCP `5555`).
4. Ejecutar [`LIBERAR_SIN_ABRIR_CAJA.bat`](file:///c:/Users/CORSAIR/Desktop/xview/LIBERAR_SIN_ABRIR_CAJA.bat) (o [`Liberacion_Xview_B866V2_Portatil.exe`](file:///c:/Users/CORSAIR/Desktop/xview/Liberacion_Xview_B866V2_Portatil.exe)):
   - **Modo 1 (Liberación rápida por ADB sin borrar nada):** Desactiva el launcher y bloqueos de Megacable (`com.dotscreen.megacable.atv`, `com.ztestb.LauncherCustomize`, `com.zte.ums.oes`), instala el launcher libre y mantiene intactos Android 12 y Widevine L1.
   - **Modo 2 (Entrar a Fastboot sin abrir la caja para flashear ROM completa):** Ejecuta `adb reboot bootloader`, lo cual envía `bl31 reboot reason: 0x7` y pone la caja cerrada directamente en modo **Fastboot** sobre el puerto USB 2.0 para correr [`FLASHEAR_B866V2_ANDROID12_SEGURO_SIN_PERDER_WIDEVINE.bat`](file:///c:/Users/CORSAIR/Desktop/xview/experimento/11_VALIDACION_OTA_Y_FIRMWARE_ZTE/FLASHEAR_B866V2_ANDROID12_SEGURO_SIN_PERDER_WIDEVINE.bat).

---

## 3. Procedimiento Verificado por UART/TTL (`COM3` a `115200 8N1`) (En Caja Abierta)

Comprobado en vivo sobre la placa `ZTE B866V2` (`Board ID = 8`, `AMLS905X2`, `Serial: Z41220110213792`):
1. En modo **Recovery**, el servicio `console` (`/init.recovery.amlogic.rc`) entrega una terminal interactiva **`uid=0(root) gid=0(root) context=u:r:shell:s0`** en `COM3` (`115200 8N1`) sin pedir contraseña.
2. Enviar el comando:
   ```sh
   reboot bootloader
   ```
   reinicia el SoC con `bl31 reboot reason: 0x7` y deja la placa directamente en modo **Fastboot** sin necesidad de hacer corto/puente en la memoria eMMC.
3. Con el cable **USB Macho-A a Tipo-C** conectado en el **puerto USB 2.0** (junto a la ranura MicroSD), `fastboot.exe` detecta la caja inmediatamente para flashear el sistema.
