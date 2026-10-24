# Debian for the B866V2 with working Wi-Fi and Bluetooth

[devmfc's Debian images](https://github.com/devmfc/debian-on-amlogic) have no
working support for the B866V2's **MediaTek MT7661LSN** Wi-Fi/Bluetooth chip:
the base kernel and device tree cannot talk to it, so there is no Wi-Fi and no
Bluetooth.

This repository is a patched version of devmfc's images and kernel that adds
support for the B866V2's MT7661LSN, so both Wi-Fi and Bluetooth work.

## How to use it

1. Download the compressed image (`*-b866v2.img.xz`) from the [Releases](../../releases) page.
2. Flash it to a USB stick (for example with balenaEtcher, which reads `.img.xz`
   directly) and boot the B866V2 from it. You get Debian with Wi-Fi and
   Bluetooth already working, with nothing else to install.
3. Log in as `root` (the default password is shown in the login banner).
   To connect to Wi-Fi run `/root/wifi-connect.sh`.
4. To install Debian to the internal eMMC, run these two scripts from `/root`,
   in this order:

   ```sh
   cd /root
   ./aml-multiboot-setup.sh      # one-off U-Boot setup, see the warning below
   ./aml-install-to-emmc.sh      # copies the system to the eMMC
   ```

   `aml-install-to-emmc.sh` refuses to run until multiboot setup has been done and **overwrites the eMMC** once it executes.

If the box ignores the USB stick the first time, boot it from the stick by your
usual method (UART/U-Boot commands, or a box that already has multiboot set up).
To undo the multiboot setup later, restore the saved U-Boot variables:

```sh
fw_setenv preboot "$(fw_printenv -n preboot_org)"
fw_setenv bootcmd "$(fw_printenv -n cmd_boot_org)"
```

### Booting the image on the B866V2: multiboot setup (read this first)

> **WARNING / DISCLAIMER. USE AT YOUR OWN RISK.** The multiboot setup rewrites
> the U-Boot environment stored on the box's eMMC (`bootcmd`, `preboot`). A
> mistake, a power cut during the write, a wrong offset or a different board
> revision can leave the box unable to boot (bricked). **The author of this
> project accepts no responsibility or liability for any damage, including a
> bricked box, data loss or hardware failure, however it happens. You do this
> entirely on your own responsibility.** Only continue if you have a way to
> recover (UART serial console, a USB burning tool and the stock firmware).

That said, it should work fine, so don't worry.

## Limitation

SD_EMMC_B is the microSD controller on the generic device tree. With this
device tree the **microSD slot is not available** (eMMC and USB are unaffected).

## Credits and licenses

* Kernel patches: ophub/amlogic-s9xxx-armbian PR #3659 ("W103D Project"),
  GPL-2.0 like the kernel. 0007 and the rebase are from this project.
* Firmware: MediaTek, redistributed under `LICENCE.mediatek` (downloaded with the
  firmware and installed to `/usr/share/doc/b866v2-mt7663-support/`).
* devmfc for the images and kernel packaging.
