# Análisis de tu Tarjeta Madre **ASUS ROG STRIX B550-A GAMING** y Configuración Especial para Xview+

## 1. Identificación Exacta de tu Placa Base
* **Modelo:** **ASUS ROG STRIX B550-A GAMING** (Edición Blanco/Plata)
* **Socket / Plataforma:** **AMD AM4** (Procesadores AMD Ryzen Serie 3000 / 4000G / 5000)
* **Chipset:** **AMD B550**
* **Controladoras USB:** Doble arquitectura de controladores **`AMD USB 3.10 eXtensible Host Controller`** (`PCI\VEN_1022`):
  1. **Controladora integrada en el procesador AMD Ryzen (SoC I/O Die):** Maneja los puertos traseros **USB 3.2 Gen 2 (10 Gbps, Puerto Rojo + USB-C)** y **2x USB 3.2 Gen 1 (5 Gbps Azules)**.
  2. **Controladora del Chipset AMD B550 (PCH):** Maneja los **2x USB 2.0 Negros traseros**, **2x USB 3.2 Gen 1 adicionales** y los headers internos del gabinete (`USB_E12` y `USB_E34`).

---

## 2. ¿Por qué tu **ASUS ROG STRIX B550-A GAMING** rechazaba la Xview+ por cable Macho-a-Macho?

A diferencia de la laptop **Dell Latitude 7490 (Intel Sunrise Point-LP)**, tu tarjeta madre **ASUS ROG STRIX B550-A GAMING** tenía **4 barreras específicas de la plataforma AMD B550 + ASUS ROG**:

### Causa A: El Bug de `BOS Descriptor` en el controlador `AMD USB 3.10 eXtensible Host Controller`
* En los chipsets **AMD B550 / X570**, cuando conectas un dispositivo en modo Bootloader (`U-Boot Fastboot PID_0D02`), modo `ADNL` (`VID_1B8E&PID_C004`) o modo `WorldCup` (`VID_1B8E&PID_C003`), el controlador xHCI de AMD envía inmediatamente una petición **BOS (Binary Object Store) Descriptor (`0x0F`)** de negociación USB 3.x Link Power Management (`LPM`) y `ContainerID`.
* El procesador **Amlogic S905X2** de la caja Xview+ usa un stack USB 2.0 antiguo en su BootROM y U-Boot (`2015.01`) que **no soporta la petición BOS de AMD y se congela en el acto** (apareciendo como *Dispositivo USB desconocido - Error de solicitud de descriptor de dispositivo* `VID_0000&PID_0002`, o desconectándose apenas corre `fastboot` o `adnl`).
* **Solución aplicada en nuestra configuración especial:** Inyectamos en `HKLM\SYSTEM\CurrentControlSet\Control\usbflags` las directivas **`SkipBOSDescriptorQuery = 1`**, **`SkipContainerIdQuery = 1`**, **`osvc = 00 00`** e **`IgnoreHWSerNum = 1`** para todos los IDs de la Xview+ (`1B8EC003`, `1B8EC004`, `18D10D02`, `18D14EE7`, `18D14EE0`, `18D14EE1`, `18D1D001`, tanto en formato de 8 como de 12 dígitos). Con esto, el controlador AMD B550 **omite la petición BOS** y habla en USB 2.0 puro con la Xview+.

### Causa B: Los Puertos Directos al CPU Ryzen vs. Puertos USB 2.0 del Chipset B550
* Si conectas el cable Macho-a-Macho en el **puerto USB Rojo (10 Gbps)** o en los **puertos Azules** de tu ROG STRIX B550-A GAMING, estás entrando directo al *I/O Die* del procesador Ryzen, o bien si lo conectas en el puerto negro que tiene escrito **`BIOS`**, pasas por el chip multiplexor **ASUS BIOS FlashBack™**, el cual monitorea el puerto esperando una memoria FAT32 con un archivo `.CAP` de BIOS.
* **En qué puerto físico DEBES conectarlo en tu ASUS ROG STRIX B550-A GAMING:**
  * En la fila superior trasera tienes **2 puertos USB 2.0 Negros** juntos:
    * Uno tiene un recuadro blanco que dice **`BIOS`** (NO uses ese).
    * **Usa el OTRO puerto USB 2.0 NEGRO que está justo al lado del puerto `BIOS`** (o bien un puerto **USB 2.0 del panel frontal** de tu gabinete conectado a `USB_E12` / `USB_E34` abajo en la placa).

