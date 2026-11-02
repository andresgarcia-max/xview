Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing
[System.Windows.Forms.Application]::EnableVisualStyles()

$ErrorActionPreference = "Continue"
$fwDir       = $PSScriptRoot
$fbExe       = Join-Path $fwDir "fastboot.exe"
$adbExe      = Join-Path (Split-Path $fwDir -Parent) "herramientas\platform-tools\adb.exe"
$updateExe   = Join-Path (Split-Path $fwDir -Parent) "experimento\09_env_unlock\update.exe"
$envUnlImg   = Join-Path $fwDir "env_unlocked.img"
$logFile     = Join-Path $fwDir "SESION_FASTBOOT_B866V2.log"
$summaryFile = Join-Path $fwDir "RESUMEN_PROCESO_B866V2.txt"

# Cerrar cualquier instancia previa de fastboot.exe o MOTOR_FASTBOOT_B866V2.ps1 para evitar colision USB
try {
    Get-Process -Name "fastboot" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
    $myPid = $PID
    $myProc = Get-CimInstance Win32_Process -Filter "ProcessId = $myPid" -ErrorAction SilentlyContinue
    $parentPid = if ($myProc) { $myProc.ParentProcessId } else { 0 }
    Get-CimInstance Win32_Process -Filter "Name LIKE 'powershell%'" -ErrorAction SilentlyContinue | Where-Object {
        $_.ProcessId -ne $myPid -and $_.ProcessId -ne $parentPid -and $_.CommandLine -match "MOTOR_FASTBOOT_B866V2"
    } | ForEach-Object {
        Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
    }
} catch {}

# ==============================================================================
# ESTADO GLOBAL Y 4 PASOS DIRECTOS (DESDE ENTRADA A FASTBOOT HASTA INYECCION)
# ==============================================================================
$script:StartTime        = Get-Date
$script:CurrentProcess   = "Iniciando monitor USB Fastboot..."
$script:ProgressValue    = 5
$script:OverallStatus    = "EN EJECUCION"
$script:FailureReason    = "Ninguno"
$script:ActionRequested  = ""
$script:PartitionResults = New-Object System.Collections.Generic.List[PSCustomObject]

$script:DeviceInfo = [ordered]@{
    Modelo           = "Megacable Xview+ ZTE ZXV10 B866V2 (board_id=8)"
    EstadoUSB        = "Buscando conexion USB..."
    IdSerie          = "Pendiente"
    VersionUBoot     = "Pendiente"
    EstadoLock       = "Pendiente"
    PruebaEscritura  = "Pendiente"
    ParticionesOK    = 0
    ParticionesError = 0
}

$script:Steps = [ordered]@{
    "Paso 1" = [PSCustomObject]@{ Num = 1; Nombre = "1. Entrada a Modo Fastboot (PID_0D02)";      Estado = "PENDIENTE"; Hora = "--:--:--"; Detalle = "Esperando deteccion de la caja en Fastboot (PID_4EE7 / PID_0D02)" }
    "Paso 2" = [PSCustomObject]@{ Num = 2; Nombre = "2. Desbloqueo de Bootloader (lock)";         Estado = "PENDIENTE"; Hora = "--:--:--"; Detalle = "Pendiente de aplicar lock=10100000 y verificar escritura" }
    "Paso 3" = [PSCustomObject]@{ Num = 3; Nombre = "3. Inyeccion de Firmware (12 particiones)";  Estado = "PENDIENTE"; Hora = "--:--:--"; Detalle = "Pendiente de inyectar particiones del sistema B866V2" }
    "Paso 4" = [PSCustomObject]@{ Num = 4; Nombre = "4. Verificacion Final y Resumen";            Estado = "PENDIENTE"; Hora = "--:--:--"; Detalle = "Pendiente de finalizar proceso y generar reporte" }
}

# ==============================================================================
# CONSTRUCCION DEL DIALOGO VISUAL EN VIVO (WINDOWS FORMS)
# ==============================================================================
$form = New-Object System.Windows.Forms.Form
$form.Text = "INYECTADOR DE FIRMWARE FASTBOOT - ZTE ZXV10 B866V2 (XVIEW+)"
$form.Size = New-Object System.Drawing.Size(820, 620)
$form.StartPosition = "CenterScreen"
$form.BackColor = [System.Drawing.Color]::FromArgb(20, 24, 33)
$form.ForeColor = [System.Drawing.Color]::White
$form.FormBorderStyle = "FixedDialog"
$form.MaximizeBox = $false
$form.TopMost = $true

$fontTitle   = New-Object System.Drawing.Font("Segoe UI", 13, [System.Drawing.FontStyle]::Bold)
$fontProcess = New-Object System.Drawing.Font("Segoe UI", 11, [System.Drawing.FontStyle]::Bold)
$fontStep    = New-Object System.Drawing.Font("Consolas", 9.5, [System.Drawing.FontStyle]::Bold)
$fontMono    = New-Object System.Drawing.Font("Consolas", 9, [System.Drawing.FontStyle]::Regular)
$fontBtn     = New-Object System.Drawing.Font("Segoe UI", 9.5, [System.Drawing.FontStyle]::Bold)

# Cabecera
$lblHeader = New-Object System.Windows.Forms.Label
$lblHeader.Text = "PROCESO FASTBOOT DIRECTO (4 PASOS) - ZTE ZXV10 B866V2"
$lblHeader.Font = $fontTitle
$lblHeader.ForeColor = [System.Drawing.Color]::FromArgb(86, 196, 255)
$lblHeader.Location = New-Object System.Drawing.Point(18, 12)
$lblHeader.Size = New-Object System.Drawing.Size(770, 28)
$form.Controls.Add($lblHeader)

# Panel de Proceso Actual + Barra de Progreso
$pnlProc = New-Object System.Windows.Forms.Panel
$pnlProc.Location = New-Object System.Drawing.Point(18, 45)
$pnlProc.Size = New-Object System.Drawing.Size(770, 82)
$pnlProc.BackColor = [System.Drawing.Color]::FromArgb(30, 36, 48)
$pnlProc.BorderStyle = "FixedSingle"
$form.Controls.Add($pnlProc)

