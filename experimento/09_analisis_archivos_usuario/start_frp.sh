# Script de Persistencia FRP para TV Box Android
# Guarda y asegura que frpc arranque al iniciar el sistema o conectarse a Wi-Fi

nohup /data/local/tmp/frpc -c /data/local/tmp/frpc.toml > /dev/null 2>&1 &
