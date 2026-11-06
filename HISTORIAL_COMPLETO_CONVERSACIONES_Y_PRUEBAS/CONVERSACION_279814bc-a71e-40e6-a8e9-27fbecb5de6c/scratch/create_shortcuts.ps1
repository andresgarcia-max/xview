$targetDir = "c:\Users\andre\OneDrive\Desktop\gym\accesos directos"
$iconPath = "c:\Users\andre\OneDrive\Desktop\gym\LEGENDS\build\icon.ico"
$wsh = New-Object -ComObject WScript.Shell

$shortcuts = @(
    @{
        Name = "1. Sistema ERP Administrador.lnk"
        Target = "$targetDir\1. INICIAR_SISTEMA_ADMINISTRADOR_ERP.bat"
        Description = "Fitness Legends - Sistema ERP Central para Administrador"
    },
    @{
        Name = "2. Terminal Torniquete Acceso.lnk"
        Target = "$targetDir\2. INICIAR_TERMINAL_TORNIQUETE.bat"
        Description = "Fitness Legends - Terminal de Control de Acceso para Torniquete"
    },
    @{
        Name = "3. Cartera de Clientes.lnk"
        Target = "$targetDir\3. ACCESO_CARTERA_DE_CLIENTES.bat"
        Description = "Fitness Legends - Cartera de Clientes y Membresias"
    },
    @{
        Name = "4. Vincular Equipos en Red LAN.lnk"
        Target = "$targetDir\4. SERVIDOR_SINCRONIZACION_LAN_(VINCULAR_EQUIPOS).bat"
        Description = "Fitness Legends - Servidor de Sincronizacion para conectar Torniquete y Admin"
    }
)

foreach ($sc in $shortcuts) {
    $lnkPath = Join-Path $targetDir $sc.Name
    $shortcut = $wsh.CreateShortcut($lnkPath)
    $shortcut.TargetPath = $sc.Target
    $shortcut.WorkingDirectory = $targetDir
    $shortcut.Description = $sc.Description
    if (Test-Path $iconPath) {
        $shortcut.IconLocation = $iconPath
    }
    $shortcut.Save()
    Write-Host "Created shortcut: $($sc.Name)"
}
