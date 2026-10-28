# Expediente Regulatorio FCC ID: `Q78-ZXV10B820CA15` (`ZTE ZXV10 B820C-A15 LE`)

Registro técnico de los documentos presentados por **ZTE Corporation** ante la **Federal Communications Commission (FCC)** de Estados Unidos para el decodificador híbrido **ZXV10 B820C-A15 / B820C-A15 LE**.

---

## 1. Datos de Registro Oficial (OET Equipment Authorization)

| Campo Regulatorio | Valor Registrado |
| :--- | :--- |
| **FCC ID Principal** | **`Q78-ZXV10B820CA15`** |
| **FCC ID Secundario Relacionado** | **`Q78-B820CA15`** |
| **Grantee Code (Fabricante)** | `Q78` (**ZTE Corporation**, ZTE Plaza, Keji Road South, Hi-Tech Industrial Park, Nanshan District, Shenzhen, China) |
| **Equipment Product Code** | `-ZXV10B820CA15` |
| **Nombre del Equipo** | **Hybrid STB (Set-Top Box) / RichMedia Box** |
| **Modelo Evaluado** | `ZXV10 B820C-A15` (`B820C-A15 LE`) |
| **Fecha de Concesión (Grant Date)** | **18 de marzo de 2020** |
| **Reglas FCC Aplicadas** | `15C` (DTS / Bluetooth LE & Wi-Fi 2.4 GHz), `15E` (U-NII Wi-Fi 5 GHz con DFS) |

---

## 2. Tabla de Frecuencias, Potencia RF y Modulación Certificadas

| Subsistema de Radio | Rango de Frecuencias (MHz) | Modulación / Estándar | Configuración de Antenas |Notas del Reporte de Pruebas FCC |
| :--- | :--- | :--- | :--- | :--- |
| **Bluetooth Classic / EDR** | `2402.0 – 2480.0 MHz` | GFSK, $\pi/4$-DQPSK, 8DPSK (79 canales) | 1T1R Interna en PCB | Usado para audio inalámbrico, mandos Bluetooth HID y emparejamiento inicial |
| **Bluetooth Low Energy (BLE)** | `2402.0 – 2480.0 MHz` | GFSK (40 canales, espaciado 2 MHz) | 1T1R Interna en PCB | Usado por el control remoto de voz con Google Assistant (`B820C-A15 LE`) |
| **Wi-Fi 2.4 GHz (WLAN)** | `2412.0 – 2462.0 MHz` (Canales 1–11) | IEEE 802.11b/g/n (HT20 / HT40) | **2x2 MIMO** (2 antenas internas PIFA/estampadas) | Soporte de coexistencia BT/WLAN en chip combo |
| **Wi-Fi 5.0 GHz U-NII-1** | `5180.0 – 5240.0 MHz` | IEEE 802.11a/n/ac (VHT20 / VHT40 / VHT80) | **2x2 MIMO** | Ancho de banda de canal de hasta 80 MHz (Wi-Fi 5) |
| **Wi-Fi 5.0 GHz U-NII-2A / 2C (DFS)** | `5260.0 – 5320.0 MHz` y `5500.0 – 5700.0 MHz` | IEEE 802.11a/n/ac (VHT20/40/80) | **2x2 MIMO** | Opera como **Cliente DFS sin detección de radar** (Dynamic Frequency Selection + TPC) |
| **Wi-Fi 5.0 GHz U-NII-3** | `5745.0 – 5825.0 MHz` | IEEE 802.11a/n/ac (VHT20/40/80) | **2x2 MIMO** | Banda alta de 5 GHz para streaming 4K de baja interferencia |
| **Sintonizador Coaxial RF (Recepción)** | `54.0 – 860.0 MHz` (Cable CATV) | **DVB-C / ITU-T J.83 Annex A/B (QAM 64 / QAM 256)** | Conector Coaxial Tipo F (75 $\Omega$) con blindaje EMI metálico | Receptor pasivo evaluado bajo FCC Part 15B (Unintentional Radiator) |

---

## 3. Inventario de Exhibiciones (Exhibits) del Expediente `Q78-ZXV10B820CA15`

1. **`Internal Photos` (~1.2 MB PDF)**:
   * Muestra el chasis abierto del `ZXV10 B820C-A15`:
     * Disipador térmico metálico sobre el SoC **Amlogic S905X2**.
     * **Jaula de blindaje RF (Shielding Can)** soldada junto al conector coaxial Tipo F que aloja el sintonizador/demodulador **DVB-C / QAM**.
     * Dos antenas internas de doble banda (`2.4/5 GHz`) situadas en los bordes laterales del chasis plástico para recepción **2x2 MIMO**.
     * Bancos de memoria **DDR3** (2 GB totales) y chip **eMMC** BGA-153.
2. **`External Photos` (~1.0 MB PDF)**:
   * Carcasa cuadrada/rectangular negra mate con logotipo del operador/ZTE, indicador LED bicolor frontal (Rojo/Verde), botón de encendido físico, y panel posterior con:
     * Entrada **RF IN (Cable Coaxial)**
     * Salida **Mini-AV (3.5 mm TRRS)**
     * Salida **HDMI 2.1 (TMDS 4K@60Hz)**
     * Puerto **LAN RJ-45 (Ethernet)**
     * Entrada de alimentación **DC 12V**
     * Puerto lateral **USB 2.0**
3. **`User Manual` (PDF)**:
   * Guía de instalación híbrida (conexión simultánea de cable coaxial CATV + red Ethernet/Wi-Fi + HDMI al televisor), instrucciones de seguridad eléctrica (adaptador externo 12V DC) y vinculación del control remoto Bluetooth LE.
4. **`Label and Label Location` (PDF)**:
   * Etiqueta inferior adherida a la base del decodificador donde figura la nomenclatura exacta: **`ZTE ZXV10 B820C-A15 LE`**, número de serie (`S/N`), dirección MAC (`MAC`) y **`FCC ID: Q78-ZXV10B820CA15`**.
5. **`Test Reports` (5 documentos PDF)**:
   * *2.4G WLAN Test Report*
   * *5G WLAN Test Report*
   * *DFS Test Report (Dynamic Frequency Selection)*
   * *Bluetooth LE Test Report*
   * *RF Exposure Evaluation (MPE @ 20 cm)*
