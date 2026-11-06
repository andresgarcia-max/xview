Add-Type -AssemblyName System.Drawing
$filePath = "C:\Users\andre\.gemini\antigravity\brain\279814bc-a71e-40e6-a8e9-27fbecb5de6c\.user_uploaded\media_1792853765345_6f88d54d.png"
$img = [System.Drawing.Image]::FromFile($filePath)
Write-Host "Image 1 - Width: $($img.Width), Height: $($img.Height)"
$bmp = New-Object System.Drawing.Bitmap($img)
Write-Host "Pixel (5, 5):" ($bmp.GetPixel(5, 5))
Write-Host "Pixel (mid, mid):" ($bmp.GetPixel([int]($img.Width/2), [int]($img.Height/2)))
Write-Host "Pixel (mid, 10):" ($bmp.GetPixel([int]($img.Width/2), 10))
$bmp.Dispose()
$img.Dispose()