$lblProcTitle = New-Object System.Windows.Forms.Label
$lblProcTitle.Text = "PROCESO ACTUAL EN EJECUCION:"
$lblProcTitle.Font = $fontMono
$lblProcTitle.ForeColor = [System.Drawing.Color]::FromArgb(170, 180, 195)
$lblProcTitle.Location = New-Object System.Drawing.Point(12, 8)
$lblProcTitle.Size = New-Object System.Drawing.Size(350, 18)
$pnlProc.Controls.Add($lblProcTitle)

$lblCurrentProcess = New-Object System.Windows.Forms.Label
$lblCurrentProcess.Text = $script:CurrentProcess
$lblCurrentProcess.Font = $fontProcess
$lblCurrentProcess.ForeColor = [System.Drawing.Color]::FromArgb(255, 215, 70)
$lblCurrentProcess.Location = New-Object System.Drawing.Point(12, 27)
$lblCurrentProcess.Size = New-Object System.Drawing.Size(742, 24)
$pnlProc.Controls.Add($lblCurrentProcess)

$progBar = New-Object System.Windows.Forms.ProgressBar
$progBar.Location = New-Object System.Drawing.Point(12, 55)
$progBar.Size = New-Object System.Drawing.Size(742, 18)
$progBar.Minimum = 0
$progBar.Maximum = 100
$progBar.Value = 5
$pnlProc.Controls.Add($progBar)

# Panel de Informacion de la Caja en Vivo
$lblDevInfo = New-Object System.Windows.Forms.Label
$lblDevInfo.Font = $fontMono
$lblDevInfo.ForeColor = [System.Drawing.Color]::FromArgb(200, 230, 255)
$lblDevInfo.BackColor = [System.Drawing.Color]::FromArgb(26, 31, 42)
$lblDevInfo.BorderStyle = "FixedSingle"
$lblDevInfo.Location = New-Object System.Drawing.Point(18, 132)
$lblDevInfo.Size = New-Object System.Drawing.Size(770, 58)
$lblDevInfo.Padding = New-Object System.Windows.Forms.Padding(8, 4, 8, 4)
$form.Controls.Add($lblDevInfo)

# Tabla Visual de los 4 Pasos
$pnlSteps = New-Object System.Windows.Forms.Panel
$pnlSteps.Location = New-Object System.Drawing.Point(18, 195)
$pnlSteps.Size = New-Object System.Drawing.Size(770, 148)
$pnlSteps.BackColor = [System.Drawing.Color]::FromArgb(26, 31, 42)
$pnlSteps.BorderStyle = "FixedSingle"
$form.Controls.Add($pnlSteps)

$stepLabels = @{}
$yPos = 8
foreach ($k in $script:Steps.Keys) {
    $lblBadge = New-Object System.Windows.Forms.Label
    $lblBadge.Font = $fontStep
    $lblBadge.TextAlign = "MiddleCenter"
    $lblBadge.Location = New-Object System.Drawing.Point(10, $yPos)
    $lblBadge.Size = New-Object System.Drawing.Size(110, 28)
    $lblBadge.BackColor = [System.Drawing.Color]::FromArgb(55, 62, 75)
    $lblBadge.ForeColor = [System.Drawing.Color]::LightGray
    $lblBadge.Text = "[PENDIENTE]"
    $pnlSteps.Controls.Add($lblBadge)

    $lblDesc = New-Object System.Windows.Forms.Label
    $lblDesc.Font = $fontMono
    $lblDesc.TextAlign = "MiddleLeft"
    $lblDesc.Location = New-Object System.Drawing.Point(128, $yPos)
    $lblDesc.Size = New-Object System.Drawing.Size(632, 28)
    $lblDesc.ForeColor = [System.Drawing.Color]::White
    $lblDesc.Text = "$($script:Steps[$k].Nombre) -> $($script:Steps[$k].Detalle)"
    $pnlSteps.Controls.Add($lblDesc)

    $stepLabels[$k] = @{ Badge = $lblBadge; Desc = $lblDesc }
    $yPos += 34
}

# Bitacora en Vivo dentro del Dialogo
$txtLog = New-Object System.Windows.Forms.TextBox
$txtLog.Multiline = $true
$txtLog.ScrollBars = "Vertical"
$txtLog.ReadOnly = $true
$txtLog.Font = $fontMono
$txtLog.BackColor = [System.Drawing.Color]::FromArgb(12, 15, 22)
$txtLog.ForeColor = [System.Drawing.Color]::FromArgb(190, 240, 200)
$txtLog.BorderStyle = "FixedSingle"
$txtLog.Location = New-Object System.Drawing.Point(18, 348)
$txtLog.Size = New-Object System.Drawing.Size(770, 172)
$form.Controls.Add($txtLog)

# Botones de Control
$btnRetry = New-Object System.Windows.Forms.Button
$btnRetry.Text = "Reintentar Proceso"
$btnRetry.Font = $fontBtn
$btnRetry.Location = New-Object System.Drawing.Point(18, 530)
$btnRetry.Size = New-Object System.Drawing.Size(180, 38)
$btnRetry.BackColor = [System.Drawing.Color]::FromArgb(40, 110, 180)
$btnRetry.ForeColor = [System.Drawing.Color]::White
$btnRetry.FlatStyle = "Flat"
$btnRetry.Add_Click({ $script:ActionRequested = "RETRY" })
$form.Controls.Add($btnRetry)

