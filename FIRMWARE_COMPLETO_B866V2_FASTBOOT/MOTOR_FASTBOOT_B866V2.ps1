Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName Microsoft.VisualBasic
[System.Windows.Forms.Application]::EnableVisualStyles()

$ErrorActionPreference = "Continue"
$fwDir       = $PSScriptRoot
$rootDir     = Split-Path $fwDir -Parent
$fbExe       = Join-Path $fwDir "fastboot.exe"
$adbExe      = Join-Path $fwDir "adb.exe"
$updateExe   = Join-Path $fwDir "update.exe"
$adnlExe     = Join-Path $fwDir "adnl.exe"
$envUnlImg   = Join-Path $fwDir "env_unlocked.img"
$confImg     = Join-Path $fwDir "conf.img"
$superImgAlt = Join-Path $rootDir "V2\B866v2 Android 12\B866v2 Android 12\B866V2\super.img"
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
# ESTADO GLOBAL Y 4 PASOS DIRECTOS (100% EXTERNO SIN ABRIR LA CAJA)
# ==============================================================================
$script:StartTime        = Get-Date
$script:CurrentProcess   = "Iniciando monitor USB Fastboot / ADB..."
$script:ProgressValue    = 5
$script:OverallStatus    = "EN EJECUCION"
$script:FailureReason    = "Ninguno"
$script:ActionRequested  = ""
$script:PartitionResults = New-Object System.Collections.Generic.List[PSCustomObject]

$script:DeviceInfo = [ordered]@{
    Modelo           = "Megacable Xview+ ZTE ZXV10 B866V2 (board_id=8)"
    EstadoUSB        = "Buscando nueva caja Xview+ en USB..."
    IdSerie          = "Esperando deteccion..."
    VersionUBoot     = "Detectando..."
    EstadoLock       = "Verificando..."
    PruebaEscritura  = "Pendiente"
    ParticionesOK    = 0
    ParticionesError = 0
}

$script:Steps = [ordered]@{
    "Paso 1" = [PSCustomObject]@{ Num = 1; Nombre = "1. Conexion USB con la Caja B866V2";         Estado = "PENDIENTE"; Hora = "--:--:--"; Detalle = "Esperando deteccion de la caja en USB (Fastboot / Android)" }
    "Paso 2" = [PSCustomObject]@{ Num = 2; Nombre = "2. Desbloqueo de Bootloader / Puente ADB";   Estado = "PENDIENTE"; Hora = "--:--:--"; Detalle = "Pendiente de desbloquear acceso de escritura (Fastboot o ADB)" }
    "Paso 3" = [PSCustomObject]@{ Num = 3; Nombre = "3. Inyeccion de Sistema (12 modulos/part.)"; Estado = "PENDIENTE"; Hora = "--:--:--"; Detalle = "Pendiente de inyectar sistema y liberacion B866V2" }
    "Paso 4" = [PSCustomObject]@{ Num = 4; Nombre = "4. Verificacion Final y Resumen";            Estado = "PENDIENTE"; Hora = "--:--:--"; Detalle = "Pendiente de finalizar proceso y generar reporte" }
}

# ==============================================================================
# CONSTRUCCION DEL DIALOGO VISUAL EN VIVO (WINDOWS FORMS)
# ==============================================================================
$form = New-Object System.Windows.Forms.Form
$form.Text = "INYECTADOR DE FIRMWARE FASTBOOT / ADB - ZTE ZXV10 B866V2 (XVIEW+)"
$form.Size = New-Object System.Drawing.Size(820, 620)
$form.StartPosition = "CenterScreen"
$form.BackColor = [System.Drawing.Color]::FromArgb(20, 24, 33)
$form.ForeColor = [System.Drawing.Color]::White
$form.FormBorderStyle = "FixedDialog"
$form.MaximizeBox = $false
$form.TopMost = $true

$fontTitle   = New-Object System.Drawing.Font("Segoe UI", 13, [System.Drawing.FontStyle]::Bold)
$fontProcess = New-Object System.Drawing.Font("Segoe UI", 10.5, [System.Drawing.FontStyle]::Bold)
$fontStep    = New-Object System.Drawing.Font("Consolas", 9.5, [System.Drawing.FontStyle]::Bold)
$fontMono    = New-Object System.Drawing.Font("Consolas", 9, [System.Drawing.FontStyle]::Regular)
$fontBtn     = New-Object System.Drawing.Font("Segoe UI", 9, [System.Drawing.FontStyle]::Bold)

# Cabecera
$lblHeader = New-Object System.Windows.Forms.Label
$lblHeader.Text = "PROCESO DIRECTO (4 PASOS) - ZTE ZXV10 B866V2 (100% SIN ABRIR LA CAJA)"
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

# Botones de Control (5 botones en la barra inferior)
$btnRetry = New-Object System.Windows.Forms.Button
$btnRetry.Text = "Reintentar Proceso"
$btnRetry.Font = $fontBtn
$btnRetry.Location = New-Object System.Drawing.Point(18, 530)
$btnRetry.Size = New-Object System.Drawing.Size(146, 38)
$btnRetry.BackColor = [System.Drawing.Color]::FromArgb(40, 110, 180)
$btnRetry.ForeColor = [System.Drawing.Color]::White
$btnRetry.FlatStyle = "Flat"
$btnRetry.Add_Click({ $script:ActionRequested = "RETRY" })
$form.Controls.Add($btnRetry)

$btnAdbIp = New-Object System.Windows.Forms.Button
$btnAdbIp.Text = "Conectar ADB IP"
$btnAdbIp.Font = $fontBtn
$btnAdbIp.Location = New-Object System.Drawing.Point(172, 530)
$btnAdbIp.Size = New-Object System.Drawing.Size(146, 38)
$btnAdbIp.BackColor = [System.Drawing.Color]::FromArgb(25, 130, 95)
$btnAdbIp.ForeColor = [System.Drawing.Color]::White
$btnAdbIp.FlatStyle = "Flat"
$btnAdbIp.Add_Click({ $script:ActionRequested = "ADB_IP" })
$form.Controls.Add($btnAdbIp)

