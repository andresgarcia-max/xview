# ZTE ZXV10 B826C-A12 — STB Security Research

> Megacable Mexico hybrid decoder running locked Android on Hisilicon Hi3798M.  
> Goal: recover root/ADB access via UART and repurpose hardware as a hacking platform.

---

## Target

| Field | Value |
|---|---|
| Device | ZTE ZXV10 B826C-A12 |
| Vendor | ZTE Corporation |
| Operator | Megacable México |
| SoC | Hisilicon Hi3798M (ARM Cortex-A53, 64-bit) |
| OS | Android (operator-locked, no PlayStore, no ADB) |
| Board ID | DVBCUA 72358 |
| Chip ID | 1002309C4FD33BCD |

---

## Hypothesis

The device runs a standard Android build with Megacable's restrictions enforced at the software layer — a locked launcher, disabled ADB, and no access to system settings. The underlying Hisilicon Hi3798M bootloader is expected to expose a UART interface pre-OS that predates operator customizations, giving us a foothold before Android's lockdown kicks in.

**Assumptions:**
- Lock is software-only (launcher overlay + build flags), not hardware fuses
- Hi3798M U-Boot is accessible via UART at 115200 baud
- TP11/TP12 test points on the PCB are UART pads (TX/RX)
- ADB is disabled via `build.prop` or system flag, not kernel-level

**What could break this:**
- Secure boot enabled on the Hi3798M (supported by the SoC)
- UART TX-only (output but no input accepted)
- eFuses blown by Megacable to lock fastboot

---

## Attack Surface

```
[Physical layer]
  └── UART pads (TP11, TP12 on PCB)
        └── Bootlog analysis
              └── U-Boot interrupt attempt
                    ├── fastboot mode
                    └── ADB enable via bootloader flag

[Software layer]
  └── Android build.prop
        └── ro.adb.secure / ro.debuggable flags
              └── ADB over USB / WiFi
```

---

## Hardware Setup

**Required:**
- USB-UART adapter (CP2102, CH340, or FT232)
- Precision test probes (SMD-compatible, fine-tip hook clips)
- Multimeter (DC voltage mode to identify TX/RX)
- Kapton tape (to hold probes stable during boot)

**Connection (standard UART crossover):**

```
TV Box      USB-UART Adapter
  TX   ──►   RX
  RX   ◄──   TX
  GND  ───   GND
```

> ⚠️ Do NOT connect VCC — board is self-powered. Connecting 3.3V from the adapter may damage the board.

**Baud rate:** 115200

---

## Methodology

### Phase 1 — UART Access
1. Identify TX, RX, GND on TP11/TP12 using multimeter (TX fluctuates ~3.3V on boot, RX stable at 3.3V, GND at 0V)
2. Connect USB-UART adapter with precision probes
3. Open serial terminal on host machine at 115200 baud
4. Power on device and capture full bootlog

### Phase 2 — Bootlog Analysis
1. Identify bootloader type (U-Boot expected)
2. Check for interrupt window (`Ctrl+C`, keypress, or autoboot countdown)
3. Map available U-Boot commands
4. Identify Android partition layout and security flags

### Phase 3 — ADB / Root
1. Attempt ADB enable via bootloader command or build flag patch
2. If fastboot accessible — flash permissive boot image
3. If UART input blocked — research Hi3798M-specific exploits for this firmware version

---

## Host Setup (macOS)

```bash
# List connected serial devices
ls /dev/tty.*

# Connect to UART
screen /dev/tty.usbserial-XXXX 115200

# Exit screen session
Ctrl+A then K
```

---

## Research Resources

- XDA Developers — search: `ZTE B826`, `Hi3798M`
- GitHub — search: `Hi3798M uart`, `hisilicon stb exploit`
- 4PDA (Russian STB community) — largest Hi3798M knowledge base
- Hisilicon Hi3798M datasheet (for UART pin mapping)

---

## Status

- [x] Device identified: ZTE ZXV10 B826C-A12
- [x] SoC identified: Hisilicon Hi3798M
- [x] PCB inspected, test points located (TP11, TP12)
- [ ] USB-UART adapter + precision probes acquired
- [ ] UART connection established
- [ ] Bootlog captured
- [ ] Bootloader analyzed
- [ ] ADB / root access obtained

---

## End Goal

Full root access or ADB shell on the device with the operator lock bypassed. Hardware repurposed as an ARM64 hacking platform or network analysis node. All findings documented for portfolio and potential writeup.

---

*Research conducted for educational purposes and hardware ownership rights.*
