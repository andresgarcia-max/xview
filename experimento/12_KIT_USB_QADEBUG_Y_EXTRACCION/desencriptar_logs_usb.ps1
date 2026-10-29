param(
    [string]$UsbDrive = "D:\"
)

$outBase = "c:\Users\CORSAIR\Desktop\xview\experimento\12_KIT_USB_QADEBUG_Y_EXTRACCION\DATOS_EXTRAIDOS_DE_LA_CAJA"
New-Item -ItemType Directory -Force -Path $outBase | Out-Null

Write-Host "================================================================================" -ForegroundColor Cyan
Write-Host "  ANALIZADOR Y DESENCRIPTADOR DE DATOS VOLCADOS POR LA CAJA XVIEW EN LA USB" -ForegroundColor Cyan
Write-Host "================================================================================" -ForegroundColor Cyan

if (-not (Test-Path $UsbDrive)) {
    Write-Host "[ALERTA] No se detecta la unidad $UsbDrive conectada." -ForegroundColor Yellow
    exit 1
}

# Tabla UCap_TranslateMap simétrica para desencriptar logs de ZTE
$origMap = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
$crypMap = "RSTUVWXYZPONMLKEDCBAQJIHGFrstuvwxyzponmlkedcbaqjihgf"
function Decode-UCapText([string]$enc) {
    $chars = $enc.ToCharArray()
    for ($i = 0; $i -lt $chars.Length; $i++) {
        $idx = $crypMap.IndexOf($chars[$i])
        if ($idx -ge 0) { $chars[$i] = $origMap[$idx] }
    }
    return ($chars -join '')
}

# Buscar carpetas U_* o LOG_ZTESTB o archivos .info / ztecfg generados por la caja
$foundDirs = Get-ChildItem -Path $UsbDrive -Directory -ErrorAction SilentlyContinue | Where-Object { $_.Name -match "^U_|LOG_ZTESTB|MiddleWareCfgInfo|ProductCfgInfo|STARTUPLOG" }
$foundFiles = Get-ChildItem -Path $UsbDrive -Recurse -File -ErrorAction SilentlyContinue | Where-Object { $_.Name -match "ifconfig\.info|route\.info|ps\.info|top\.info|HDMI\.info|ztecfg|\.pcap|logsvr\.log|ExternalStorage\.ini" }

if (-not $foundDirs -and -not $foundFiles) {
    Write-Host "[RESULTADO] Aún no se encontraron carpetas de volcado (U_<MAC>_<FECHA>) en $UsbDrive." -ForegroundColor Yellow
    Write-Host "  - Si ya insertaste la USB en la caja y no se creó ninguna carpeta:"
    Write-Host "    1) Verifica si la caja tiene versión de software distinta a V84711310.1024 / V82815417.2033."
    Write-Host "    2) Recuerda que en el perfil Izzi (4301_4) basecomp bloquea MEDIA_MOUNTED ('not start U disk log')."
    exit 0
}

Write-Host "[OK] Se detectaron archivos/carpetas generados por la caja Xview en $UsbDrive!" -ForegroundColor Green
foreach ($f in $foundFiles) {
    $dest = Join-Path $outBase $f.Name
    Copy-Item $f.FullName $dest -Force
    Write-Host "  -> Copiado: $($f.FullName) ($($f.Length) bytes)" -ForegroundColor Green
    if ($f.Extension -in @(".info", ".log", ".txt")) {
        $raw = [System.IO.File]::ReadAllText($f.FullName)
        if ($raw -match "Tkmmkl|mncd|qz|J8") {
            $dec = Decode-UCapText $raw
            [System.IO.File]::WriteAllText("$dest.DESENCRIPTADO.txt", $dec, [System.Text.Encoding]::UTF8)
            Write-Host "     [Desencriptado UCap guardado en $dest.DESENCRIPTADO.txt]" -ForegroundColor Cyan
        }
    }
}
Write-Host "`nTodos los archivos extraídos se guardaron en: $outBase" -ForegroundColor Cyan
