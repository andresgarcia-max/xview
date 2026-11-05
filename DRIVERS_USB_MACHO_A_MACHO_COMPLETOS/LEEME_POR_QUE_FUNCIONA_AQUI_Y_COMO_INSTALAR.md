# Guía Rápida: Clonar los Drivers del Cable USB Macho-a-Macho en Otra PC

Ejecuta como Administrador:
👉 **`INSTALAR_TODOS_LOS_DRIVERS_MACHO_A_MACHO.bat`**

## ¿Qué instala este paquete (extraído de la Dell Latitude 7490 `0KP0FT`)?
1. **5 Certificados Digitales (`.cer`)** en `Root` y `TrustedPublisher` para que Windows 10/11 acepte sin bloqueos los drivers autofirmados de `libwdi` y `libusb-win32`.
2. **Driver Amlogic ADNL (`VID_1B8E&PID_C004` - `oem79.inf`)**: El driver que faltaba en tu otra PC y que permite que `adnl.exe` y el script de flasheo detecten la caja al salir de BootROM.
3. **Driver Amlogic WorldCup (`VID_1B8E&PID_C003` - `oem80.inf`)**: Para el modo BootROM (`InUsbBurn` al presionar `RESET`).
4. **Drivers Google Android WinUSB (`VID_18D1&PID_0D02`, `PID_4EE7`, `PID_4EE0`, `PID_4EE2`)**: Para U-Boot Fastboot, Recovery `fastbootd` y ADB.
5. **Banderas de Registro `usbflags`** (`CONFIGURACION_USBFLAGS_XVIEW_MACHO_A_MACHO.reg`) + desactivación de Suspensión Selectiva USB + ajuste de Integridad de Memoria (HVCI).