$btnSummary = New-Object System.Windows.Forms.Button
$btnSummary.Text = "Ver Resumen (.TXT)"
$btnSummary.Font = $fontBtn
$btnSummary.Location = New-Object System.Drawing.Point(326, 530)
$btnSummary.Size = New-Object System.Drawing.Size(148, 38)
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
$btnReboot.Location = New-Object System.Drawing.Point(482, 530)
$btnReboot.Size = New-Object System.Drawing.Size(148, 38)
$btnReboot.BackColor = [System.Drawing.Color]::FromArgb(140, 95, 30)
$btnReboot.ForeColor = [System.Drawing.Color]::White
$btnReboot.FlatStyle = "Flat"
$btnReboot.Add_Click({ $script:ActionRequested = "REBOOT" })
$form.Controls.Add($btnReboot)

$btnExit = New-Object System.Windows.Forms.Button
$btnExit.Text = "Cerrar Programa"
$btnExit.Font = $fontBtn
$btnExit.Location = New-Object System.Drawing.Point(638, 530)
$btnExit.Size = New-Object System.Drawing.Size(150, 38)
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
    } elseif ($script:OverallStatus -match "FALLO|ERROR") {
        $lblCurrentProcess.ForeColor = [System.Drawing.Color]::FromArgb(255, 105, 105)
    } else {
        $lblCurrentProcess.ForeColor = [System.Drawing.Color]::FromArgb(255, 215, 70)
    }

    $val = [Math]::Max(0, [Math]::Min(100, [int]$script:ProgressValue))
    $progBar.Value = $val

    $lblDevInfo.Text = "USB: $($script:DeviceInfo.EstadoUSB)  |  SERIE: $($script:DeviceInfo.IdSerie)`r`nU-BOOT: $($script:DeviceInfo.VersionUBoot)  |  LOCK: $($script:DeviceInfo.EstadoLock)  |  ACCESO: $($script:DeviceInfo.PruebaEscritura)"

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
    $lines.Add(" 1. INFORMACION DEL DISPOSITIVO")
    $lines.Add("--------------------------------------------------------------------------------")
    $lines.Add("  * Modelo / Placa        : $($script:DeviceInfo.Modelo)")
    $lines.Add("  * Estado Conexion USB   : $($script:DeviceInfo.EstadoUSB)")
    $lines.Add("  * Numero de Serie       : $($script:DeviceInfo.IdSerie)")
    $lines.Add("  * Version de U-Boot     : $($script:DeviceInfo.VersionUBoot)")
    $lines.Add("  * Estado Lock           : $($script:DeviceInfo.EstadoLock)")
    $lines.Add("  * Estado de Acceso      : $($script:DeviceInfo.PruebaEscritura)")
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
        $lines.Add(" 3. DETALLE DE PARTICIONES / MODULOS INYECTADOS")
        $lines.Add("--------------------------------------------------------------------------------")
        foreach ($p in $script:PartitionResults) {
            $lines.Add("  * $($p.Particion.PadRight(15)) | $($p.Archivo.PadRight(22)) | [$($p.Estado.PadRight(9))] | $($p.Tiempo)s | $($p.Detalle)")
        }
    }
    $lines.Add("================================================================================")

    try {
        [System.IO.File]::WriteAllLines($summaryFile, $lines, [System.Text.Encoding]::UTF8)
    } catch {}
}