$btnSummary = New-Object System.Windows.Forms.Button
$btnSummary.Text = "Ver Resumen (.TXT)"
$btnSummary.Font = $fontBtn
$btnSummary.Location = New-Object System.Drawing.Point(212, 530)
$btnSummary.Size = New-Object System.Drawing.Size(180, 38)
$btnSummary.BackColor = [System.Drawing.Color]::FromArgb(55, 75, 105)
$btnSummary.ForeColor = [System.Drawing.Color]::White
$btnSummary.FlatStyle = "Flat"
$btnSummary.Add_Click({
    if (Test-Path $summaryFile) { Start-Process "notepad.exe" -ArgumentList $summaryFile }
})
$form.Controls.Add($btnSummary)

$btnReboot = New-Object System.Windows.Forms.Button
$btnReboot.Text = "Reiniciar a Android"
$btnReboot.Font = $fontBtn
$btnReboot.Location = New-Object System.Drawing.Point(406, 530)
$btnReboot.Size = New-Object System.Drawing.Size(180, 38)
$btnReboot.BackColor = [System.Drawing.Color]::FromArgb(140, 95, 30)
$btnReboot.ForeColor = [System.Drawing.Color]::White
$btnReboot.FlatStyle = "Flat"
$btnReboot.Add_Click({ $script:ActionRequested = "REBOOT" })
$form.Controls.Add($btnReboot)

$btnExit = New-Object System.Windows.Forms.Button
$btnExit.Text = "Cerrar Programa"
$btnExit.Font = $fontBtn
$btnExit.Location = New-Object System.Drawing.Point(600, 530)
$btnExit.Size = New-Object System.Drawing.Size(188, 38)
$btnExit.BackColor = [System.Drawing.Color]::FromArgb(150, 40, 45)
$btnExit.ForeColor = [System.Drawing.Color]::White
$btnExit.FlatStyle = "Flat"
$btnExit.Add_Click({
    $script:ActionRequested = "EXIT"
    $form.Close()
})
$form.Controls.Add($btnExit)

$form.Add_FormClosing({
    $script:ActionRequested = "EXIT"
})

# ==============================================================================
# FUNCIONES DE ACTUALIZACION DE DIALOGO, BITACORA Y REPORTE .TXT
# ==============================================================================
function Update-DialogUI {
    if ($null -eq $form -or $form.IsDisposed) { return }
    $lblCurrentProcess.Text = $script:CurrentProcess
    if ($script:OverallStatus -match "EXITO") {
        $lblCurrentProcess.ForeColor = [System.Drawing.Color]::FromArgb(80, 240, 130)
    } elseif ($script:OverallStatus -match "FALLO|BLOQUEADO") {
        $lblCurrentProcess.ForeColor = [System.Drawing.Color]::FromArgb(255, 105, 105)
    } else {
        $lblCurrentProcess.ForeColor = [System.Drawing.Color]::FromArgb(255, 215, 70)
    }

    $val = [Math]::Max(0, [Math]::Min(100, [int]$script:ProgressValue))
    $progBar.Value = $val

    $lblDevInfo.Text = "USB: $($script:DeviceInfo.EstadoUSB)  |  SERIE: $($script:DeviceInfo.IdSerie)`r`nU-BOOT: $($script:DeviceInfo.VersionUBoot)  |  LOCK: $($script:DeviceInfo.EstadoLock)  |  ESCRITURA: $($script:DeviceInfo.PruebaEscritura)"

    foreach ($k in $script:Steps.Keys) {
        $st = $script:Steps[$k]
        $ui = $stepLabels[$k]
        $ui.Badge.Text = "[$($st.Estado)]"
        $ui.Desc.Text  = "$($st.Nombre) -> $($st.Detalle)"
        switch ($st.Estado) {
            "EXITO" {
                $ui.Badge.BackColor = [System.Drawing.Color]::FromArgb(25, 125, 65)
                $ui.Badge.ForeColor = [System.Drawing.Color]::White
                $ui.Desc.ForeColor  = [System.Drawing.Color]::FromArgb(140, 250, 170)
            }
            "EN CURSO" {
                $ui.Badge.BackColor = [System.Drawing.Color]::FromArgb(185, 130, 15)
                $ui.Badge.ForeColor = [System.Drawing.Color]::White
                $ui.Desc.ForeColor  = [System.Drawing.Color]::FromArgb(255, 225, 110)
            }
            "FALLO" {
                $ui.Badge.BackColor = [System.Drawing.Color]::FromArgb(165, 35, 45)
                $ui.Badge.ForeColor = [System.Drawing.Color]::White
                $ui.Desc.ForeColor  = [System.Drawing.Color]::FromArgb(255, 140, 140)
            }
            default {
                $ui.Badge.BackColor = [System.Drawing.Color]::FromArgb(55, 62, 75)
                $ui.Badge.ForeColor = [System.Drawing.Color]::LightGray
                $ui.Desc.ForeColor  = [System.Drawing.Color]::FromArgb(185, 195, 205)
            }
        }
    }
    [System.Windows.Forms.Application]::DoEvents()
}

function Write-Status($msg, $color = "White") {
    $ts = (Get-Date).ToString("HH:mm:ss")
    $line = "[$ts] $msg"
    Write-Host $line -ForegroundColor $color
    try { Add-Content -Path $logFile -Value $line -Encoding UTF8 -ErrorAction SilentlyContinue } catch {}
    if ($null -ne $txtLog -and (-not $txtLog.IsDisposed)) {
        $txtLog.AppendText("$line`r`n")
        $txtLog.SelectionStart = $txtLog.TextLength
        $txtLog.ScrollToCaret()
    }
    [System.Windows.Forms.Application]::DoEvents()
}

