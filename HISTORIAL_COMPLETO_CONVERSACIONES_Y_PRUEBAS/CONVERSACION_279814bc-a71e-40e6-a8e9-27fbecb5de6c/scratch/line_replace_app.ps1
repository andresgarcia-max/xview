$file = "LEGENDS\src\App.tsx"
$lines = [System.IO.File]::ReadAllLines((Resolve-Path $file), [System.Text.Encoding]::UTF8)

$newLines = @()
$foundImport = $false
$foundDiv = $false
$foundMain = $false

for ($i = 0; $i -lt $lines.Length; $i++) {
    $line = $lines[$i]
    if (!$foundImport -and $line.Contains("import { IndependentTerminalApp }")) {
        $newLines += $line
        $newLines += "import gymDumbbellBg from './assets/gym-dumbbell-background.jpg';"
        $foundImport = $true
        continue
    }

    if (!$foundDiv -and $line.Contains('<div className="min-h-screen bg-gym-bg')) {
        $newLines += '    <div className="min-h-screen bg-[#07080b] text-gym-text flex flex-col font-sans select-none sm:select-auto relative overflow-hidden">'
        $newLines += '      {/* Background Layer: Gym Dumbbells Texture & Ambient Lighting (Opción 1) */}'
        $newLines += '      <div className="fixed inset-0 pointer-events-none z-0 overflow-hidden">'
        $newLines += '        <img'
        $newLines += '          src={gymDumbbellBg}'
        $newLines += '          alt="Gym Dumbbell Background"'
        $newLines += '          className="w-full h-full object-cover filter brightness-[0.38] contrast-[1.12] saturate-[0.85]"'
        $newLines += '        />'
        $newLines += '        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_50%_15%,rgba(245,158,11,0.08)_0%,rgba(0,0,0,0.65)_70%)]" />'
        $newLines += '        <div className="absolute inset-0 bg-gradient-to-b from-black/45 via-black/35 to-black/70" />'
        $newLines += '      </div>'
        $foundDiv = $true
        continue
    }

    if ($line.Contains('<div className="flex-1 flex overflow-hidden">')) {
        $newLines += '      <div className="flex-1 flex overflow-hidden relative z-10">'
        continue
    }

    if (!$foundMain -and $line.Contains('bg-gradient-to-b from-gym-bg to-[#07080c]')) {
        $newLines += '        } bg-black/25 backdrop-blur-[0.5px]`}>'
        $foundMain = $true
        continue
    }

    $newLines += $line
}

[System.IO.File]::WriteAllLines((Resolve-Path $file), $newLines, [System.Text.Encoding]::UTF8)
Write-Host "Replaced line-by-line: foundImport=$foundImport, foundDiv=$foundDiv, foundMain=$foundMain"