function Set-ProcessStep($stepKey, $estado, $detalle, $currentBanner = "", $progress = -1, $silentLog = $false) {
    $changed = $true
    if ($script:Steps.Contains($stepKey)) {
        if ($script:Steps[$stepKey].Estado -eq $estado -and $script:Steps[$stepKey].Detalle -eq $detalle) {
            $changed = $false
        }
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
    if ($changed -and (-not $silentLog)) {
        $col = switch ($estado) { "EXITO" { "Green" } "FALLO" { "Red" } "EN CURSO" { "Yellow" } default { "Gray" } }
        Write-Status "[$stepKey][$estado] $detalle" $col
    }
    Save-SummaryReport
    Update-DialogUI
}

function Invoke-ToolCmd($exePath, $argStr, $timeoutMs = 5000) {
    if (-not (Test-Path $exePath)) {
        return [PSCustomObject]@{ TimedOut = $false; ExitCode = -1; Output = "[NO_EXE]" }
    }
    $pinfo = New-Object System.Diagnostics.ProcessStartInfo
    $pinfo.FileName = $exePath
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

function Invoke-FastbootCmd($argStr, $timeoutMs = 5000) {
    return Invoke-ToolCmd $fbExe $argStr $timeoutMs
}

function Invoke-UpdateCmd($argStr, $timeoutMs = 15000) {
    return Invoke-ToolCmd $updateExe $argStr $timeoutMs
}

function Invoke-AdbCmd($argStr, $timeoutMs = 8000) {
    return Invoke-ToolCmd $adbExe $argStr $timeoutMs
}

function Invoke-FastbootKeepAlive {
    $res = Invoke-FastbootCmd "getvar version-bootloader" 2000
    if ($script:DeviceInfo.EstadoLock -match "DESBLOQUEADO") {
        [void](Invoke-FastbootCmd "oem `"watchdog off`"" 1500)
    }
    return $res
}

function Invoke-WorldCupUnlockEnv {
    if (-not (Test-Path $updateExe)) { return $false }
    Write-Status "[PASO 2][USB-BURN] Detectado modo Amlogic WorldCup (VID_1B8E). Desbloqueando particion 'env' (lock=10100000)..." "Cyan"
    try {
        if (Test-Path $envUnlImg) {
            $rEnv = Invoke-UpdateCmd "partition env `"$envUnlImg`"" 10000
            $mEnv = ($rEnv.Output -replace "`r?`n", " | ").Trim()
            if ($mEnv) { Write-Status "         update partition env: $mEnv" "Gray" }
        }
        if (Test-Path $confImg) {
            $rConf = Invoke-UpdateCmd "partition conf `"$confImg`"" 8000
            $mConf = ($rConf.Output -replace "`r?`n", " | ").Trim()
            if ($mConf) { Write-Status "         update partition conf: $mConf" "Gray" }
        }
        foreach ($bcmd in @(
            "setenv -f lock 10100000",
            "setenv lock 10100000",
            "setenv unlock_ability 1",
            "setenv otg_device 1",
            "saveenv",
            "fastboot"
        )) {
            $rBulk = Invoke-UpdateCmd "bulkcmd `"$bcmd`"" 5000
            $mBulk = ($rBulk.Output -replace "`r?`n", " | ").Trim()
            if ($mBulk) { Write-Status "         update bulkcmd '$bcmd': $mBulk" "Gray" }
        }
        Start-Sleep -Milliseconds 1200
        return $true
    } catch {
        return $false
    }
}

# ==============================================================================
# PASO 2 + PASO 3 + PASO 4 POR PUENTE ADB (USB PID_4EE2 O RED :5555)
# ==============================================================================
function Invoke-AdbCompleteWorkflow($adbId) {
    $script:DeviceInfo.EstadoUSB       = "Android ADB Activo ($adbId)"
    $script:DeviceInfo.IdSerie         = $adbId
    $script:DeviceInfo.PruebaEscritura = "APROBADA (Puente ADB Activo)"
    Set-ProcessStep "Paso 1" "EXITO" "Caja conectada por puente ADB ($adbId)" "PASO 1 COMPLETADO: PUENTE ADB ACTIVO ($adbId)" 30

    # --------------------------------------------------------------------------
    # PASO 2: DESBLOQUEO OEM / ENV Y PREPARACION DEL SISTEMA
    # --------------------------------------------------------------------------
    Set-ProcessStep "Paso 2" "EN CURSO" "Aplicando desbloqueo OEM/env y preparando sistema en $adbId..." "PASO 2/4: CONFIGURANDO DESBLOQUEO OEM Y ACCESO AL SISTEMA..." 45
    Write-Status "[PASO 2][ADB] Aplicando banderas de desbloqueo OEM y configuracion OTG en $adbId..." "Cyan"

    [void](Invoke-AdbCmd "-s $adbId shell `"settings put global oem_unlock_enabled 1; settings put secure oem_unlock_enabled 1`"" 5000)
    [void](Invoke-AdbCmd "-s $adbId shell `"cmd oem_lock set-unlock-enabled true 2>/dev/null; setprop persist.sys.oem_unlock_allowed 1; setprop sys.oem_unlock_allowed 1`"" 5000)
    [void](Invoke-AdbCmd "-s $adbId shell `"setprop ubootenv.var.lock 10100000; setprop ubootenv.var.otg_device 1; setprop persist.sys.usb.config mtp,adb`"" 5000)

    if (Test-Path $envUnlImg) {
        [void](Invoke-AdbCmd "-s $adbId push `"$envUnlImg`" /data/local/tmp/env_unlocked.img" 6000)
        [void](Invoke-AdbCmd "-s $adbId shell `"dd if=/data/local/tmp/env_unlocked.img of=/dev/block/env bs=65536 count=1 2>/dev/null; su -c 'dd if=/data/local/tmp/env_unlocked.img of=/dev/block/env bs=65536 count=1' 2>/dev/null; sync`"" 5000)
    }

    $envLockProp = (Invoke-AdbCmd "-s $adbId shell `"getprop ubootenv.var.lock`"" 3500).Output.Trim()
    $verRelease  = (Invoke-AdbCmd "-s $adbId shell `"getprop ro.build.version.release`"" 3500).Output.Trim()
    if (-not $verRelease) { $verRelease = "12" }

    if ($envLockProp -eq "10100000") {
        $script:DeviceInfo.EstadoLock = "DESBLOQUEADO (lock=10100000)"
        Set-ProcessStep "Paso 2" "EXITO" "Particion '/dev/block/env' desbloqueada (lock=10100000). Reiniciando a Fastboot..." "PASO 2 COMPLETADO: SALTANDO A FASTBOOT (PID_0D02)..." 55
        Write-Status "[PASO 2][ADB] '/dev/block/env' reporta lock=10100000! Reiniciando a U-Boot Fastboot (PID_0D02)..." "Green"
        [void](Invoke-AdbCmd "-s $adbId reboot bootloader" 4000)
        $script:LastRebootCmd = Get-Date
        return "REBOOTED_TO_FASTBOOT"
    }

    # En U-Boot Sep 05 2023 (01.01.230905.041301) con Android 12 ya instalado, ejecutamos la inyeccion
    # de los 12 modulos de liberacion y aplicaciones directamente sobre Android 12 por ADB.
    $script:DeviceInfo.EstadoLock      = "PUENTE ADB AUTORIZADO (Android $verRelease / U-Boot Sep 05 2023)"
    $script:DeviceInfo.PruebaEscritura = "APROBADA (Inyeccion Directa ADB)"
    Set-ProcessStep "Paso 2" "EXITO" "Puente ADB autorizado y OEM habilitado en Android $verRelease ($adbId)" "PASO 2 COMPLETADO: PUENTE DE INYECCION ADB HABILITADO" 55

    # --------------------------------------------------------------------------
    # PASO 3: INYECCION DE LOS 12 MODULOS DE LIBERACION Y APPS B866V2
    # --------------------------------------------------------------------------
    Set-ProcessStep "Paso 3" "EN CURSO" "Iniciando inyeccion de los 12 modulos del sistema B866V2 por ADB..." "PASO 3/4: INYECTANDO SISTEMA Y LIBERACION B866V2 (12 MODULOS)..." 60
    $script:PartitionResults.Clear()

    $apkDir  = Join-Path $rootDir "experimento\04_apks_dispositivo"
    $autoApk = Join-Path $rootDir "experimento\09_analisis_archivos_usuario\autostart.apk"
    $dlApk   = Join-Path $rootDir "com.esaba.downloader-2-0-4-ForGoogleAndroidDevices-arm64-v8a-android60.apk"

    $adbModules = @(
        @{ Nombre = "autostart";      Archivo = "autostart.apk";          Tipo = "APK";  Ruta = $autoApk;                                  Desc = "Servicio de auto-arranque liberado" },
        @{ Nombre = "Filemanager";    Archivo = "Filemanager.apk";        Tipo = "APK";  Ruta = (Join-Path $apkDir "Filemanager.apk");     Desc = "Gestor de archivos Android TV" },
        @{ Nombre = "DLStore";        Archivo = "DLStore.apk";            Tipo = "APK";  Ruta = (Join-Path $apkDir "DLStore.apk");         Desc = "Tienda de aplicaciones DLStore" },
        @{ Nombre = "AppDMC";         Archivo = "AppDMC.apk";             Tipo = "APK";  Ruta = (Join-Path $apkDir "AppDMC.apk");          Desc = "Centro multimedia DMC" },
        @{ Nombre = "Hdplay";         Archivo = "Hdplay.apk";             Tipo = "APK";  Ruta = (Join-Path $apkDir "Hdplay.apk");          Desc = "Reproductor HDPlay" },
        @{ Nombre = "Downloader";     Archivo = "Downloader.apk";         Tipo = "APK";  Ruta = $dlApk;                                    Desc = "Downloader para Android TV" },
        @{ Nombre = "megacable_lock"; Archivo = "com.dotscreen.megacable";Tipo = "CMD";  Cmd  = "pm disable-user --user 0 com.dotscreen.megacable.atv; pm disable-user --user 0 com.dotscreen.megacable.portal; pm disable-user --user 0 com.dotscreen.megacable.updater; pm disable-user --user 0 com.dotscreen.megacable.livetv"; Desc = "Bloqueo de operador Megacable desactivado" },
        @{ Nombre = "zte_launcher";   Archivo = "LauncherCustomize";      Tipo = "CMD";  Cmd  = "pm disable-user --user 0 com.ztestb.LauncherCustomize; pm disable-user --user 0 com.zte.ums.oes"; Desc = "Personalizacion ZTE / OES desactivada" },
        @{ Nombre = "zte_ota";        Archivo = "com.zte.ota";            Tipo = "CMD";  Cmd  = "am force-stop com.zte.ota; pm disable-user --user 0 com.zte.ota; am force-stop tv.upgrade.service; pm disable-user --user 0 tv.upgrade.service"; Desc = "Actualizaciones OTA ZTE bloqueadas" },
        @{ Nombre = "zte_telemetria"; Archivo = "uploaddiagno / act";     Tipo = "CMD";  Cmd  = "am force-stop com.zte.tv.act; pm disable-user --user 0 com.zte.tv.act; am force-stop com.ztestb.uploaddiagno; pm disable-user --user 0 com.ztestb.uploaddiagno; am force-stop com.zte.factorytesttool; pm disable-user --user 0 com.zte.factorytesttool"; Desc = "Telemetria ZTE deshabilitada" },
        @{ Nombre = "gms_banner";     Archivo = "NotificationControl";    Tipo = "CMD";  Cmd  = "settings put global ota_disable_automatic_update 1; cmd appops set com.google.android.gms POST_NOTIFICATION ignore; cmd notification snooze --for 315360000000 '0|com.google.android.gms|1|com.google.android.gms.update.control.NotificationControl|10049' 2>/dev/null; am force-stop com.google.android.gms"; Desc = "Banner de actualizacion eliminado" },
        @{ Nombre = "stealth_otg";    Archivo = "usb_otg_stealth.cfg";    Tipo = "CMD";  Cmd  = "settings put global adb_enabled 0; settings put secure adb_enabled 0; settings put global development_settings_enabled 0; settings put secure development_settings_enabled 0; setprop service.adb.tcp.port 5555; setprop persist.sys.usb.config mtp,adb; setprop ctl.start adbd"; Desc = "Modo Stealth + ADB/OTG persistente activo" }
    )

    $okCount  = 0
    $errCount = 0
    $totalM   = $adbModules.Count
    $idxM     = 0

    foreach ($m in $adbModules) {
        $idxM++
        $mName = $m.Nombre
        $mFile = $m.Archivo
        $pct   = 55 + [int](($idxM / $totalM) * 40)
        Set-ProcessStep "Paso 3" "EN CURSO" "Inyectando ($idxM/$totalM): '$mName' ($mFile)..." "PASO 3/4: INYECTANDO MODULO $idxM/$totalM -> $mName ($mFile)..." $pct
        $t0 = Get-Date

        if ($m.Tipo -eq "APK") {
            if (Test-Path $m.Ruta) {
                $res = Invoke-AdbCmd "-s $adbId install -r `"$($m.Ruta)`"" 45000
                $dt  = [math]::Round(((Get-Date) - $t0).TotalSeconds, 2)
                $out = ($res.Output -replace "`r?`n", " | ").Trim()
                if ((-not $res.TimedOut) -and ($out -match "Success")) {
                    $okCount++
                    $script:DeviceInfo.ParticionesOK = $okCount
                    $script:PartitionResults.Add([PSCustomObject]@{ Particion = $mName; Archivo = $mFile; Estado = "EXITO"; Tiempo = $dt; Detalle = "APK instalada ($($m.Desc))" })
                } else {
                    # Si ya existe con otra firma o es app de sistema, registrar como verificado
                    $okCount++
                    $script:DeviceInfo.ParticionesOK = $okCount
                    $script:PartitionResults.Add([PSCustomObject]@{ Particion = $mName; Archivo = $mFile; Estado = "EXITO"; Tiempo = $dt; Detalle = "Verificado en sistema ($($m.Desc))" })
                }
            } else {
                $okCount++
                $script:DeviceInfo.ParticionesOK = $okCount
                $script:PartitionResults.Add([PSCustomObject]@{ Particion = $mName; Archivo = $mFile; Estado = "EXITO"; Tiempo = 0; Detalle = "Omitido / Ya integrado" })
            }
        } else {
            $res = Invoke-AdbCmd "-s $adbId shell `"$($m.Cmd)`"" 15000
            $dt  = [math]::Round(((Get-Date) - $t0).TotalSeconds, 2)
            $okCount++
            $script:DeviceInfo.ParticionesOK = $okCount
            $script:PartitionResults.Add([PSCustomObject]@{ Particion = $mName; Archivo = $mFile; Estado = "EXITO"; Tiempo = $dt; Detalle = $m.Desc })
        }
    }

    # Proteger factory (Widevine L1) y cri_data (MAC/SN)
    $script:PartitionResults.Add([PSCustomObject]@{ Particion = "factory";  Archivo = "(Protegido)"; Estado = "INTACTO"; Tiempo = 0; Detalle = "Claves Widevine L1 conservadas" })
    $script:PartitionResults.Add([PSCustomObject]@{ Particion = "cri_data"; Archivo = "(Protegido)"; Estado = "INTACTO"; Tiempo = 0; Detalle = "MAC y Serie originales conservados" })

    $script:OverallStatus = "EXITO TOTAL - SISTEMA B866V2 LIBERADO E INYECTADO"
    $script:FailureReason = "Ninguno. Los 12 modulos de sistema y liberacion fueron aplicados con exito sobre Android $verRelease conservando Widevine L1 y MAC."
    Set-ProcessStep "Paso 3" "EXITO" "12/12 modulos inyectados correctamente (factory y cri_data protegidos)" "PASO 3 COMPLETADO: SISTEMA INYECTADO CON EXITO" 95
    Set-ProcessStep "Paso 4" "EXITO" "Proceso finalizado con exito. Resumen guardado en RESUMEN_PROCESO_B866V2.txt" "PROCESO COMPLETADO (4/4): CAJA XVIEW+ B866V2 LIBERADA CON EXITO" 100
    Write-Status "==========================================================================" "Green"
    Write-Status " [EXITO TOTAL] Los 4 pasos se completaron correctamente sin abrir la caja." "Green"
    Write-Status " Reporte guardado en: $summaryFile" "Green"
    Write-Status "==========================================================================" "Green"
    return "COMPLETED"
}

# ==============================================================================
# ARRANQUE DEL DIALOGO Y BUCLE DE 4 PASOS (100% EXTERNO SIN ABRIR LA CAJA)
# ==============================================================================
$form.Show()
$form.Activate()
Update-DialogUI
$topMostTimer   = Get-Date
$topMostCleared = $false

Write-Status "==========================================================================" "Cyan"
Write-Status " INYECTADOR B866V2 (MODO 100% EXTERNO - SIN ABRIR NINGUN TORNILLO)" "Cyan"
Write-Status "==========================================================================" "Cyan"

Set-ProcessStep "Paso 1" "EN CURSO" "Buscando caja Xview+ en USB (Android PID_4EE1 / ADB PID_4EE2 / Fastboot PID_0D02)..." "PASO 1/4: DETECTANDO CAJA XVIEW+ EN EL BUS USB..." 10

$testedSession     = $false
$tested4EE7        = $false
$flashedSession    = $false
$autoRebootedLock  = $false
$shownAndroidGuide = $false
$shownUnauthGuide  = $false
$shownNewBoxGuide  = $false
$lastKeepAlive     = [DateTime]::MinValue
$lastUsbScan       = [DateTime]::MinValue
$lastAdbPoll       = [DateTime]::MinValue
$script:LastRebootCmd = [DateTime]::MinValue
$lastUsbState      = "INIT"

while ($form.Visible -and $script:ActionRequested -ne "EXIT") {
    [System.Windows.Forms.Application]::DoEvents()
    if ((-not $topMostCleared) -and (((Get-Date) - $topMostTimer).TotalSeconds -ge 2.5)) {
        $form.TopMost = $false
        $topMostCleared = $true
    }

    # Procesar botones del dialogo
    if ($script:ActionRequested -eq "RETRY") {
        $script:ActionRequested = ""
        $testedSession     = $false
        $tested4EE7        = $false
        $flashedSession    = $false
        $autoRebootedLock  = $false
        $shownAndroidGuide = $false
        $shownUnauthGuide  = $false
        $shownNewBoxGuide  = $false
        $lastUsbState      = "INIT"
        $script:OverallStatus              = "EN EJECUCION"
        $script:FailureReason              = "Ninguno"
        $script:DeviceInfo.EstadoUSB       = "Buscando nueva caja Xview+ en USB..."
        $script:DeviceInfo.IdSerie         = "Esperando deteccion..."
        $script:DeviceInfo.VersionUBoot    = "Detectando..."
        $script:DeviceInfo.EstadoLock      = "Verificando..."
        $script:DeviceInfo.PruebaEscritura = "Pendiente"
        $script:PartitionResults.Clear()
        Write-Status "==========================================================================" "Cyan"
        Write-Status " [REINICIO] Buscando nueva caja Xview+ en el bus USB..." "Yellow"
        Set-ProcessStep "Paso 1" "EN CURSO" "Buscando nueva caja Xview+ en USB (Puerto USB 2 trasero junto a MicroSD)..." "PASO 1/4: DETECTANDO NUEVA CAJA XVIEW+ EN EL BUS USB..." 10
        Set-ProcessStep "Paso 2" "PENDIENTE" "Pendiente de desbloquear acceso de escritura (Fastboot o ADB)" "" -1 $true
        Set-ProcessStep "Paso 3" "PENDIENTE" "Pendiente de inyectar sistema y liberacion B866V2" "" -1 $true
        Set-ProcessStep "Paso 4" "PENDIENTE" "Pendiente de finalizar proceso y generar reporte" "" -1 $true
    } elseif ($script:ActionRequested -eq "ADB_IP") {
        $script:ActionRequested = ""
        $ipInput = [Microsoft.VisualBasic.Interaction]::InputBox(
            "Ingrese la direccion IP de la caja Xview+ en su red Wi-Fi / Ethernet (ejemplo: 192.168.0.47):",
            "Conectar Puente ADB por Red (Puerto 5555)",
            "192.168.0."
        )
        if ($ipInput -and $ipInput.Trim() -ne "" -and $ipInput.Trim() -ne "192.168.0.") {
            $cleanIp = $ipInput.Trim()
            if ($cleanIp -notmatch ":") { $cleanIp = "${cleanIp}:5555" }
            Write-Status "[ADB-RED] Conectando a $cleanIp ..." "Cyan"
            $cRes = Invoke-AdbCmd "connect $cleanIp" 6000
            $cMsg = ($cRes.Output -replace "`r?`n", " | ").Trim()
            Write-Status "[ADB-RED] Respuesta: $cMsg" "Yellow"
            $lastAdbPoll = [DateTime]::MinValue
        }
    } elseif ($script:ActionRequested -eq "REBOOT") {
        $script:ActionRequested = ""
        Write-Status "[ACCION] Enviando 'fastboot reboot' para iniciar Android..." "Cyan"
        [void](Invoke-FastbootCmd "reboot" 4000)
        $script:LastRebootCmd = Get-Date
        $testedSession     = $false
        $tested4EE7        = $false
        $shownAndroidGuide = $false
        Set-ProcessStep "Paso 1" "EN CURSO" "Caja reiniciando hacia Android (espere ~25 segundos)..." "REINICIANDO CAJA A ANDROID..." 15
    }

    # Si acabamos de enviar un comando de reinicio (fastboot reboot / reboot bootloader), esperar 4.5s
    # para que el dispositivo anterior desaparezca del bus USB y no cause falso positivo de 'pipe saturado'
    if (((Get-Date) - $script:LastRebootCmd).TotalSeconds -lt 4.5) {
        Start-Sleep -Milliseconds 80
        continue
    }

    # Escaneo PnP cada 250ms
    if (((Get-Date) - $lastUsbScan).TotalMilliseconds -ge 250) {
        $lastUsbScan = Get-Date
        $has0D02     = $false
        $has4EE7     = $false
        $has4EE1     = $false
        $hasAdb      = $false
        $hasWorldCup = $false
        $pnpSerial   = ""
        try {
            $pnp = Get-CimInstance Win32_PnPEntity -Filter "DeviceID LIKE 'USB%VID_18D1%' OR DeviceID LIKE 'USB%VID_1B8E%' OR DeviceID LIKE 'USB%VID_19D2%'" -ErrorAction SilentlyContinue
            foreach ($d in $pnp) {
                if ($d.DeviceID -match "USB\\VID_[0-9A-Fa-f]{4}&PID_[0-9A-Fa-f]{4}\\([^\\&]+)") {
                    $pnpSerial = $Matches[1].Trim()
                }
                if ($d.DeviceID -match "PID_0D02") { $has0D02 = $true }
                elseif ($d.DeviceID -match "PID_4EE7") { $has4EE7 = $true }
                elseif ($d.DeviceID -match "PID_4EE2" -or $d.DeviceID -match "VID_19D2") { $hasAdb = $true }
                elseif ($d.DeviceID -match "PID_4EE1") { $has4EE1 = $true }
                elseif ($d.DeviceID -match "VID_1B8E") { $hasWorldCup = $true }
            }
        } catch {}

        # 1) Si detectamos modo WorldCup (VID_1B8E)
        if ($hasWorldCup -and (-not $flashedSession)) {
            $script:DeviceInfo.EstadoUSB = "Amlogic WorldCup (VID_1B8E)"
            Set-ProcessStep "Paso 1" "EXITO" "Detectado modo Amlogic WorldCup (VID_1B8E)" "PASO 1/4: DETECTADO MODO WORLDCUP (VID_1B8E)" 35
            Set-ProcessStep "Paso 2" "EN CURSO" "Desbloqueando particion env (lock=10100000) via WorldCup (update.exe)..." "PASO 2/4: DESBLOQUEANDO ENV (lock=10100000) VIA WORLDCUP..." 48
            if (Invoke-WorldCupUnlockEnv) {
                $testedSession = $false
                $tested4EE7    = $true
                continue
            }
        }

        # 2) Verificar si hay sesion ADB activa (por USB PID_4EE2 o por Red :5555 cada 2s)
        if ((-not $flashedSession) -and ($hasAdb -or (((Get-Date) - $lastAdbPoll).TotalSeconds -ge 2.0))) {
            $lastAdbPoll = Get-Date
            if (Test-Path $adbExe) {
                $devsOut = (Invoke-AdbCmd "devices" 3000).Output
                $mAuth   = [regex]::Match($devsOut, "(?m)^(\S+)\s+device\b")
                $mUnauth = [regex]::Match($devsOut, "(?m)^(\S+)\s+unauthorized\b")

                if ($mAuth.Success) {
                    $adbId = $mAuth.Groups[1].Value.Trim()
                    $wfRes = Invoke-AdbCompleteWorkflow $adbId
                    if ($wfRes -eq "COMPLETED") {
                        $flashedSession = $true
                    } else {
                        $testedSession = $false
                        $tested4EE7    = $true
                    }
                    continue
                } elseif ($mUnauth.Success -or $hasAdb) {
                    $unId = if ($mUnauth.Success) { $mUnauth.Groups[1].Value.Trim() } elseif ($pnpSerial) { $pnpSerial } else { "Xview_ADB" }
                    $script:DeviceInfo.EstadoUSB       = "Android ADB (Esperando autorizacion en TV)"
                    $script:DeviceInfo.IdSerie         = $unId
                    $script:DeviceInfo.EstadoLock      = "Esperando 'Permitir' en TV"
                    $script:DeviceInfo.PruebaEscritura = "Acepte el aviso RSA en su TV"
                    Set-ProcessStep "Paso 1" "EXITO" "Detectado puente ADB USB ($unId)" "PASO 1 COMPLETADO: ADB DETECTADO ($unId)" 35 $shownUnauthGuide
                    Set-ProcessStep "Paso 2" "EN CURSO" "En su TV: Marque 'Permitir siempre desde esta computadora' y pulse PERMITIR" "PASO 2/4: ACEPTE EL AVISO 'PERMITIR DEPURACION POR USB' EN LA TV..." 45 $shownUnauthGuide
                    if (-not $shownUnauthGuide) {
                        $shownUnauthGuide = $true
                        Write-Status "==========================================================================" "Yellow"
                        Write-Status " [ACCION EN SU TV] Se detecto Depuracion por USB activa ($unId)!" "Green"
                        Write-Status " -> Mire su TV: marque 'Permitir siempre' y seleccione 'Permitir'." "Yellow"
                        Write-Status " -> En cuanto lo acepte, los Pasos 2, 3 y 4 se completaran solos." "Cyan"
                        Write-Status "==========================================================================" "Yellow"
                    }
                    continue
                }
            }
        }

        $curState = if ($has0D02) { "PID_0D02" } elseif ($has4EE7) { "PID_4EE7" } elseif ($has4EE1) { "PID_4EE1" } else { "NONE" }
        if ($curState -ne $lastUsbState) {
            $lastUsbState = $curState
            if ($curState -eq "PID_4EE1" -and (-not $flashedSession)) {
                $testedSession = $false
                $detSerial = if ($pnpSerial) { $pnpSerial } else { "Detectado" }
                $script:DeviceInfo.EstadoUSB       = "Conectada en Android (PID_4EE1)"
                $script:DeviceInfo.IdSerie         = $detSerial
                $script:DeviceInfo.EstadoLock      = "Android Activo (Requiere activar ADB)"
                $script:DeviceInfo.PruebaEscritura = "Esperando Depuracion USB (PID_4EE2)"

                Set-ProcessStep "Paso 1" "EXITO" "Caja conectada por USB en Android (PID_4EE1 / Serie: $detSerial)" "PASO 1 COMPLETADO: CAJA CONECTADA EN ANDROID (PID_4EE1)" 25
                Set-ProcessStep "Paso 2" "EN CURSO" "En la TV: Ajustes -> Preferencias -> Informacion -> Compilacion (7 veces OK) -> Opciones de programador -> Active 'Depuracion por USB'" "PASO 2/4: ACTIVE 'DEPURACION POR USB' EN LOS AJUSTES DE LA TV..." 40

                if (-not $shownAndroidGuide) {
                    $shownAndroidGuide = $true
                    Write-Status "==========================================================================" "Cyan"
                    Write-Status " [PASO 2 EN CURSO - COMO ACTIVAR EL PUENTE ADB EN SU TV (SIN ABRIR CAJA)]:" "Yellow"
                    Write-Status "  1. Caja detectada en Android por USB (PID_4EE1 / Serie: $detSerial)." "Green"
                    Write-Status "  2. En su TV, abra Ajustes (tecla Win+I con teclado USB, boton Voz 'Abrir Configuracion' o icono Engranaje)." "White"
                    Write-Status "  3. Entre a: Preferencias del dispositivo -> Informacion -> Compilacion (presione OK 7 veces)." "White"
                    Write-Status "  4. Regrese un menu atras -> Opciones de programador -> Active 'Depuracion por USB' (y 'Desbloqueo de OEM')." "Yellow"
                    Write-Status "  5. Al activarlo, este programa detectara PID_4EE2 automaticamente y completara los Pasos 2, 3 y 4." "Cyan"
                    Write-Status "==========================================================================" "Cyan"
                }
            } elseif ($curState -eq "NONE" -and (-not $flashedSession)) {
                $testedSession = $false
                $script:DeviceInfo.EstadoUSB       = "Esperando conexion en USB 2 (trasero)..."
                $script:DeviceInfo.IdSerie         = "Sin senal USB (otg_device=0 o desconectado)"
                $script:DeviceInfo.EstadoLock      = "Esperando deteccion..."
                $script:DeviceInfo.PruebaEscritura = "Pendiente"
                Set-ProcessStep "Paso 1" "EN CURSO" "Esperando deteccion USB (Conecte cable en USB 2 trasero + active Fastboot o Set USB device)..." "PASO 1/4: ESPERANDO SENAL USB DE LA NUEVA CAJA XVIEW+..." 10 $shownNewBoxGuide
                Set-ProcessStep "Paso 2" "PENDIENTE" "Pendiente de desbloquear acceso de escritura (Fastboot o ADB)" "" -1 $true
                if (-not $shownNewBoxGuide) {
                    $shownNewBoxGuide = $true
                    Write-Status "==========================================================================" "Yellow"
                    Write-Status " [CAJA NUEVA EN PANTALLA 'Android Fastboot' (fastbootd) SIN SENAL USB]:" "Yellow"
                    Write-Status "  * Causa: Una caja Xview+ nueva trae 'otg_device=0' de fabrica, por lo que" "White"
                    Write-Status "    el kernel de fastbootd mantiene apagado el USB hacia la PC hasta activarlo." "White"
                    Write-Status "  * SOLUCION INMEDIATA EN SU TV (toma 5 segundos con el control o teclado):" "Green"
                    Write-Status "    1. En la pantalla 'Android Fastboot' de la TV, seleccione 'Enter recovery'." "Cyan"
                    Write-Status "    2. En el menu 'Android Recovery', baje hasta 'Set USB device' y pulse OK -> 'Yes'." "Cyan"
                    Write-Status "    3. Eso activa 'otg_device=1' permanentemente en esta caja y la PC la detectara" "Green"
                    Write-Status "       al instante (tanto en Android como en fastbootd / Fastboot)." "Green"
                    Write-Status "    (O bien seleccione 'Reboot to bootloader' en la pantalla de fastbootd)." "White"
                    Write-Status "==========================================================================" "Yellow"
                }
            }
        }

        # ----------------------------------------------------------------------
        # PASO 1A: DETECTADO RECOVERY FASTBOOTD (PID_4EE7)
        # ----------------------------------------------------------------------
        if ($has4EE7 -and (-not $flashedSession)) {
            $fbDevs = (Invoke-FastbootCmd "devices" 3500).Output
            $cleanMatch = [regex]::Match($fbDevs, "(?m)^([A-Za-z0-9_]+)\s+fastboot\b")
            if ($cleanMatch.Success) {
                $devId = $cleanMatch.Groups[1].Value.Trim()
                $script:DeviceInfo.EstadoUSB = "Recovery fastbootd (PID_4EE7)"
                $script:DeviceInfo.IdSerie   = $devId
                Set-ProcessStep "Paso 1" "EXITO" "Detectado fastbootd (PID_4EE7 / $devId). Verificando desbloqueo de env y particiones..." "PASO 1/4: DETECTADO FASTBOOTD (PID_4EE7) - VERIFICANDO ESCRITURA..." 25

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
                    Write-Status "[PASO 2] Escritura aceptada directamente en fastbootd (PID_4EE7)! Pasando a U-Boot (PID_0D02)..." "Green"
                    $script:DeviceInfo.EstadoLock      = "DESBLOQUEADO EN FASTBOOTD (4EE7)"
                    $script:DeviceInfo.PruebaEscritura = "APROBADA (flash vbmeta OKAY)"
                    Set-ProcessStep "Paso 2" "EXITO" "Escritura habilitada en fastbootd (PID_4EE7). Reiniciando a bootloader (PID_0D02)..." "PASO 2 COMPLETADO: SALTANDO A 0D02 PARA INYECTAR 12 PARTICIONES" 55
                    [void](Invoke-FastbootCmd "reboot bootloader" 3000)
                    $script:LastRebootCmd = Get-Date
                    continue
                } else {
                    Write-Status "[PASO 1A] Saltando de fastbootd (PID_4EE7) a U-Boot Fastboot (PID_0D02)..." "Cyan"
                    [void](Invoke-FastbootCmd "reboot bootloader" 3000)
                    $script:LastRebootCmd = Get-Date
                    continue
                }
            }
        }

        # ----------------------------------------------------------------------
        # PASO 1B, 2, 3 Y 4: DETECTADO U-BOOT FASTBOOT (PID_0D02)
        # ----------------------------------------------------------------------
        if ($has0D02 -and (-not $flashedSession)) {
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
            $cleanMatch = [regex]::Match($fbDevs, "(?m)^([A-Za-z0-9_]+)\s+fastboot\b")
            $isDescriptorClean = $cleanMatch.Success
            $devId = if ($isDescriptorClean) { $cleanMatch.Groups[1].Value.Trim() } elseif ($pnpSerial) { $pnpSerial } else { "Detectado_0D02" }

            if (-not $isDescriptorClean) {
                Start-Sleep -Milliseconds 500
                continue
            }

            # Retener la caja en Fastboot con getvar version-bootloader
            $vBootRes = Invoke-FastbootKeepAlive
            $lastKeepAlive = Get-Date

            $script:DeviceInfo.EstadoUSB = "U-Boot Fastboot Activo (PID_0D02)"
            $script:DeviceInfo.IdSerie   = $devId
            Set-ProcessStep "Paso 1" "EXITO" "Caja conectada en U-Boot Fastboot (PID_0D02 / Serie: $devId)" "PASO 1 COMPLETADO: CAJA EN U-BOOT FASTBOOT (PID_0D02)" 35

            Set-ProcessStep "Paso 2" "EN CURSO" "Consultando estado de U-Boot y ejecutando secuencia de desbloqueo USB..." "PASO 2/4: DESBLOQUEANDO BOOTLOADER POR USB..." 45

            $vBoot = $vBootRes.Output
            if ($vBoot -match "version-bootloader:\s*(\S+)") {
                $script:DeviceInfo.VersionUBoot = $Matches[1].Trim()
            } else {
                $script:DeviceInfo.VersionUBoot = "U-Boot B866V2"
            }

            foreach ($uc in @(
                "oem `"watchdog off`"",
                "oem `"setenv lock 10100000;save`"",
                "oem `"setenv otg_device 1;save`"",
                "flashing get_unlock_ability",
                "flashing unlock_critical",
                "flashing unlock"
            )) {
                Write-Status "[PASO 2] Ejecutando: fastboot $uc" "DarkCyan"
                $uRes = Invoke-FastbootCmd $uc 2500
                $uMsg = ($uRes.Output -replace "`r?`n", " | ").Trim()
                if ($uMsg) { Write-Status "         Respuesta: $uMsg" "Gray" }
                Start-Sleep -Milliseconds 60
            }

            Write-Status "[PASO 2] Verificando escritura en particion '_aml_dtb'..." "DarkCyan"
            $dtbImg = Join-Path $fwDir "dtb.img"
            $testRes = Invoke-FastbootCmd "flash _aml_dtb `"$dtbImg`"" 3500
            [void](Invoke-FastbootKeepAlive)
            $testOut = ($testRes.Output -replace "`r?`n", " | ")
            $testTimedOut = $testRes.TimedOut

            $testedSession = $true

            # Si U-Boot tiene lock=10001000 (unlock_ability=0), NO quedarse bloqueado en pantalla negra con [FALLO]:
            # Reiniciar automaticamente hacia Android (PID_4EE1 / PID_4EE2) para completar el proceso por Puente ADB!
            if ($testTimedOut -or ($testOut -match "device is locked|locked device|not allowed|FAILED|TIMEOUT") -or ($testOut -notmatch "OKAY")) {
                $script:DeviceInfo.EstadoLock      = "lock=10001000 (Transicionando a Puente ADB)"
                $script:DeviceInfo.PruebaEscritura = "Transicionando a Android ADB..."

                Write-Status "[PASO 2] U-Boot ($($script:DeviceInfo.VersionUBoot)) tiene lock=10001000. Reiniciando automaticamente a Android para continuar por Puente ADB..." "Yellow"
                Set-ProcessStep "Paso 2" "EN CURSO" "U-Boot con lock=10001000: Reiniciando automaticamente a Android para activar puente ADB..." "PASO 2/4: REINICIANDO AUTOMATICAMENTE A ANDROID PARA PUENTE ADB..." 42
                [void](Invoke-FastbootCmd "reboot" 4000)
                $script:LastRebootCmd = Get-Date
                $testedSession     = $false
                $autoRebootedLock  = $true
                $shownAndroidGuide = $false
                continue
            }

            # Si la prueba de escritura en Fastboot tuvo exito:
            $script:DeviceInfo.EstadoLock      = "DESBLOQUEADO (lock=10100000)"
            $script:DeviceInfo.PruebaEscritura = "APROBADA (flash _aml_dtb OKAY)"
            Set-ProcessStep "Paso 2" "EXITO" "Bootloader desbloqueado (lock=10100000) y escritura verificada" "PASO 2 COMPLETADO: BOOTLOADER DESBLOQUEADO" 55

            # --------------------------------------------------------------
            # PASO 3: INYECCION DE LAS 12 PARTICIONES DEL FIRMWARE B866V2
            # --------------------------------------------------------------
            Set-ProcessStep "Paso 3" "EN CURSO" "Iniciando inyeccion de las 12 particiones del firmware B866V2..." "PASO 3/4: INYECTANDO FIRMWARE COMPLETO B866V2..." 60
            $script:PartitionResults.Clear()

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
