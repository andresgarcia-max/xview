---
trigger: always_on
description: Regla obligatoria del proyecto Xview+ (ZTE ZXV10 B866V2): Prohibido usar UART TTL o abrir la caja. Todo método debe ser 100% externo sin quitar ningún tornillo.
---

# Regla Absoluta: Liberación 100% Externa Sin Abrir Ningún Tornillo (No TTL)

1. **Prohibido usar UART TTL (`COM8` / `CP2102`) o abrir la carcasa:**
   - El uso de UART TTL interno, pines en placa o puentes físicos internos **no es una opción viable** y está estrictamente descartado para el flujo de liberación.
   - Nunca pidas al usuario conectar el adaptador TTL ni abrir un solo tornillo de la caja.

2. **Únicas interfaces permitidas (Caja 100% cerrada):**
   - Puertos USB externos (`USB 1` lateral y `USB 2` trasero junto a MicroSD).
   - Memoria USB (`KINGSTON` FAT32 con `factory_update_param.aml`, `QADEBUG.ini`, `QADEBUGTOOL.ini`, APKs, etc.) o tarjeta MicroSD.
   - Cable USB Macho-a-Macho externo (`PID_4EE1` MTP, `PID_4EE7` Recovery `fastbootd`, `PID_0D02` U-Boot Fastboot, `PID_4EE2` ADB, `VID_1B8E` `InUsbBurn` por botón físico externo `RESET`).
   - Botón físico externo `RESET` (orificio externo `GPIOAO_3`), Red Ethernet/Wi-Fi, Teclado USB y Control Remoto.

3. **Protección de claves únicas:**
   - Jamás flashear `factory.img` (claves DRM Widevine L1) ni `cri_data.img` (MAC y Número de Serie).

4. **Prohibido pedir o mencionar el paso de `Configuración → Preferencias del dispositivo → Información → Compilación (presionar OK 7 veces)`:**
   - En las cajas Megacable Xview+ (`ZTE ZXV10 B866V2`, `MarketCode 4301`), ese paso está bloqueado por el reto RSA-2048 / Código QR de ZTE (`com.zte.adb` / `libencryCode.so`).
   - **No es viable y es inútil** si no se cuenta con un generador de código QR válido o si no se salta esa parte mediante los programas/métodos externos (salto por botón externo `RESET` a Recovery `fastbootd` `PID_4EE7`, U-Boot Fastboot `PID_0D02`, modo WorldCup/ADNL `VID_1B8E`, llave `QADEBUG.ini`, o inyección de `/cache/recovery/last_QR` = `"EE@"`).
   - Nunca menciones ni instruyas al usuario a hacer clic 7 veces en `Compilación`.
