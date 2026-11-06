Add-Type -AssemblyName System.Drawing

$images = @(
    "gym-dumbbell-background.jpg",
    "dumbbell_widescreen_test.jpg",
    "wide_dumbbell_seamless.jpg",
    "modal-gym-bg-hd.jpg",
    "LEGENDS\src\assets\modal-gym-bg.jpg",
    "LEGENDS\src\assets\web-gym-weights.jpg",
    "LEGENDS\src\assets\gym-background-club-perfect.jpg"
)

foreach ($imgName in $images) {
    if (Test-Path $imgName) {
        $img = [System.Drawing.Image]::FromFile((Resolve-Path $imgName))
        Write-Host "$imgName -> $($img.Width)x$($img.Height)"
        $img.Dispose()
    }
}