### Causa C: Ahorro de Energía del Enlace PCIe (ASPM) entre el CPU Ryzen y el Chipset B550
* En placas B550, el chipset se comunica con el procesador Ryzen mediante líneas PCIe. Si Windows tiene activo el **PCI Express Link State Power Management (ASPM)** o el **`EnhancedPowerManagementEnabled`** en los hubs AMD (`VEN_1022`), el controlador USB entra en micro-estados de suspensión (`U1`/`U2`/`D3`) y corta la transferencia de imágenes grandes (`super.img`).
* **Solución aplicada en nuestra configuración especial:** Nuestro script desactiva automáticamente `ASPM` (`powercfg`), desactiva la Suspensión Selectiva USB y apaga `EnhancedPowerManagementEnabled` y `IdleInWorkingState` en todos los concentradores `PCI\VEN_1022` de tu B550.

### Causa D: Protección de Sobrecorriente ASUS (`Overcurrent Protection`) por los +5V del Cable Macho-a-Macho
* La **ASUS ROG STRIX B550-A GAMING** tiene fusibles poliméricos y protección electrónica contra voltaje inverso (+5V VBUS).
* Si al conectar el cable Macho-a-Macho notas que la PC ni siquiera hace sonido de conexión o sale aviso de *"Sobrecorriente en puerto USB"*, coloca un **pedacito de 2 mm de cinta aislante o diurex sobre el pin 1 (+5V, el contacto del extremo derecho mirando el conector USB macho de frente con los plásticos hacia abajo / contactos hacia arriba)** únicamente en la punta del cable que entra a tu PC, o conecta un **Hub USB 2.0 económico** entre la B550-A Gaming y el cable Macho-a-Macho.

---

## 3. Cómo Aplicar la Configuración Especial en tu PC (ASUS ROG STRIX B550-A GAMING)

1. En tu PC con la **ASUS ROG STRIX B550-A GAMING**, descarga o actualiza este repositorio (`git pull`).
2. Entra a la carpeta [`DRIVERS_USB_MACHO_A_MACHO_COMPLETOS/`](file:///C:/Users/andre/OneDrive/Desktop/xview/DRIVERS_USB_MACHO_A_MACHO_COMPLETOS) y ejecuta como Administrador:
   👉 **[`EJECUTAR_EN_MI_PC_ASUS_ROG_STRIX_B550.bat`](file:///C:/Users/andre/OneDrive/Desktop/xview/DRIVERS_USB_MACHO_A_MACHO_COMPLETOS/EJECUTAR_EN_MI_PC_ASUS_ROG_STRIX_B550.bat)**
3. **Reinicia tu PC una vez** (para que Windows recargue la tabla `usbflags` con `SkipBOSDescriptorQuery` en el controlador AMD xHCI y aplique la desactivación de HVCI).
4. Conecta el cable USB Macho-a-Macho en el **puerto USB 2.0 NEGRO trasero (el que está junto al puerto `BIOS`, sin el recuadro `BIOS`)**.

### (Opcional) Ajustes Recomendados en el BIOS UEFI de tu ASUS ROG STRIX B550-A GAMING
Si entras al BIOS de tu placa (`Supr` / `F2` -> `Advanced Mode (F7)`):
* **`Advanced` -> `USB Configuration`:**
  * `Legacy USB Support` -> **`[Enabled]`**
  * `XHCI Hand-off` -> **`[Enabled]`**
* **`Advanced` -> `APM Configuration`:**
  * `ErP Ready` -> **`[Disabled]`**
* **`Boot` -> `Secure Boot`:**
  * `OS Type` -> **`[Other OS]`** (Permite que `libusb0.sys` de `WorldCup_Device` cargue sin restricciones de firma WHQL de Windows 11).
