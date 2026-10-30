# Informe Forense en Vivo: Entrada a Fastboot, Diagnóstico de `watchdog_reboot` (`U-Boot 2015.01-g220e992`) y Solución Anti-Watchdog

**Fecha de Captura:** 29 de Octubre de 2026  
**Dispositivo Analizado:** `ZTE ZXV10 B866V2` (`SN: Z41220090388172`, `MAC: C4:74:1E:F6:BE:19`, `ProductID: 48100499007041200000C4741EF6BE19`)  
**SoC / Bootloader:** `Amlogic G12A (S905X2) Rev E (28:E - 40:2)` — `U-Boot 2015.01-g220e992 (Sep 05 2023 - 04:13:01)`  
**Interfaz UART:** `Silicon Labs CP210x USB to UART Bridge (COM8 @ 115200 bps)`  

---

## 1. Cronología Exacta de los 5 Reinicios Capturados en Vivo (`boot times 0` a `boot times 4`)

Mediante el monitor de memoria en tiempo real ([`monitor_smartty.ps1`](./monitor_smartty.ps1)) sobre `SmarTTY.exe` (`PID 3284`), se reconstruyó la secuencia completa sin desconectar el puerto serie:

| Ciclo (`BL2`) | Razón de Reinicio (`BL31` / `U-Boot`) | Qué Ocurrió Exactamente en Hardware y Firmware | Resultado |
| :--- | :--- | :--- | :--- |
| **`boot times 0`** | `reboot_mode=cold_boot` | Encendido en frío con el botón de Reset presionado (`GPIOAO_3 == 0`). El kernel detecta `gpio_keypad_down_cnt: 60` y ejecuta `Restarting system with command 'recovery'`. | Entra a modo **Recovery** |
| **`boot times 1`** | `bl31 reboot reason: 0xd` / `0x2` (`reboot_mode:::: update`) | U-Boot lee `[commannd:boot-recovery]` en la partición `misc`, desactiva Dolby (`recovery mode, need disable dolby`) y levanta la consola root `#` en UART. | Consola root `#` activa en Recovery |
| **`boot times 2`** | `bl31 reboot reason: 0x7` (`reboot_mode:::: fastboot`) | Desde la consola root `#` se ejecutó `reboot bootloader`. U-Boot registra el gadget USB Fastboot (`g_dnl_register: g_dnl_driver.name = usb_dnl_fastboot`). Se lanza el script `.bat`: `cb_oem_cmd()` falla en `f_fastboot.c:1649` y acto seguido ejecuta `cmd cb_reboot is reboot-bootloader`, que escribe `bootonce-bootloader` en `misc`. | Entra a **Fastboot** y reinicia hacia `boot times 3` |
| **`boot times 3`** | `reboot mode: bootloader` (`reboot_mode:::: fastboot`) | La placa vuelve a entrar a Fastboot, pero con **`lock state: 10001000`** (`info->lock_state = 1`). Todos los comandos `cb_download` (`01800000`, `00800000`, `00040000`) y `erase()` (`f_fastboot.c:1530`) son rechazados en milisegundos con `ERROR: device is locked, can not run this cmd.Please flashing unlock & flashing unlock_critical`. Al final del script o por timeout se dispara el reinicio. | Rechaza escritura por `lock_state = 1` y sale de Fastboot |
| **`boot times 4`** | `reboot_mode=watchdog_reboot` (`reboot_mode:::: watchdog_reboot`) | El temporizador Watchdog de hardware reinicia el SoC. U-Boot ejecuta `Command: bcb uboot-command`, lee `[commannd:bootonce-bootloader]` en `misc` y lo descarta (`command mark(uboot-command) not match bootonce-bootloader,don't execute.`). Luego busca `boot-recovery` en `misc`, falla (`read boot-recovery from misc failed.`) y asigna **`ztebsp: bootcmd=bootsys`**. | Carga Android normal (`bootsys`) y sale de Fastboot/Recovery |

---

## 2. Los 2 Motivos Exactos por los que la Caja se Sale de Fastboot (Errores Diagnosticados)

