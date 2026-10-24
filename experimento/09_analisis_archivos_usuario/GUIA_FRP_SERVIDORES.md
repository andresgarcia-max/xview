# 🚀 GUÍA DE INSTALACIÓN Y CONFIGURACIÓN: SCRCPY FUERA DE LA RED LOCAL (FRP WAN)

Esta guía te explica paso a paso cómo configurar tu servidor VPS y tus TV Box ZTE B866V2 para poder controlar las cajas en vivo a 60 FPS desde **cualquier lugar del mundo fuera de la red local** usando **Scrcpy + FRP (Fast Reverse Proxy)**.

---

## 📡 PASO 1: Configuración en tu Servidor VPS (Linux Ubuntu/Debian/CentOS)

1. Sube el archivo `frp/instalar_servidor_frp.sh` a tu VPS (por ejemplo vía SSH o SFTP).
2. O ejecuta los siguientes comandos directamente en la terminal de tu VPS:

```bash
# Otorgar permisos de ejecución
chmod +x instalar_servidor_frp.sh

# Ejecutar el instalador automático
sudo bash instalar_servidor_frp.sh
```

El script descargará la versión oficial de `frps`, creará el servicio para que inicie automáticamente cuando se encienda tu VPS y abrirá los puertos necesarios en el Firewall.

* **Puerto de Control FRP:** `7000`
* **Dashboard Web:** `http://TU_IP_VPS:7500` *(Usuario: admin | Password: admin123)*
* **Puertos para las Cajas:** `7001` al `7999`

---

## 📺 PASO 2: Configuración del Cliente en la TV Box (`frpc.toml`)

1. En tu PC, abre el archivo `z:\Modems\Xview\frp\frpc.toml` con un editor de texto.
2. Cambia `TU_IP_O_DOMINIO_SERVIDOR` por la dirección IP pública o subdominio de tu VPS:

```toml
serverAddr = "cajas.midominio.com"  # O la IP de tu VPS (ej: 190.12.34.56)
serverPort = 7000

auth.token = "XviewRemoteControl2026SecretKey"

[[proxies]]
name = "caja_01_scrcpy"
type = "tcp"
localIP = "127.0.0.1"
localPort = 5555
remotePort = 7001  # Asigna un puerto diferente a cada caja (7001, 7002, 7003...)
```

3. Abre el programa `Liberacion_Xview_B866V2_Portatil.exe` o `python liberacion_gui.py`.
4. Conecta la caja en red local y haz clic en **🌐 Activar Túnel FRP WAN**.

---

## 🖥️ PASO 3: Cómo conectarte con Scrcpy desde cualquier lugar (WAN)

Una vez que la caja tenga el túnel FRP activo y esté conectada a cualquier red Wi-Fi / Ethernet con acceso a Internet:

1. Abre la aplicación gráfica del proyecto en tu laptop/PC.
2. En la casilla **Dirección IP/Puerto**, escribe la IP/Dominio de tu VPS y el puerto asignado a esa caja.
   - Ejemplo: `cajas.midominio.com:7001` (o `190.12.34.56:7001`)
3. Haz clic en **🔌 Conectar IP**.
4. Haz clic en **🖥️ CONTROL REMOTO HD EN VIVO (SCRCPY)**.

¡Se abrirá inmediatamente la pantalla de la TV Box en HD a 60 FPS desde cualquier parte del mundo! 🚀
