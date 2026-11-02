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