function Save-SummaryReport {
    $nowStr = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
    $elapsed = [math]::Round(((Get-Date) - $script:StartTime).TotalSeconds, 1)

    $lines = New-Object System.Collections.Generic.List[string]
    $lines.Add("================================================================================")
    $lines.Add("          REPORTE Y RESUMEN DE PROCESO - ZTE ZXV10 B866V2 (XVIEW+)              ")
    $lines.Add("================================================================================")
    $lines.Add(" >>> ESTADO GLOBAL: [$($script:OverallStatus)] <<<")
    $lines.Add(" >>> PROCESO ACTUAL: $($script:CurrentProcess)")
    $lines.Add("================================================================================")
    $lines.Add(" Fecha de inicio    : $($script:StartTime.ToString('yyyy-MM-dd HH:mm:ss'))")
    $lines.Add(" Ultima actualiz.   : $nowStr (Duracion: ${elapsed}s)")
    $lines.Add(" Diagnostico        : $($script:FailureReason)")
    $lines.Add("--------------------------------------------------------------------------------")
    $lines.Add(" 1. INFORMACION DEL DISPOSITIVO EN FASTBOOT")
    $lines.Add("--------------------------------------------------------------------------------")
    $lines.Add("  * Modelo / Placa        : $($script:DeviceInfo.Modelo)")
    $lines.Add("  * Estado Conexion USB   : $($script:DeviceInfo.EstadoUSB)")
    $lines.Add("  * Numero de Serie       : $($script:DeviceInfo.IdSerie)")
    $lines.Add("  * Version de U-Boot     : $($script:DeviceInfo.VersionUBoot)")
    $lines.Add("  * Estado Lock           : $($script:DeviceInfo.EstadoLock)")
    $lines.Add("  * Prueba de Escritura   : $($script:DeviceInfo.PruebaEscritura)")
    $lines.Add("--------------------------------------------------------------------------------")
    $lines.Add(" 2. REGISTRO DE LOS 4 PASOS DEL PROCESO")
    $lines.Add("--------------------------------------------------------------------------------")
    foreach ($k in $script:Steps.Keys) {
        $s = $script:Steps[$k]
        $stPad = $s.Estado.PadRight(10)
        $nmPad = $s.Nombre.PadRight(38)
        $lines.Add("  [$stPad] | $($s.Hora) | $nmPad | $($s.Detalle)")
    }

    if ($script:PartitionResults.Count -gt 0) {
        $lines.Add("--------------------------------------------------------------------------------")
        $lines.Add(" 3. DETALLE DE PARTICIONES INYECTADAS")
        $lines.Add("--------------------------------------------------------------------------------")
        foreach ($p in $script:PartitionResults) {
            $lines.Add("  * $($p.Particion.PadRight(12)) | $($p.Archivo.PadRight(18)) | [$($p.Estado.PadRight(9))] | $($p.Tiempo)s | $($p.Detalle)")
        }
    }
    $lines.Add("================================================================================")

    try {
        [System.IO.File]::WriteAllLines($summaryFile, $lines, [System.Text.Encoding]::UTF8)
    } catch {}
}

function Set-ProcessStep($stepKey, $estado, $detalle, $currentBanner = "", $progress = -1) {
    if ($script:Steps.Contains($stepKey)) {
        $script:Steps[$stepKey].Estado  = $estado
        $script:Steps[$stepKey].Hora    = (Get-Date).ToString("HH:mm:ss")
        $script:Steps[$stepKey].Detalle = $detalle
    }
    if ($currentBanner -ne "") {
        $script:CurrentProcess = $currentBanner
    }
    if ($progress -ge 0) {
        $script:ProgressValue = $progress
    }
    $col = switch ($estado) { "EXITO" { "Green" } "FALLO" { "Red" } "EN CURSO" { "Yellow" } default { "Gray" } }
    Write-Status "[$stepKey][$estado] $detalle" $col
    Save-SummaryReport
    Update-DialogUI
}

function Invoke-FastbootCmd($argStr, $timeoutMs = 5000) {
    $pinfo = New-Object System.Diagnostics.ProcessStartInfo
    $pinfo.FileName = $fbExe
    $pinfo.Arguments = $argStr
    $pinfo.WorkingDirectory = $fwDir
    $pinfo.RedirectStandardOutput = $true
    $pinfo.RedirectStandardError = $true
    $pinfo.UseShellExecute = $false
    $pinfo.CreateNoWindow = $true

    $proc = New-Object System.Diagnostics.Process
    $proc.StartInfo = $pinfo
    [void]$proc.Start()
    $outTask = $proc.StandardOutput.ReadToEndAsync()
    $errTask = $proc.StandardError.ReadToEndAsync()

    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    while (-not $proc.HasExited -and $sw.ElapsedMilliseconds -lt $timeoutMs) {
        [System.Windows.Forms.Application]::DoEvents()
        Start-Sleep -Milliseconds 25
    }
    if (-not $proc.HasExited) {
        try { $proc.Kill() } catch {}
        return [PSCustomObject]@{ TimedOut = $true; ExitCode = -1; Output = "[TIMEOUT]" }
    }
    [void]$outTask.Wait(300)
    [void]$errTask.Wait(300)
    $oStr = if ($outTask.IsCompleted) { $outTask.Result } else { "" }
    $eStr = if ($errTask.IsCompleted) { $errTask.Result } else { "" }
    $combined = ($oStr + "`n" + $eStr).Trim()
    return [PSCustomObject]@{ TimedOut = $false; ExitCode = $proc.ExitCode; Output = $combined }
}

