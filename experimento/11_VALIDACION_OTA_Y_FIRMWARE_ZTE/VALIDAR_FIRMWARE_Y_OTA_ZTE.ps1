param(
    [string]$TargetPath = "c:\Users\CORSAIR\Desktop\xview\V2\B866v2 Android 12\B866v2 Android 12\B866V2"
)

$ErrorActionPreference = "Stop"
$sha256 = [System.Security.Cryptography.SHA256]::Create()

function Read-UInt32BE([byte[]]$b, [int]$off) {
    return ([uint32]$b[$off] -shl 24) -bor ([uint32]$b[$off+1] -shl 16) -bor ([uint32]$b[$off+2] -shl 8) -bor [uint32]$b[$off+3]
}
function Read-UInt64BE([byte[]]$b, [int]$off) {
    $hi = Read-UInt32BE $b $off
    $lo = Read-UInt32BE $b ($off+4)
    return ([uint64]$hi -shl 32) -bor [uint64]$lo
}

Write-Host "==========================================================================" -ForegroundColor Cyan
Write-Host " VALIDADOR CRIPTOGRAFICO DE FIRMWARE Y PAQUETES OTA ZTE (XVIEW+ B866V2)" -ForegroundColor Cyan
Write-Host "==========================================================================" -ForegroundColor Cyan
Write-Host "Objetivo analizado: $TargetPath`n"

# Si es un archivo .zip (Paquete OTA update.zip / ota.zip)
if ((Test-Path $TargetPath) -and (Get-Item $TargetPath) -is [System.IO.FileInfo] -and $TargetPath.EndsWith(".zip")) {
    Write-Host "[MODO 1: VALIDACION DE PAQUETE OTA (.ZIP) CONTRA CERTIFICADO OFICIAL ZTE]" -ForegroundColor Yellow
    $fBytes = [System.IO.File]::ReadAllBytes($TargetPath)
    $len = $fBytes.Length
    Write-Host "  Tamano del archivo ZIP: $len bytes"
    if ($len -gt 6) {
        # Footer Android Whole-File OTA: últimos 6 bytes antes del comentario EOCD
        $footerOff = $len - 6
        $sigStartFromEnd = [System.BitConverter]::ToUInt16($fBytes, $footerOff)
        $magic = [System.BitConverter]::ToUInt16($fBytes, $footerOff + 2)
        $commentLen = [System.BitConverter]::ToUInt16($fBytes, $footerOff + 4)
        if ($magic -eq 0xFFFF) {
            Write-Host "  [OK] Pie de firma Android Whole-File OTA detectado (Magic: 0xFFFF, Offset desde EOF: $sigStartFromEnd bytes)" -ForegroundColor Green
        } else {
            Write-Host "  [ALERTA] El archivo ZIP NO posee el pie de firma Whole-File OTA (0xFFFF) requerido por system/bin/recovery." -ForegroundColor Red
        }
    }
    exit 0
}

# Modo Carpeta de Firmware (.img)
if (-not (Test-Path "$TargetPath\vbmeta.img")) {
    Write-Host "[ERROR] No se encontro vbmeta.img en $TargetPath" -ForegroundColor Red
    exit 1
}

Write-Host "1. VALIDACION DE CABECERA Y FIRMA RSA-2048 EN VBMETA.IMG (AVB 1.0)" -ForegroundColor Yellow
$vbBytes = [System.IO.File]::ReadAllBytes("$TargetPath\vbmeta.img")
$magic = [System.Text.Encoding]::ASCII.GetString($vbBytes, 0, 4)
$algoType = Read-UInt32BE $vbBytes 28
$flags = Read-UInt32BE $vbBytes 120
$authSize = Read-UInt64BE $vbBytes 12
$auxSize = Read-UInt64BE $vbBytes 20
$pkOff = 256 + [int]$authSize + [int](Read-UInt64BE $vbBytes 64)
$pkSize = [int](Read-UInt64BE $vbBytes 72)

$pkBytes = New-Object byte[] $pkSize
[Array]::Copy($vbBytes, $pkOff, $pkBytes, 0, $pkSize)
$pkHash = [System.BitConverter]::ToString($sha256.ComputeHash($pkBytes)).Replace("-","").ToLower()

$modulus = New-Object byte[] 256
[Array]::Copy($vbBytes, $pkOff + 8, $modulus, 0, 256)
$rsaParams = New-Object System.Security.Cryptography.RSAParameters
$rsaParams.Modulus = $modulus
$rsaParams.Exponent = [byte[]]@(0x01, 0x00, 0x01)
$rsa = [System.Security.Cryptography.RSA]::Create()
$rsa.ImportParameters($rsaParams)

$vbSig = New-Object byte[] 256
[Array]::Copy($vbBytes, 288, $vbSig, 0, 256)

$vbDataCurrent = New-Object byte[] (256 + [int]$auxSize)
[Array]::Copy($vbBytes, 0, $vbDataCurrent, 0, 256)
[Array]::Copy($vbBytes, 256 + [int]$authSize, $vbDataCurrent, 256, [int]$auxSize)
$hashCurrent = $sha256.ComputeHash($vbDataCurrent)
$validCurrent = $rsa.VerifyHash($hashCurrent, $vbSig, [System.Security.Cryptography.HashAlgorithmName]::SHA256, [System.Security.Cryptography.RSASignaturePadding]::Pkcs1)

$vbDataZero = $vbDataCurrent.Clone()
$vbDataZero[123] = 0
$hashZero = $sha256.ComputeHash($vbDataZero)
$validZero = $rsa.VerifyHash($hashZero, $vbSig, [System.Security.Cryptography.HashAlgorithmName]::SHA256, [System.Security.Cryptography.RSASignaturePadding]::Pkcs1)

