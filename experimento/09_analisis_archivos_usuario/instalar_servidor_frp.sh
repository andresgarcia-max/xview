#!/bin/bash
# Script de Instalación Automática de FRP Server (frps) en Linux VPS (Ubuntu/Debian/CentOS)
# Ejecutar como root: sudo bash instalar_servidor_frp.sh

echo "======================================================================"
echo "   INSTALADOR DE SERVIDOR FRP PARA CONTROL REMOTO DE CAJAS XVIEW      "
echo "======================================================================"
echo ""

# 1. Crear directorio de instalación
mkdir -p /etc/frp
mkdir -p /tmp/frp_install

# 2. Descargar última versión de FRP (v0.58.0 Linux 64-bit)
echo "[1/4] Descargando binario FRP Server..."
curl -sSL https://github.com/fatedier/frp/releases/download/v0.58.0/frp_0.58.0_linux_amd64.tar.gz -o /tmp/frp_install/frp.tar.gz

if [ $? -ne 0 ]; then
    echo "[ERROR] Falló la descarga de FRP. Verifica la conexión a Internet de la VPS."
    exit 1
fi

tar -xzf /tmp/frp_install/frp.tar.gz -C /tmp/frp_install/
cp /tmp/frp_install/frp_0.58.0_linux_amd64/frps /usr/local/bin/
chmod +x /usr/local/bin/frps

# 3. Crear archivo de configuración /etc/frp/frps.toml
echo "[2/4] Creando configuración en /etc/frp/frps.toml..."
cat << 'EOF' > /etc/frp/frps.toml
bindPort = 7000

# Token de seguridad para las cajas (Personaliza si lo deseas)
auth.token = "XviewRemoteControl2026SecretKey"

# Dashboard Web de control
webServer.addr = "0.0.0.0"
webServer.port = 7500
webServer.user = "admin"
webServer.password = "admin123"
EOF

# 4. Crear Servicio Systemd para inicio automático
echo "[3/4] Creando servicio systemd (frps.service)..."
cat << 'EOF' > /etc/systemd/system/frps.service
[Unit]
Description=FRP Server for Remote Scrcpy Box Control
After=network.target

[Service]
Type=simple
ExecStart=/usr/local/bin/frps -c /etc/frp/frps.toml
Restart=on-failure
RestartSec=5s

[Install]
WantedBy=multi-user.target
EOF

# 5. Recargar y Activar Servicio
echo "[4/4] Iniciando servicio..."
systemctl daemon-reload
systemctl enable frps
systemctl restart frps

# Abrir puertos en UFW si está activo
if command -v ufw > /dev/null; then
    ufw allow 7000/tcp
    ufw allow 7500/tcp
    ufw allow 7001:7999/tcp
    echo "[INFO] Puertos 7000, 7500 y 7001-7999 abiertos en UFW firewall."
fi

echo ""
echo "======================================================================"
echo "   ¡SERVIDOR FRP INSTALADO Y EN EJECUCIÓN CON ÉXITO!"
echo "======================================================================"
echo " - Puerto de Control FRP: 7000"
echo " - Panel Web Dashboard: http://TU_IP_VPS:7500 (User: admin / Pass: admin123)"
echo " - Puertos asignables para Cajas: 7001 al 7999"
echo "======================================================================"
