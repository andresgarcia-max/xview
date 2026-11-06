$repos = @(
    @{ Name = "fitlife-design-ui"; Owner = "codewithsadee"; Repo = "fitlife" },
    @{ Name = "futras-fitness-ui"; Owner = "codewithsadee"; Repo = "futras" },
    @{ Name = "responsive-gym-website-bedimcode"; Owner = "bedimcode"; Repo = "responsive-gym-website" },
    @{ Name = "GimnasioQR-control-acceso-django"; Owner = "elJulioDev"; Repo = "GimnasioQR" },
    @{ Name = "GymPulse-qr-access-react-go"; Owner = "Miftah-Fentaw"; Repo = "GymPulse" },
    @{ Name = "GYM-One-management-system"; Owner = "mayerbalintdev"; Repo = "GYM-One" },
    @{ Name = "gyms-qr-attendance-scanner"; Owner = "rjgaton007"; Repo = "gyms" },
    @{ Name = "gym-management-nextjs-tailwind"; Owner = "atharvaarbat"; Repo = "gym-management" },
    @{ Name = "GymMaster-react-tailwind-ui"; Owner = "AdamyoussefEg"; Repo = "GymMaster" },
    @{ Name = "voltfit-dashboard-analytics"; Owner = "kendrekaran"; Repo = "voltfit-dashboard" }
)

$destDir = "c:\Users\andre\OneDrive\Desktop\gym\repositorios"
New-Item -ItemType Directory -Force -Path $destDir | Out-Null

foreach ($r in $repos) {
    $zipPath = Join-Path $destDir "$($r.Name).zip"
    $urlMain = "https://github.com/$($r.Owner)/$($r.Repo)/archive/refs/heads/main.zip"
    $urlMaster = "https://github.com/$($r.Owner)/$($r.Repo)/archive/refs/heads/master.zip"
    
    Write-Host "Downloading $($r.Owner)/$($r.Repo)..."
    curl.exe -L -s -f -o $zipPath $urlMain
    if ($LASTEXITCODE -ne 0) {
        curl.exe -L -s -f -o $zipPath $urlMaster
    }
    if ($LASTEXITCODE -eq 0 -and (Test-Path $zipPath)) {
        Write-Host "Extracting $($r.Name).zip..."
        tar.exe -xf $zipPath -C $destDir
        if ($LASTEXITCODE -eq 0) {
            Remove-Item $zipPath -Force
            Write-Host "Extracted $($r.Name) successfully."
        } else {
            Write-Host "Failed to extract $($r.Name).zip"
        }
    } else {
        Write-Host "Failed to download $($r.Owner)/$($r.Repo)"
    }
}
Get-ChildItem -Path $destDir -Directory | Select-Object -ExpandProperty Name
