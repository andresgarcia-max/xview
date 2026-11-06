# ==============================================================================
# AUTO_ACCESO_TOTAL_XVIEW.ps1
# Monitoreo pasivo (sin tocar endpoints WinUSB para no bloquear WUDFHost/MTP).
# 1. Si detecta COM8 (UART), envia comandos para habilitar MTP + ADB de inmediato.
# 2. Si detecta reconexion de USB\VID_18D1&PID_4EE*, abre Este equipo > Xview+
#    y si ADB esta activo (PID_4EE2 / PID_4EE7), conecta por ADB y lista todo.
# ==============================================================================

$ErrorActionPreference = "SilentlyContinue"
$adbExe = "c:\Users\andre\OneDrive\Desktop\xview\experimento\06_internet_y_repositorios\adb_tools\platform-tools\adb.exe"
$seenUsb = $false
$seenCom8 = $false

Write-Output "[$(Get-Date -Format 'HH:mm:ss')] Esperando reconexion de cable USB Macho-Macho o UART COM8..."

for ($i = 0; $i -lt 1200; $i++) {
    # 1. Revisar si conectaron COM8 (UART)
    $ports = [System.IO.Ports.SerialPort]::GetPortNames()
    if ($ports -contains "COM8" -and -not $seenCom8) {
        $seenCom8 = $true
        Write-Output "[$(Get-Date -Format 'HH:mm:ss')] [!] COM8 detectado! Activando MTP + ADB desde consola UART..."
        try {
            $sp = New-Object System.IO.Ports.SerialPort("COM8", 115200, "None", 8, "One")
            $sp.ReadTimeout = 1000
            $sp.WriteTimeout = 1000
            $sp.Open()
            $sp.Write("`r`n")
            Start-Sleep -Milliseconds 300
            # Enviar comandos para habilitar transferencia de archivos MTP y ADB en Android / Recovery
            $sp.Write("setprop persist.sys.usb.config mtp,adb`r`n")
            Start-Sleep -Milliseconds 200
            $sp.Write("setprop sys.usb.config mtp,adb`r`n")
            Start-Sleep -Milliseconds 200
            $sp.Write("svc usb setFunctions mtp`r`n")
            Start-Sleep -Milliseconds 500
            $out = $sp.ReadExisting()
            Write-Output "[COM8 Respuesta]: $out"
            $sp.Close()
        } catch {
            Write-Output "[COM8 Error]: $_"
        }
    }

    # 2. Revisar pasivamente PnP para Xview+ (VID_18D1)
    $usb = Get-PnpDevice -PresentOnly | Where-Object { $_.InstanceId -match "VID_18D1" }
    if ($usb -and -not $seenUsb) {
        $seenUsb = $true
        foreach ($d in $usb) {
            Write-Output "[$(Get-Date -Format 'HH:mm:ss')] [+] USB Conectado: $($d.FriendlyName) | $($d.InstanceId) | Class: $($d.Class)"
        }
        Start-Sleep -Seconds 2
        $shell = New-Object -ComObject Shell.Application
        $thisPC = $shell.NameSpace(17)
        $xviewItem = $thisPC.Items() | Where-Object { $_.Name -match "Xview" }
        if ($xviewItem) {
            $xviewItem.InvokeVerb("open")
            Write-Output "[$(Get-Date -Format 'HH:mm:ss')] [+] Abierta ventana limpia 'Este equipo > Xview+'."
        }
        if (Test-Path $adbExe) {
            $adbOut = & $adbExe devices -l 2>&1
            Write-Output "[ADB]: $adbOut"
        }
        break
    }
    Start-Sleep -Milliseconds 500
}
