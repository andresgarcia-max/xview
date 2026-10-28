# Reporte Forense de APKs del Ecosistema ZTE Híbrido (`B820C-A15 LE` / `B866V2`)

Este documento resume los hallazgos obtenidos al analizar las tablas de cadenas (`string_ids`) de los archivos `classes.dex`, librerías nativas (`.so`) y archivos de código fuente `.java.bak` incluidos dentro de las aplicaciones de sistema de ZTE.

---

## 1. Hallazgo Crítico: Código compartido entre Megacable (`B866V2`) e Izzi (`B820C-A15 LE`)

Al inspeccionar los binarios DEX de las aplicaciones del sistema ZTE, se comprobó que ZTE compila **un único árbol de código base unificado** para todos sus operadores de Latinoamérica y Asia, activando o desactivando restricciones en tiempo de ejecución según los valores de `MarketCode` y `OperatorCode` almacenados en la partición `/dev/block/conf`.

### Evidencia en `com.ztestb.LauncherCustomize.apk`
En el archivo `classes.dex` de `com.ztestb.LauncherCustomize.apk` se encontraron las siguientes constantes y métodos compilados:
- **Izzi Telecom (`B820C-A15 LE`):**
  - `MEXICO_IZZI`
  - `isMEXICO_IZZI`
  - `isRestrictedIzzi`
  - `Control izzitv`
  - `isBootToLauncher()`
- **Megacable Xview+ (`B866V2`):**
  - `Mexico_Megacable`
  - `MEXICO_MEGACABLE_OTT`
  - `com.dotscreen.megacable.view.MainActivity`
  - `com.dotscreen.megacable.atv`
- **Modelos hermanos:**
  - `B826C-A16` (variante híbrida hermana del `B820C-A15 LE`)

### Evidencia en `com.ztestb.basecomp.apk`
En `com.ztestb.basecomp.apk` se encontró la cadena de registro exacta que explica por qué el `B820C-A15 LE` de Izzi ignora las memorias USB con scripts de depuración (`udisk log`):
- `Mexican IZZI market, not start U disk log for broadcast[zte.intent.action.MEDIA_MOUNTED]`
- Tabla SQLite de configuración: `CREATE TABLE cfginfo (section TEXT, name TEXT, value TEXT, reserved TEXT);`

### Evidencia en `com.zte.netmanager.apk`
- Contiene validación explícita de `MEXICO_IZZI` para controlar el aprovisionamiento de red e interfaces de gestión.

---

## 2. Arquitectura del Demonio `com.zte.adb.apk` y `libencryCode.so`

Los ingenieros de ZTE dejaron accidentalmente dos archivos de respaldo de código fuente Java dentro del APK `com.zte.adb.apk`:
1. [`com_zte_adb_common_ClientManager.java.bak`](file:///c:/Users/CORSAIR/Desktop/xview/experimento/10_ZTE_B820C_A15_LE_COMPLETO/07_analisis_apks_zte_hibrido/com_zte_adb_common_ClientManager.java.bak)
2. [`com_zte_adb_common_UtilsInstance.java.bak`](file:///c:/Users/CORSAIR/Desktop/xview/experimento/10_ZTE_B820C_A15_LE_COMPLETO/07_analisis_apks_zte_hibrido/com_zte_adb_common_UtilsInstance.java.bak)

Y en `tv.settings.aidl.apk`:
3. [`tv.settings.aidl.apk_tv_settings_aidl_ApplicationService.java.bak`](file:///c:/Users/CORSAIR/Desktop/xview/experimento/10_ZTE_B820C_A15_LE_COMPLETO/07_analisis_apks_zte_hibrido/tv.settings.aidl.apk_tv_settings_aidl_ApplicationService.java.bak)

### Cómo funciona `com.zte.adb`:
- `ClientManager.java` abre un socket local UNIX hacia el demonio nativo **`zxdb`**:
  ```java
  LocalSocketAddress address = new LocalSocketAddress("zxdb", LocalSocketAddress.Namespace.RESERVED);
  ```
- Para habilitar o deshabilitar ADB, construye un comando con `UtilsInstance.get ADbCmd("open")` o `UtilsInstance.get ADbCmd("close")`.
- Carga la librería nativa [`lib_armeabi_libencryCode.so`](file:///c:/Users/CORSAIR/Desktop/xview/experimento/10_ZTE_B820C_A15_LE_COMPLETO/07_analisis_apks_zte_hibrido/lib_armeabi_libencryCode.so) (`System.loadLibrary("encryCode")`), la cual exporta:
  - `Java_com_zte_adb_common_UtilsInstance_generateCheckData`
  - `Java_com_zte_adb_common_UtilsInstance_generateRandom`
- Además lee las banderas de configuración desde `content://stbconfig/summary/` (`config.EnableADB`, `config.AdbPassword`, `config.MarketCode`, `config.OperatorCode`).

---

## 3. Subsistema Híbrido DVB-C (`com.droidlogic.dvbaudioservice.apk` y `com.zte.factorytesttool.apk`)

- **`com.droidlogic.dvbaudioservice.apk`** ([`com.droidlogic.dvbaudioservice.apk_analisis_dex.json`](file:///c:/Users/CORSAIR/Desktop/xview/experimento/10_ZTE_B820C_A15_LE_COMPLETO/07_analisis_apks_zte_hibrido/com.droidlogic.dvbaudioservice.apk_analisis_dex.json)): Gestiona la mezcla de audio Dolby/AC3 del demultiplexor de hardware Amlogic DVB (`TS0`).
- **`com.zte.factorytesttool.apk`** ([`com.zte.factorytesttool.apk_analisis_dex.json`](file:///c:/Users/CORSAIR/Desktop/xview/experimento/10_ZTE_B820C_A15_LE_COMPLETO/07_analisis_apks_zte_hibrido/com.zte.factorytesttool.apk_analisis_dex.json)): Contiene 49,971 cadenas e incluye las pruebas de fábrica de ZTE para el sintonizador DVB (`DVBTest`), bloqueo de frecuencia RF (`lock frequency`), pruebas de SmartCard/CA y lectura/escritura de MAC/SN en la partición `conf`.

---

## 4. Conclusión Técnica sobre la Partición `conf` (`/dev/block/conf`)

1. Tanto en el **`ZTE ZXV10 B820C-A15 LE`** (Izzi) como en el **`ZTE ZXV10 B866V2`** (Megacable Xview+), la personalización restrictiva (`isRestrictedIzzi`, bloqueo de `U disk log`, ocultamiento de Opciones de Desarrollador y desactivación de `adbd`) depende del `MarketCode` y `OperatorCode` guardados en la partición **`conf`** (`4 MB`).
2. En el repositorio [`tohenk__b860hv5-os10-root`](file:///c:/Users/CORSAIR/Desktop/xview/experimento/10_ZTE_B820C_A15_LE_COMPLETO/02_repositorios_github_y_herramientas/tohenk__b860hv5-os10-root) se incluye el binario limpio [`conf.PARTITION`](file:///c:/Users/CORSAIR/Desktop/xview/experimento/10_ZTE_B820C_A15_LE_COMPLETO/02_repositorios_github_y_herramientas/tohenk__b860hv5-os10-root/data/conf.PARTITION) de 4 MB para la plataforma ZTE `g12a_u212_v1` (Amlogic S905X2), el cual permite restaurar la configuración estándar en modo **Amlogic WorldCup USB** sin modificar el `bootloader.img` (preservando la inicialización `BL2` de memoria RAM DDR3 en el `B820C-A15 LE` o DDR4 en el `B866V2`).