### Motivo A: Rechazo de `cb_oem_cmd()` y ejecución hasta `fastboot reboot` con `lock state: 10001000`
En [`BLOQUES_ASCII_COMPLETOS_SESION.txt`](./BLOQUES_ASCII_COMPLETOS_SESION.txt) (líneas 883–892 y 1253–1262) quedó capturada la respuesta interna del U-Boot de septiembre de 2023 (`2015.01-g220e992`):
```text
at ../drivers/usb/gadget/f_fastboot.c:1649/cb_oem_cmd()
cmd cb_reboot is reboot-bootloader
...
cmd cb_download is download:01800000
lock state: 10001000
info->version_major = 1
info->version_minor = 0
info->unlock_ability = 0
info->lock_state = 1
info->lock_critical_state = 0
info->lock_bootloader = 0
ERROR: device is locked, can not run this cmd.Please flashing unlock & flashing unlock_critical
at ../drivers/usb/gadget/f_fastboot.c:1075/cb_download()
```
* Al fallar el desbloqueo inicial, `fastboot flash` no tarda minutos transfiriendo `super.img`, sino que aborta en milisegundos en cada partición y llega directamente a la última línea del `.bat` (`fastboot reboot`), sacando la caja de Fastboot.

### Motivo B: Watchdog de Hardware Activo en U-Boot (`reboot_mode:::: watchdog_reboot`) + Pérdida de `bootonce-bootloader`
En [`BLOQUES_ASCII_COMPLETOS_SESION.txt`](./BLOQUES_ASCII_COMPLETOS_SESION.txt) (líneas 453–490) se demostró por qué la placa no regresa a Fastboot tras un reinicio por inactividad:
```text
reboot_mode:::: watchdog_reboot
s_version: U-Boot 2015.01-g220e992
...
gpio: pin GPIOAO_3 (gpio 3) value is 1
Command: bcb uboot-command 
Start read misc partition datas!
get bootloader message from misc partition:
[commannd:bootonce-bootloader]
[status:]
[recovery:]
command mark(uboot-command) not match bootonce-bootloader,don't execute.
ztebsp: bootcmd=bootsys
...
read boot-recovery from misc failed.
```
* Mientras la placa espera en Fastboot sin actividad USB (por ejemplo, esperando confirmación `S/N` del usuario o durante pausas `timeout /t 2` y `timeout /t 4`), el **Watchdog de hardware (`ffd0f0d0.watchdog`)** expira y reinicia la placa (`reboot_mode=watchdog_reboot`).
* Ese reinicio de hardware **borra el registro volátil `bl31 reboot reason: 0x7`** (`fastboot`).
* En el siguiente arranque, U-Boot ve `reboot_mode=watchdog_reboot`, comprueba la partición `misc` con `bcb uboot-command` (que ignora `bootonce-bootloader`), intenta leer `boot-recovery` de `misc` (`read boot-recovery from misc failed.`) y termina forzando **`ztebsp: bootcmd=bootsys`** (sistema Android normal).

---

## 3. Solución Implementada en 3 Capas (Aciertos y Mejoras)

1. **Capa 1 — Ancla Persistente en `/dev/block/misc` desde Recovery (`#`) antes de ir a Fastboot:**
   Ejecutar en `SmarTTY` (consola root `#` de Recovery):
   ```sh
   echo -n "boot-recovery" > /dev/block/misc ; sync ; reboot bootloader
   ```
   * **Efecto:** El registro de hardware `BL31` (`0x7`) mete la placa inmediatamente a **Fastboot**. Pero si el Watchdog llega a disparar `watchdog_reboot`, U-Boot encuentra `boot-recovery` en `misc`, **bloquea `ztebsp: bootcmd=bootsys`** y regresa siempre a la consola root `#` de **Recovery**.
2. **Capa 2 — Enganche Inmediato `< 200 ms` sin `timeout` en [`FLASHEAR_B866V2_ANDROID12_SEGURO_SIN_PERDER_WIDEVINE.bat`](../11_VALIDACION_OTA_Y_FIRMWARE_ZTE/FLASHEAR_B866V2_ANDROID12_SEGURO_SIN_PERDER_WIDEVINE.bat):**
   * Se eliminó la pausa interactiva posterior y todos los `timeout /t 2` / `timeout /t 4`.
   * El script valida primero el firmware y entra en un bucle rápido `:WAIT_FASTBOOT` que detecta la enumeración USB de `usb_dnl_fastboot` al instante, enviando `fastboot getvar version` y `fastboot oem "watchdog off"`.
3. **Capa 3 — Retenedor Keep-Alive Indefinido ([`RETENER_FASTBOOT_ANTI_WATCHDOG.bat`](./RETENER_FASTBOOT_ANTI_WATCHDOG.bat)):**
   * Envía pulsos `fastboot getvar version` cada 2 segundos y `fastboot oem "watchdog off"` para mantener activa la máquina de estados USB de `f_fastboot.c` todo el tiempo que se requiera sin que el Watchdog reinicie la placa.