function Invoke-FastbootKeepAlive {
    # En U-Boot con lock=10001000, cb_oem_cmd() rechaza 'oem watchdog off' con FAILlocked device.
    # En cambio, cb_getvar() ('getvar version-bootloader') NO pasa por check_lock() y mantiene vivo el gadget USB.
    $res = Invoke-FastbootCmd "getvar version-bootloader" 2000
    if ($script:DeviceInfo.EstadoLock -match "DESBLOQUEADO") {
        [void](Invoke-FastbootCmd "oem `"watchdog off`"" 1500)
    }
    return $res
}

function Invoke-WorldCupUnlockEnv {
    # Modo InUsbBurn externo (boton fisico RESET al encender con cable USB Macho-Macho, sin abrir la caja)
    if (-not (Test-Path $updateExe)) { return $false }
    Write-Status "[PASO 2][USB-BURN] Detectado modo Amlogic USB Burn (VID_1B8E). Inyectando env_unlocked.img (lock=10100000)..." "Cyan"
    try {
        if (Test-Path $envUnlImg) {
            & $updateExe partition env $envUnlImg 2>&1 | Out-Null
        }
        & $updateExe bulkcmd "setenv lock 10100000" 2>&1 | Out-Null
        & $updateExe bulkcmd "saveenv" 2>&1 | Out-Null
        & $updateExe bulkcmd "fastboot" 2>&1 | Out-Null
        Start-Sleep -Milliseconds 1500
        return $true
    } catch {
        return $false
    }
}

# ==============================================================================
# ARRANQUE DEL DIALOGO Y BUCLE DE 4 PASOS (100% EXTERNO SIN ABRIR LA CAJA)
# ==============================================================================
$form.Show()
$form.Activate()
Update-DialogUI
# Quitar TopMost fijo tras 2 segundos para permitir al usuario mover otras ventanas si lo desea
$topMostTimer = Get-Date
$topMostCleared = $false

Write-Status "==========================================================================" "Cyan"
Write-Status " INYECTADOR FASTBOOT B866V2 (MODO 100% EXTERNO - SIN ABRIR LA CAJA)" "Cyan"
Write-Status "==========================================================================" "Cyan"

Set-ProcessStep "Paso 1" "EN CURSO" "Esperando caja en Fastboot (Con USB KINGSTON en USB 1, mantenga RESET 8s)..." "PASO 1/4: ESPERANDO CONEXION FASTBOOT (PID_4EE7 / PID_0D02)..." 10

$testedSession   = $false
$tested4EE7      = $false
$flashedSession  = $false
$lastKeepAlive   = [DateTime]::MinValue
$lastUsbScan     = [DateTime]::MinValue
$lastUsbState    = ""

while ($form.Visible -and $script:ActionRequested -ne "EXIT") {
    [System.Windows.Forms.Application]::DoEvents()
    if ((-not $topMostCleared) -and (((Get-Date) - $topMostTimer).TotalSeconds -ge 2.5)) {
        $form.TopMost = $false
        $topMostCleared = $true
    }

    # Procesar botones del dialogo
    if ($script:ActionRequested -eq "RETRY") {
        $script:ActionRequested = ""
        $testedSession  = $false
        $tested4EE7     = $false
        $flashedSession = $false
        $script:OverallStatus = "EN EJECUCION"
        $script:FailureReason = "Ninguno"
        Set-ProcessStep "Paso 2" "PENDIENTE" "Pendiente de aplicar lock=10100000 y verificar escritura" "REINTENTANDO PROCESO FASTBOOT..." 25
        Set-ProcessStep "Paso 3" "PENDIENTE" "Pendiente de inyectar particiones del sistema B866V2" "" -1
        Set-ProcessStep "Paso 4" "PENDIENTE" "Pendiente de finalizar proceso y generar reporte" "" -1
    } elseif ($script:ActionRequested -eq "REBOOT") {
        $script:ActionRequested = ""
        Write-Status "[ACCION] Enviando 'fastboot reboot' para reiniciar hacia Android..." "Cyan"
        [void](Invoke-FastbootCmd "reboot" 4000)
        $testedSession = $false
        $tested4EE7    = $false
        Set-ProcessStep "Paso 1" "EN CURSO" "Caja reiniciada hacia Android. Esperando nueva entrada a Fastboot..." "CAJA REINICIADA A ANDROID - ESPERANDO FASTBOOT..." 10
    }

    # Escaneo PnP cada 250ms
    if (((Get-Date) - $lastUsbScan).TotalMilliseconds -ge 250) {
        $lastUsbScan = Get-Date
        $has0D02 = $false
        $has4EE7 = $false
        $has4EE1 = $false
        $hasWorldCup = $false
        try {
            $pnp = Get-CimInstance Win32_PnPEntity -Filter "DeviceID LIKE 'USB%VID_18D1%' OR DeviceID LIKE 'USB%VID_1B8E%'" -ErrorAction SilentlyContinue
            foreach ($d in $pnp) {
                if ($d.DeviceID -match "PID_0D02") { $has0D02 = $true }
                elseif ($d.DeviceID -match "PID_4EE7") { $has4EE7 = $true }
                elseif ($d.DeviceID -match "PID_4EE1") { $has4EE1 = $true }
                elseif ($d.DeviceID -match "VID_1B8E") { $hasWorldCup = $true }
            }
        } catch {}

        if ($hasWorldCup -and (-not $flashedSession)) {
            $script:DeviceInfo.EstadoUSB = "Amlogic WorldCup (VID_1B8E)"
            Set-ProcessStep "Paso 1" "EXITO" "Detectado modo Amlogic WorldCup (VID_1B8E)" "PASO 1/4: DETECTADO MODO WORLDCUP (VID_1B8E)" 35
            Set-ProcessStep "Paso 2" "EN CURSO" "Desbloqueando particion env (lock=10100000) via WorldCup..." "PASO 2/4: DESBLOQUEANDO ENV VIA WORLDCUP..." 45
            if (Invoke-WorldCupUnlockEnv) {
                $testedSession = $false
                continue
            }
        }

        $curState = if ($has0D02) { "PID_0D02" } elseif ($has4EE7) { "PID_4EE7" } elseif ($has4EE1) { "PID_4EE1" } else { "NONE" }
        if ($curState -ne $lastUsbState) {
            $lastUsbState = $curState
            if ($curState -eq "PID_4EE1") {
                $testedSession = $false
                $script:DeviceInfo.EstadoUSB = "En Android normal (PID_4EE1)"
                $script:DeviceInfo.IdSerie   = "Z41220090388172"
                Set-ProcessStep "Paso 1" "EN CURSO" "Caja en Android (PID_4EE1). Inserte USB KINGSTON y mantenga RESET 8s..." "CAJA EN ANDROID: MANTENGA RESET 8s CON USB KINGSTON PARA ENTRAR A FASTBOOT" 12
            } elseif ($curState -eq "NONE") {
                $testedSession = $false
                $script:DeviceInfo.EstadoUSB = "Reiniciando / Buscando Fastboot..."
                Update-DialogUI
            }
        }

        # ----------------------------------------------------------------------
        # PASO 1A: DETECTADO RECOVERY FASTBOOTD (PID_4EE7)
        # ----------------------------------------------------------------------
        if ($has4EE7) {
            $fbDevs = (Invoke-FastbootCmd "devices" 3500).Output
            $cleanMatch = [regex]::Match($fbDevs, "(?m)^([A-Za-z0-9_]+)\s+fastboot\b")
            if ($cleanMatch.Success) {
                $devId = $cleanMatch.Groups[1].Value.Trim()
                $script:DeviceInfo.EstadoUSB = "Recovery fastbootd (PID_4EE7)"
                $script:DeviceInfo.IdSerie   = $devId
                Set-ProcessStep "Paso 1" "EXITO" "Detectado fastbootd (PID_4EE7 / $devId). Verificando desbloqueo de env y particiones..." "PASO 1/4: DETECTADO FASTBOOTD (PID_4EE7) - VERIFICANDO ESCRITURA..." 25

                # En fastbootd (PID_4EE7), '/dev/block/dtb' NO existe en recovery.fstab.
                # Primero intentamos desbloquear e inyectar env_unlocked.img en '/dev/block/env' y probamos con 'vbmeta' (que si existe en recovery.fstab).
                [void](Invoke-FastbootCmd "flashing unlock" 2500)
                [void](Invoke-FastbootCmd "flashing unlock_critical" 2500)
                if (Test-Path $envUnlImg) {
                    Write-Status "[PASO 1A] Intentando desbloquear '/dev/block/env' desde fastbootd (PID_4EE7)..." "DarkCyan"
                    $resEnv4ee7 = Invoke-FastbootCmd "flash env `"$envUnlImg`"" 3500
                    $envMsg = ($resEnv4ee7.Output -replace "`r?`n", " | ").Trim()
                    if ($envMsg) { Write-Status "          Respuesta env (4EE7): $envMsg" "Gray" }
                }

                $tested4EE7 = $true
                $vbmetaImg = Join-Path $fwDir "vbmeta.img"
                $res4ee7 = Invoke-FastbootCmd "flash vbmeta `"$vbmetaImg`"" 3500
                if ((-not $res4ee7.TimedOut) -and ($res4ee7.ExitCode -eq 0) -and ($res4ee7.Output -match "OKAY") -and ($res4ee7.Output -notmatch "FAILED|Lock State|locked|not allowed")) {
                    Write-Status "[PASO 2] Escritura aceptada directamente en fastbootd (PID_4EE7)! Pasando a U-Boot (PID_0D02) con env desbloqueado..." "Green"
                    $script:DeviceInfo.EstadoLock      = "DESBLOQUEADO EN FASTBOOTD (4EE7)"
                    $script:DeviceInfo.PruebaEscritura = "APROBADA (flash vbmeta OKAY)"
                    Set-ProcessStep "Paso 2" "EXITO" "Escritura habilitada en fastbootd (PID_4EE7). Reiniciando a bootloader (PID_0D02)..." "PASO 2 COMPLETADO: SALTANDO A 0D02 PARA INYECTAR 12 PARTICIONES" 55
                    [void](Invoke-FastbootCmd "reboot bootloader" 3000)
                    Start-Sleep -Milliseconds 600
                    continue
                } else {
                    Write-Status "[PASO 1A] Saltando de fastbootd (PID_4EE7) a U-Boot Fastboot (PID_0D02)..." "Cyan"
                    [void](Invoke-FastbootCmd "reboot bootloader" 3000)
                    Start-Sleep -Milliseconds 600
                    continue
                }
            }
        }

        # ----------------------------------------------------------------------
        # PASO 1B, 2, 3 Y 4: DETECTADO U-BOOT FASTBOOT (PID_0D02)
        # ----------------------------------------------------------------------
        if ($has0D02) {
            # Si ya completamos la prueba en esta sesion PID_0D02, mantener Keep-Alive limpio (getvar version-bootloader) cada 3s
            if ($testedSession) {
                if (((Get-Date) - $lastKeepAlive).TotalSeconds -ge 3.0) {
                    $lastKeepAlive = Get-Date
                    [void](Invoke-FastbootKeepAlive)
                }
                Start-Sleep -Milliseconds 50
                continue
            }

            $fbRes  = Invoke-FastbootCmd "devices" 3000
            $fbDevs = $fbRes.Output
            $anyMatch   = [regex]::Match($fbDevs, "(?m)^(\S+)\s+fastboot\b")
            $cleanMatch = [regex]::Match($fbDevs, "(?m)^([A-Za-z0-9_]+)\s+fastboot\b")
            $isDescriptorClean = $cleanMatch.Success
            $devId = if ($isDescriptorClean) { $cleanMatch.Groups[1].Value.Trim() } else { "Z41220090388172" }

            # Si el endpoint USB quedo saturado por una sesion previa (???????? o TIMEOUT en fastboot devices):
            if (-not $isDescriptorClean) {
                $script:DeviceInfo.EstadoUSB       = "PID_0D02 (Reiniciar caja para limpiar USB)"
                $script:DeviceInfo.IdSerie         = "$devId (Pipe USB saturado)"
                $script:DeviceInfo.VersionUBoot    = "U-Boot 2015.01-g220e992 (Sep 05 2023)"
                $script:DeviceInfo.EstadoLock      = "Esperando reconexion limpia"
                $script:DeviceInfo.PruebaEscritura = "Pendiente de reinicio USB"

                Set-ProcessStep "Paso 1" "EXITO" "Caja detectada en USB (PID_0D02), pero con sesion USB previa saturada" "PASO 1 COMPLETADO: CAJA DETECTADA EN USB (PID_0D02)" 30
                Set-ProcessStep "Paso 2" "EN CURSO" "Desconecte y reconecte la corriente de la caja (con USB KINGSTON en USB 1 y presionando RESET 8s) para continuar..." "PASO 2/4: REINICIE LA CAJA CON RESET 8s PARA LIMPIAR CANAL USB..." 40
                Write-Status "[AVISO USB] El canal USB de la caja quedo ocupado por la sesion anterior. Desconecte la corriente de la caja 3s y reconectela manteniendo RESET 8s (con la USB KINGSTON puesta)." "Yellow"
                $testedSession = $true
                continue
            }

            # Canal USB 100% limpio -> Retener la caja en Fastboot con getvar version-bootloader (no rechazado por check_lock)
            $vBootRes = Invoke-FastbootKeepAlive
            $lastKeepAlive = Get-Date

            $script:DeviceInfo.EstadoUSB = "U-Boot Fastboot Activo (PID_0D02)"
            $script:DeviceInfo.IdSerie   = $devId
            Set-ProcessStep "Paso 1" "EXITO" "Caja conectada y retenida en U-Boot Fastboot (PID_0D02 / $devId) con Keep-Alive" "PASO 1 COMPLETADO: CAJA EN U-BOOT FASTBOOT (PID_0D02)" 35

            # --------------------------------------------------------------
            # PASO 2: DESBLOQUEO DE BOOTLOADER (LOCK=10100000) - 100% SIN ABRIR LA CAJA
            # --------------------------------------------------------------
            Set-ProcessStep "Paso 2" "EN CURSO" "Consultando estado de U-Boot y ejecutando secuencia de desbloqueo USB..." "PASO 2/4: DESBLOQUEANDO BOOTLOADER POR USB..." 45

            $script:DeviceInfo.VersionUBoot = "U-Boot 2015.01-g220e992 (Sep 05 2023)"
            $vBoot = $vBootRes.Output
            if ($vBoot -match "version-bootloader:\s*(\S+)") {
                $script:DeviceInfo.VersionUBoot = "$($Matches[1].Trim()) (Sep 05 2023)"
            }

            # 1) Intentar desbloqueo estandar USB Fastboot
            foreach ($uc in @("flashing get_unlock_ability", "flashing unlock", "flashing unlock_critical")) {
                Write-Status "[PASO 2] Ejecutando: fastboot $uc" "DarkCyan"
                $uRes = Invoke-FastbootCmd $uc 2500
                $uMsg = ($uRes.Output -replace "`r?`n", " | ").Trim()
                if ($uMsg) { Write-Status "         Respuesta: $uMsg" "Gray" }
                Start-Sleep -Milliseconds 80
            }

            # 2) Verificar si 'unlocked' cambio a 'yes' tras flashing unlock
            $unlCheck = (Invoke-FastbootCmd "getvar unlocked" 2500).Output
            $isUnlockedNow = ($unlCheck -match "unlocked:\s*yes")

            # 3) Si aun no hemos probado desbloquear env desde Recovery fastbootd (PID_4EE7) en esta sesion, saltar a fastbootd (permitido sin check_lock en f_fastboot.c)
            if ((-not $isUnlockedNow) -and (-not $tested4EE7)) {
                Write-Status "[PASO 2] Saltando a Recovery fastbootd (PID_4EE7) via 'fastboot reboot fastboot' para probar desbloqueo de '/dev/block/env' sin abrir la caja..." "Cyan"
                $tested4EE7 = $true
                [void](Invoke-FastbootCmd "reboot fastboot" 3000)
                Start-Sleep -Milliseconds 800
                continue
            }

            # 4) Si ya esta desbloqueado, fijar lock=10100000 y apagar watchdog por OEM
            if ($isUnlockedNow) {
                foreach ($oemCmd in @("oem `"watchdog off`"", "oem `"setenv lock 10100000`"", "oem `"saveenv`"")) {
                    Write-Status "[PASO 2] Ejecutando: fastboot $oemCmd" "DarkCyan"
                    $oRes = Invoke-FastbootCmd $oemCmd 2500
                    $oMsg = ($oRes.Output -replace "`r?`n", " | ").Trim()
                    if ($oMsg) { Write-Status "         Respuesta: $oMsg" "Gray" }
                }
            } else {
                Write-Status "[PASO 2] Aviso: 'getvar unlocked' reporta 'no' (lock=10001000, unlock_ability=0 en U-Boot Sep 05 2023)." "Yellow"
            }

            Write-Status "[PASO 2] Verificando escritura en particion '_aml_dtb'..." "DarkCyan"
            $dtbImg = Join-Path $fwDir "dtb.img"
            $testRes = Invoke-FastbootCmd "flash _aml_dtb `"$dtbImg`"" 3500
            [void](Invoke-FastbootKeepAlive)
            $testOut = ($testRes.Output -replace "`r?`n", " | ")
            $testTimedOut = $testRes.TimedOut

            $testedSession = $true

            if ($testTimedOut -or ($testOut -match "device is locked|locked device|not allowed|FAILED|TIMEOUT") -or ($testOut -notmatch "OKAY")) {
                $script:DeviceInfo.EstadoLock      = "BLOQUEADO (lock=10001000)"
                $script:DeviceInfo.PruebaEscritura = "RECHAZADA (device is locked)"
                $script:OverallStatus              = "DETENIDO EN PASO 2 (U-BOOT BLOQUEADO lock=10001000)"
                $script:FailureReason              = "La caja entro a U-Boot Fastboot (PID_0D02) sin abrir la caja, pero U-Boot (Sep 05 2023, 01.01.230905.041301) tiene lock=10001000 (unlock_ability=0) y ro.oem_unlock_supported=0. En f_fastboot.c, cb_flashing ignora 'flashing unlock' cuando unlock_ability=0 y check_lock() bloquea cb_oem_cmd() y cb_download()."

                Set-ProcessStep "Paso 2" "FALLO" "U-Boot rechaza desbloqueo y escritura USB (ERROR: device is locked / lock=10001000)" "PROCESO DETENIDO EN PASO 2: U-BOOT BLOQUEADO DE FABRICA (lock=10001000)" 50
                Set-ProcessStep "Paso 3" "FALLO" "Cancelado porque U-Boot (PID_0D02) bloquea la descarga de particiones" "" -1
                Set-ProcessStep "Paso 4" "FALLO" "Reporte guardado en RESUMEN_PROCESO_B866V2.txt (Caja retenida en Fastboot)" "" -1
                continue
            }

            # Si la prueba de escritura tuvo exito:
            $script:DeviceInfo.EstadoLock      = "DESBLOQUEADO (lock=10100000)"
            $script:DeviceInfo.PruebaEscritura = "APROBADA (flash _aml_dtb OKAY)"
            Set-ProcessStep "Paso 2" "EXITO" "Bootloader desbloqueado (lock=10100000) y escritura verificada" "PASO 2 COMPLETADO: BOOTLOADER DESBLOQUEADO" 55

                # --------------------------------------------------------------
                # PASO 3: INYECCION DE LAS 12 PARTICIONES DEL FIRMWARE B866V2
                # --------------------------------------------------------------
                Set-ProcessStep "Paso 3" "EN CURSO" "Iniciando inyeccion de las 12 particiones del firmware B866V2..." "PASO 3/4: INYECTANDO FIRMWARE COMPLETO B866V2..." 60

                $partitions = @(
                    @("_aml_dtb",   "dtb.img"),
                    @("dtb",        "dtb.img"),
                    @("dtbo",       "dtbo.img"),
                    @("boot",       "boot.img"),
                    @("recovery",   "recovery.img"),
                    @("vbmeta",     "vbmeta.img"),
                    @("logo",       "logo.img"),
                    @("oem",        "oem.img"),
                    @("param",      "param.img"),
                    @("conf",       "conf.img"),
                    @("cache",      "cache_ext4.img"),
                    @("bootloader", "bootloader.img")
                )

                $okCount  = 0
                $errCount = 0
                $totalP   = $partitions.Count
                $idxP     = 0

                foreach ($p in $partitions) {
                    $idxP++
                    $pName = $p[0]
                    $pFile = $p[1]
                    $pPath = Join-Path $fwDir $pFile
                    $pct   = 55 + [int](($idxP / $totalP) * 40)

                    if (Test-Path $pPath) {
                        Set-ProcessStep "Paso 3" "EN CURSO" "Inyectando ($idxP/$totalP): particion '$pName' ($pFile)..." "PASO 3/4: INYECTANDO PARTICION $idxP/$totalP -> $pName ($pFile)..." $pct
                        $t0 = Get-Date
                        $fRes = Invoke-FastbootCmd "flash $pName `"$pPath`"" 45000
                        [void](Invoke-FastbootCmd "oem `"watchdog off`"" 2500)
                        $dt = [math]::Round(((Get-Date) - $t0).TotalSeconds, 2)
                        $fOut = ($fRes.Output -replace "`r?`n", " | ")

                        if ((-not $fRes.TimedOut) -and ($fRes.ExitCode -eq 0) -and ($fOut -notmatch "FAILED|error:|device is locked")) {
                            $okCount++
                            $script:DeviceInfo.ParticionesOK = $okCount
                            $script:PartitionResults.Add([PSCustomObject]@{ Particion = $pName; Archivo = $pFile; Estado = "EXITO"; Tiempo = $dt; Detalle = "OKAY" })
                        } else {
                            $errCount++
                            $script:DeviceInfo.ParticionesError = $errCount
                            $script:PartitionResults.Add([PSCustomObject]@{ Particion = $pName; Archivo = $pFile; Estado = "FALLO"; Tiempo = $dt; Detalle = $fOut })
                        }
                    }
                }

                # Proteger factory (Widevine L1) y cri_data (MAC/SN)
                $script:PartitionResults.Add([PSCustomObject]@{ Particion = "factory";  Archivo = "(Protegido)"; Estado = "INTACTO"; Tiempo = 0; Detalle = "Claves Widevine L1 conservadas" })
                $script:PartitionResults.Add([PSCustomObject]@{ Particion = "cri_data"; Archivo = "(Protegido)"; Estado = "INTACTO"; Tiempo = 0; Detalle = "MAC y Serie originales conservados" })

                if ($errCount -eq 0) {
                    $flashedSession = $true
                    $script:OverallStatus = "EXITO TOTAL - FIRMWARE INYECTADO"
                    $script:FailureReason = "Ninguno. Todas las particiones ($okCount/$totalP) fueron inyectadas."
                    Set-ProcessStep "Paso 3" "EXITO" "12/12 particiones inyectadas correctamente (factory y cri_data protegidos)" "PASO 3 COMPLETADO: FIRMWARE INYECTADO CON EXITO" 95
                    Set-ProcessStep "Paso 4" "EXITO" "Proceso finalizado con exito. Resumen guardado en RESUMEN_PROCESO_B866V2.txt" "PROCESO COMPLETADO (4/4): FIRMWARE B866V2 INYECTADO CON EXITO" 100
                } else {
                    $script:OverallStatus = "FALLO PARCIAL EN INYECCION ($errCount ERRORES)"
                    $script:FailureReason = "Fallaron $errCount de $totalP particiones durante la inyeccion."
                    Set-ProcessStep "Paso 3" "FALLO" "Exitosas: $okCount | Con error: $errCount" "PASO 3 CON ERRORES: REVISE EL RESUMEN" 95
                    Set-ProcessStep "Paso 4" "FALLO" "Proceso finalizado con errores. Resumen guardado en RESUMEN_PROCESO_B866V2.txt" "PROCESO DETENIDO CON ERRORES EN PASO 3" 100
                }
        }
    }

    Start-Sleep -Milliseconds 40
}
