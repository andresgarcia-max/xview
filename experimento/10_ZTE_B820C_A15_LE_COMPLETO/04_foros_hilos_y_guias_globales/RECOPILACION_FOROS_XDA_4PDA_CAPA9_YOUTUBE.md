# Recopilación Global de Foros (XDA, 4PDA, Capa9, AVPasión, YoReparo) y Tutoriales de YouTube: ZTE ZXV10 B820C-A15 LE

Compendio técnico de todos los hilos de discusión, métodos de recuperación, desbloqueo de opciones de desarrollador, comandos de control remoto, activación de ADB y reemplazo de launcher documentados a nivel global para el **ZTE ZXV10 B820C-A15 LE**.

---

## 1. XDA Developers — Hilo Principal (`t/4241699`)
* **Título:** *"How to Activate Developer Options in Izzi zte zxv10 b820c-a15?"*
* **Problemática reportada:**
  * En los decodificadores **ZTE ZXV10 B820C-A15 LE** entregados por **izzi** (*izzitv Smart*), el procedimiento estándar de Android TV (*Ajustes → Preferencias del dispositivo → Información → pulsar 7 veces en Compilación*) fue bloqueado mediante una política de usuario (`UserManager.DISALLOW_DEBUGGING_FEATURES`) y por banderas en la partición `conf` (`config.MarketCode` / `config.OperatorCode` / `isRestrictedIzzi`).
  * Al intentar pulsar 7 veces en *Compilación*, el sistema muestra que **la cuenta no tiene permisos para realizar esta acción** o simplemente no habilita el menú *Opciones de desarrollador*.
* **Hallazgo forense en las APKs de sistema ZTE (`com.ztestb.LauncherCustomize.apk` y `com.android.tv.settings.apk`):**
  * El sistema lee las propiedades de ZTE `config.MarketCode` y `config.OperatorCode` desde el servicio nativo `libzteaidlproductcfg.so` (`com.zte.android.productcfg`), que a su vez las carga desde la partición **`conf` (`4 MB`, `/dev/block/conf`)**.
  * La clase `com.ztestb.util.MarketUtil` incluye explícitamente las funciones `isMEXICO_IZZI()` e `isRestrictedIzzi()`, las cuales restringen el launcher y las opciones de depuración cuando detectan el código de mercado de izzi.

---

## 2. El Secreto de la Partición `conf` (Comunidad Indonesia / 4PDA / GitHub `tohenk/b860hv5-os10-root`)

En los foros de Indonesia y Rusia (**4PDA**), donde los operadores despliegan la misma familia de placas ZTE con SoC **Amlogic S905X2 (`g12a_u212_v1`)**, se descubrió por qué el bloqueo de Opciones de Desarrollador sobrevive a un *Factory Reset* normal:

1. **Dónde vive el bloqueo:**
   * No está únicamente en `/data`, sino en la partición **`conf` (`/dev/block/conf`, tamaño `0x400000` = `4 MB`)**.
2. **Cómo se salta sin cambiar la ROM completa:**
   * Conectando el decodificador en modo **Amlogic WorldCup / MaskROM USB** con un cable **USB-A Macho a Macho** y ejecutando `update.exe` (incluido en [`02_repositorios_github_y_herramientas/tohenk__b860hv5-os10-root`](./../02_repositorios_github_y_herramientas/tohenk__b860hv5-os10-root)):
     1. Se hace copia de seguridad de la partición `conf` original:
        ```cmd
        update.exe mread store conf normal 0x400000 conf_original.img
        ```
     2. Se desbloquea el entorno U-Boot:
        ```cmd
        update.exe bulkcmd "setenv -f lock 10100000"
        update.exe bulkcmd "saveenv"
        ```
     3. Se flashea la imagen `conf.PARTITION` limpia (incluida en [`data/conf.PARTITION`](./../02_repositorios_github_y_herramientas/tohenk__b860hv5-os10-root/data/conf.PARTITION)):
        ```cmd
        update.exe partition conf data\conf.PARTITION
        ```
     4. Al encender el equipo, `MarketUtil.isRestrictedIzzi()` evalúa `false`, **permitiendo activar Opciones de Desarrollador y Depuración USB/ADB con los 7 clics normales en Compilación**.

---

## 3. Análisis de `com.zte.adb.apk` y `libencryCode.so` (Agente ADB Interno de ZTE)

Dentro del firmware de la familia ZTE `ZXV10` (`B820C-A15` / `B866V2`) existe la aplicación de sistema [`com.zte.adb.apk`](../../04_apks_dispositivo/com.zte.adb.apk), cuyo análisis binario revela todos los mecanismos internos de soporte técnico de ZTE:

1. **Variantes de interfaz integradas dentro del APK:**
   * `com.zte.adb.AdbSetting` (Estándar ZTE con desafío-respuesta aleatorio `zte.stbtool.randomcode`)
   * `com.zte.adb.AdbSetting_amx` (Variante para **América Móvil / Claro / Latam** — layout `activity_start_adb_amx.xml`)
   * `com.zte.adb.AdbSetting_atv9` (Variante para **Android TV 9+** — layout `activity_start_adb_atv9.xml`)
2. **Propiedades del sistema que controla `com.zte.adb`:**
   * `service.adb.tcp.port` (Puerto TCP de ADB, por defecto `5555`)
   * `persist.sys.usb.config` (`adb`)
   * `init.svc.adbd` (`running` / `stopped`)
   * `sys.zte.adb.random` / `persist.sys.zte.adb.random` (Semilla aleatoria para el generador de contraseñas de ZTE `libencryCode.so`)
   * `sys.adb.login_chances` y `sys.zte.adb.misstoomuch` (Contador de intentos fallidos de contraseña ADB)
3. **Intents de combinación de teclas (`combkey`) registrados por ZTE:**
   * `com.zte.combkey.grab_log` (Iniciar captura de logs a memoria USB)
   * `com.zte.combkey.grab_log_stop` (Detener captura de logs)
   * `com.zte.combkey.copy_usb_log` (Copiar logs del sistema al USB)
   * `com.zte.combkey.clear_usb_log` (Limpiar logs)

---

## 4. Procedimientos Prácticos de YouTube y Foros (Capa9, YoReparo, AVPasión)

### Método 1: Hard Reset / Entrada al Menú Recovery con Teclado USB
Cuando el **ZTE B820C-A15 LE** se queda en pantalla negra, bucle en el logo de izzi/Android o extremadamente lento:
1. Desenchufar el eliminador de corriente de **12V DC** de la parte trasera.
2. Conectar un **teclado USB estándar de PC** en el puerto USB 2.0 lateral. *(Obligatorio: el control remoto Bluetooth/IR no funciona dentro del menú Recovery de Android).*
3. Mantener presionado el **botón físico de encendido (Power)** en la carcasa del decodificador.
4. Sin soltar el botón, conectar el cable de corriente de **12V DC** y mantener presionado durante **25 a 30 segundos** hasta que el LED frontal cambie de **rojo a verde** y aparezca el menú **Android Recovery**.
5. Con las flechas del teclado USB bajar hasta **`Wipe data/factory reset`** → presionar `Enter` → seleccionar **`Factory data reset`**.
6. Seleccionar **`Wipe cache partition`** → `Yes` → **`Reboot system now`**.

### Método 2: Uso del Decodificador en Cualquier Red Wi-Fi + Bypass de Launcher (Sin Cables ni Root)
Si el equipo muestra la pantalla de bloqueo del operador al iniciar:
1. **Conectar un Teclado USB** o usar el botón de **Google Assistant (Micrófono)** en el control remoto.
2. En el teclado USB, presionar combinaciones estándar de Android TV:
   * `Windows + B` (abre el navegador o búsqueda de Google)
   * `Windows + I` o tecla `Settings/Menu` (abre Ajustes de Android)
   * O mantener presionado el botón de **Asistente de Voz** en el control y decir: ***"Abrir Configuración"*** o ***"Abrir Google Play Store"***.
3. Entrar a *Red e Internet* y conectarse a cualquier red Wi-Fi doméstica o hotspot móvil.
4. Abrir **Google Play Store** (por comando de voz *"Abrir Play Store"*) e instalar:
   * **Projectivy Launcher** (o **FLauncher**)
   * **Button Mapper**
   * **tvQuickActions** o **Downloader by AFTVnews** (código o URL para sideloading si está permitido)
5. En **Projectivy Launcher**, ir a *Ajustes de Projectivy → Accesibilidad*, activar el servicio de accesibilidad y marcar **"Sobrescribir el launcher actual (Override current launcher)"**. Con esto, el decodificador inicia directamente en Projectivy Launcher al encender y al presionar el botón `Home`.

### Método 3: Emparejamiento y Reset del Control Remoto Bluetooth (`B820C-A15 LE`)
* Para desvincular/resetear el control remoto Bluetooth LE y volver a emparejarlo con el `B820C-A15 LE`:
  * Mantener presionados simultáneamente los botones **`OK` + `Home`** (o **`Back` + `Home`** según la revisión del control izzi) a menos de 30 cm del decodificador durante 5 segundos hasta que el LED del control parpadee rápidamente.
