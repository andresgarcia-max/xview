# Análisis Forense y Comparativo de Placas Base: `STBAR_C` (2019) vs. `STBAR_J` (2022+)

Este documento analiza al detalle las fotografías de alta resolución agregadas en la carpeta [`diagramas/`](./), donde se comparan **dos revisiones físicas distintas** de la tarjeta lógica de la familia **ZTE ZXV10 B866 (Xview+)** que comparten exactamente el mismo molde mecánico (`71162`), recortes de chasis y posición de puertos, pero cambian radicalmente sus componentes internos (RAM, eMMC, módulo Wi-Fi/BT, disipación y enrutamiento de pistas).

---

## 1. Tabla Comparativa Directa de Hardware (Extraída de las Fotografías)

| Componente / Serigrafía | Placa Revisión A: **`STBAR_C`** (Fotos `PM.jpeg`, `(1)`, `(2)`, `(3)`) | Placa Revisión B: **`STBAR_J`** (Fotos `(4)`, `(5)`, `(6)`) | Impacto en Firmware y Modificación |
| :--- | :--- | :--- | :--- |
| **Código de Revisión (Silkscreen)** | **`STBAR_C`** / `190602` / `71162` | **`STBAR_J`** / `200400` / `71162` | Diferente revisión de ingeniería sobre el mismo chasis `71162`. |
| **Fabricante de PCB y Fecha** | `T-M1 E254667 S` (`H4E0670180 101`) — **Junio 2019 (`1906`)** | `ASKPCB E239218` (`06360111B`) — **Marzo 2022 (`2212 : 04-02`)** | 3 años de diferencia entre ambos lotes de producción. |
| **Etiqueta de Serie / QR** | Código de barras lineal: `WPCC00B1304223` | Matriz QR: `740277 008333 HSL6` | Formato de trazabilidad actualizado en lotes 2022+. |
| **Disipador del Procesador (SoC)** | **Aluminio plateado con aletas verticales** (`D1A12`) | **Bloque plano negro cerámico / anodizado sin aletas** | La placa `STBAR_J` reduce altura y costo del disipador. |
| **Topología de Memoria RAM (2 GB)** | **4 chips en total (2 arriba + 2 abajo)** | **2 chips en total (2 arriba + 0 abajo; pads inferiores vacíos)** | Requiere tabla de entrenamiento DDR (`BL2` en `bootloader.img`) compatible con la densidad de fila/columna. |
| **Marca y Modelo de Chips RAM** | **UnilC (`Unic`) `SCB13H4G160AF-11M`** (`MN5823C46-P` / `MN6708C46-P`, 4×512 MB DDR3) | **CXMT `CXDQ3BFAM-CG`** (`AP122024800000` lote `2134`, 2×1 GB DDR4) | Cambio completo de tecnología de memoria: de 4 chips UnilC a 2 chips CXMT de alta densidad. |
| **Memoria Flash eMMC 5.1 (8 GB)** | **SanDisk `SDINBDG4-8G`** (ubicada en `D5A12`, punto amarillo) | **Samsung `KLM8G1GETF-B041`** (`SEC 134 B041`, ubicada en `D1A6`) | Distinta nomenclatura de componentes en placa (`A12` vs `A6`) y distinto tiempos de respuesta eMMC. |
| **Chip Wi-Fi / Bluetooth** | **MediaTek `MT7668RSN`** (`1946-BZAAL`) **soldado directo al PCB (BGA)** | **MediaTek `MT7661RSN`** (`2130-BZJEL`) **montado sobre módulo PCB almenado (*castellated*)** | **Crítico:** Aunque ambos son MediaTek serie `mt766x`, `MT7668RSN` y `MT7661RSN` usan IDs de dispositivo y archivos de firmware NVRAM/BT distintos. |
| **Sistema de Antenas Wi-Fi/BT** | **Cable coaxial negro externo** soldado al pad `X9` + antena PCB | **100% antenas impresas en PCB** (sin cable coaxial externo) | Simplificación de ensamblaje en la revisión `STBAR_J`. |
| **Transformador LAN Ethernet** | `PPT 2027Q PSF-1626` | `PPT 2114Q PSF-1626` | Mismo aislamiento magnético 10/100 Mbps (`PSF-1626`). |
| **Puerto Serial UART (4 pines)** | `X1A12` (Esquina inferior derecha, cara superior) | `X1A6` / `L1A19` (Misma esquina inferior derecha, cara superior) | Pinout UART idéntico (`115200 8N1`: Pin 1 cuadrado = `3.3V`, `TX`, `RX`, `GND`). |

---

## 2. Identificación Visual por Fotografía