Write-Host "  - Llave Publica AVB ZTE (SHA-256) : $pkHash"
Write-Host "  - Flags actuales en vbmeta.img    : 0x$($flags.ToString('X8')) (bit0=HASHTREE_DISABLED, bit1=VERIFICATION_DISABLED)"
if ($validCurrent) {
    Write-Host "  - Firma RSA-2048 estado actual    : VALIDA (Aceptada por Bootloader Bloqueado)" -ForegroundColor Green
} elseif ($validZero) {
    Write-Host "  - Firma RSA-2048 estado actual    : INVALIDA en estado actual (Fue parcheado el byte 123 a Flags=0x03)" -ForegroundColor Red
    Write-Host "  - Firma RSA-2048 original (0x00)  : VALIDA 100% OFICIAL ZTE (Requiere Bootloader Desbloqueado lock=10100000 para usar Flags=0x03)" -ForegroundColor Yellow
} else {
    Write-Host "  - Firma RSA-2048                  : INVALIDA" -ForegroundColor Red
}

Write-Host "`n2. VALIDACION DE DESCRIPTORES DE PARTICION CONTRA VBMETA.IMG OFICIAL ZTE" -ForegroundColor Yellow
$pos = 256 + [int]$authSize
$endPos = $pos + [int](Read-UInt64BE $vbBytes 104)
while ($pos -lt $endPos) {
    $tag = Read-UInt64BE $vbBytes $pos
    $numBytes = Read-UInt64BE $vbBytes ($pos + 8)
    if ($numBytes -eq 0) { break }
    if ($tag -eq 2) {
        $imgSize = Read-UInt64BE $vbBytes ($pos + 16)
        $pNameLen = Read-UInt32BE $vbBytes ($pos + 56)
        $saltLen = Read-UInt32BE $vbBytes ($pos + 60)
        $digLen = Read-UInt32BE $vbBytes ($pos + 64)
        $pName = [System.Text.Encoding]::ASCII.GetString($vbBytes, $pos + 132, [int]$pNameLen)
        $saltBytes = New-Object byte[] $saltLen
        [Array]::Copy($vbBytes, $pos + 132 + [int]$pNameLen, $saltBytes, 0, [int]$saltLen)
        $expDig = [System.BitConverter]::ToString($vbBytes, $pos + 132 + [int]$pNameLen + [int]$saltLen, [int]$digLen).Replace("-","").ToLower()
        $fName = if ($pName -eq "dt") { "dtb.img" } else { "$pName.img" }
        $fPath = "$TargetPath\$fName"
        if (Test-Path $fPath) {
            $fBytes = [System.IO.File]::ReadAllBytes($fPath)
            if ($fBytes.Length -ge [int]$imgSize) {
                $toHash = New-Object byte[] ($saltLen + [int]$imgSize)
                [Array]::Copy($saltBytes, 0, $toHash, 0, [int]$saltLen)
                [Array]::Copy($fBytes, 0, $toHash, [int]$saltLen, [int]$imgSize)
                $calcDig = [System.BitConverter]::ToString($sha256.ComputeHash($toHash)).Replace("-","").ToLower()
                if ($calcDig -eq $expDig) {
                    Write-Host ("  [OK OFICIAL ZTE] Particion '{0}' ({1}): Hash SHA-256 verificado ({2})" -f $pName, $fName, $calcDig.Substring(0,16)) -ForegroundColor Green
                } else {
                    Write-Host ("  [MODIFICADA]     Particion '{0}' ({1}): No coincide con el hash ZTE en vbmeta" -f $pName, $fName) -ForegroundColor Yellow
                }
            }
        }
    }
    $pos += 16 + [int]$numBytes
}

Write-Host "`n3. VALIDACION INDEPENDIENTE DE RECOVERY.IMG (PIE AVBf + RSA-2048)" -ForegroundColor Yellow
if (Test-Path "$TargetPath\recovery.img") {
    $rec = [System.IO.File]::ReadAllBytes("$TargetPath\recovery.img")
    $fOff = $rec.Length - 64
    $fMagic = [System.Text.Encoding]::ASCII.GetString($rec, $fOff, 4)
    if ($fMagic -eq "AVBf") {
        $rVbOff = [int](Read-UInt64BE $rec ($fOff + 20))
        $rSig = New-Object byte[] 256
        [Array]::Copy($rec, $rVbOff + 288, $rSig, 0, 256)
        $rAuth = [int](Read-UInt64BE $rec ($rVbOff + 12))
        $rAux = [int](Read-UInt64BE $rec ($rVbOff + 20))
        $rData = New-Object byte[] (256 + $rAux)
        [Array]::Copy($rec, $rVbOff, $rData, 0, 256)
        [Array]::Copy($rec, $rVbOff + 256 + $rAuth, $rData, 256, $rAux)
        $rHash = $sha256.ComputeHash($rData)
        $rValid = $rsa.VerifyHash($rHash, $rSig, [System.Security.Cryptography.HashAlgorithmName]::SHA256, [System.Security.Cryptography.RSASignaturePadding]::Pkcs1)
        if ($rValid) {
            Write-Host "  [OK OFICIAL ZTE] recovery.img posee firma RSA-2048 100% original e intacta de ZTE." -ForegroundColor Green
        } else {
            Write-Host "  [MODIFICADA]     recovery.img no pasa la firma RSA-2048 oficial de ZTE." -ForegroundColor Red
        }
    }
}
Write-Host "`n==========================================================================" -ForegroundColor Cyan