### A. Placa Revisión A — `STBAR_C` (Lote `190602` / Año 2019–2020)
* **Archivos:**
  * [`WhatsApp Image 2026-10-26 at 5.22.56 PM.jpeg`](./WhatsApp%20Image%202026-10-26%20at%205.22.56%20PM.jpeg) y [`(1).jpeg`](./WhatsApp%20Image%202026-10-26%20at%205.22.56%20PM%20(1).jpeg) *(Cara Inferior)*
  * [`WhatsApp Image 2026-10-26 at 5.22.56 PM (2).jpeg`](./WhatsApp%20Image%202026-10-26%20at%205.22.56%20PM%20(2).jpeg) y [`(3).jpeg`](./WhatsApp%20Image%202026-10-26%20at%205.22.56%20PM%20(3).jpeg) *(Cara Superior)*
* **Detalles Forenses Clave:**
  1. **RAM en ambas caras:** Al voltear la tarjeta (`STBAR_C`), los dos recuadros dentro del marco naranja tienen soldados dos chips **Unic `SCB13H4G160AF-11M`**, y en la cara superior junto al disipador de aletas plateado hay otros dos chips idénticos.
  2. **Wi-Fi BGA directo + Cable Coaxial:** El chip **MediaTek `MT7668RSN`** está soldado directamente sobre la placa verde y tiene un cable negro de antena soldado en el borde izquierdo.
  3. **Nomenclatura de serigrafía terminada en `A12`:** Todos los componentes cercanos al procesador llevan el sufijo `A12` (`D1A12`, `D5A12`, `C80A12`, `X1A12`).

### B. Placa Revisión B — `STBAR_J` (Lote `200400` / `ASKPCB 2212` / Año 2022+)
* **Archivos:**
  * [`WhatsApp Image 2026-10-26 at 5.22.56 PM (4).jpeg`](./WhatsApp%20Image%202026-10-26%20at%205.22.56%20PM%20(4).jpeg) y [`(5).jpeg`](./WhatsApp%20Image%202026-10-26%20at%205.22.56%20PM%20(5).jpeg) *(Cara Inferior)*
  * [`WhatsApp Image 2026-10-26 at 5.22.56 PM (6).jpeg`](./WhatsApp%20Image%202026-10-26%20at%205.22.56%20PM%20(6).jpeg) *(Cara Superior)*
* **Detalles Forenses Clave:**
  1. **Bancos de RAM inferiores vacíos:** En la cara inferior (`STBAR_J`), dentro del recuadro naranja se ven claramente las dos parrillas de puntos BGA vacías sin chips soldados. Toda la memoria RAM está arriba usando **2 chips `CXMT CXDQ3BFAM-CG`** de mayor densidad.
  2. **Módulo Wi-Fi tipo estampilla (`MT7661RSN`):** El chip Wi-Fi/BT viene montado sobre una pequeña placa hija cuadrada soldada en diagonal por sus bordes (*castellated pads*), coincidiendo exactamente con el chip reportado en la extracción de tu sistema (`MT7661LSN`/`MT7661RSN`, driver `mt7663`).
  3. **Memoria eMMC Samsung (`KLM8G1GETF-B041`):** Reemplaza a la SanDisk del modelo viejo.
  4. **Nomenclatura de serigrafía terminada en `A6`:** En esta revisión, ZTE re-enumeró el esquemático interno del SoC/RAM/eMMC con el sufijo `A6` (`D1A6`, `C96A6`, `R29A6`).

---

## 3. Conclusión Técnica: ¿Qué significa esto al flashear o adaptar Firmware?

1. **Tu extracción del repositorio corresponde a la arquitectura `STBAR_J` (con chip `MT7661RSN`):**
   * En los registros extraídos de tu caja ([`ENCICLOPEDIA_XVIEW_ZTE_B866_Y_VARIANTES_2027.md`](../experimento/06_internet_y_repositorios/ENCICLOPEDIA_XVIEW_ZTE_B866_Y_VARIANTES_2027.md)), el sistema detecta el controlador **MediaTek `MT7661` (driver `mt7663`)**, que es exactamente el módulo soldado en la placa **`STBAR_J`** (Fotos 4 y 5).
2. **Por qué ciertas ROMs dejan sin Wi-Fi/Bluetooth a una de las dos placas:**
   * Aunque ambas placas entran en la misma carcasa plástica (`71162`), la placa **`STBAR_C`** lleva un **`MT7668RSN`** y la placa **`STBAR_J`** lleva un **`MT7661RSN`**. Si instalas una ROM cuyo `/vendor/firmware` o `dtb.img` solo incluye la configuración para `MT7668`, la placa `STBAR_J` arranca pero no activa el Wi-Fi ni el control remoto Bluetooth (y viceversa).
3. **Peligro con `bootloader.img` antiguos en placas con RAM `CXMT` (`STBAR_J`):**
   * Un `bootloader.img` muy antiguo (compilado en 2019 solo para los 4 chips DDR3 `Unic` de la placa `STBAR_C`) puede fallar al inicializar los 2 chips DDR4 `CXMT` de la placa `STBAR_J` (2022). El `bootloader.img` incluido en tu carpeta [`V2/B866v2 Android 12`](../V2/B866v2%20Android%2012) (compilación finales de 2023) ya incluye en su etapa `BL2` soporte multimemoria tanto para DDR3 (`Unic`) como para DDR4 (`CXMT`).
