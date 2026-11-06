$files = @(
    "ACCESO_WEB_FITNESS_LEGENDS.html",
    "Control_De_Acceso_Web.html",
    "LEGENDS\public\Control_De_Acceso_Web.html",
    "LEGENDS\public\terminal.html",
    "LEGENDS\dist\Control_De_Acceso_Web.html",
    "LEGENDS\dist\terminal.html",
    "LEGENDS\release\FITNESS_LEGENDS_TERMINAL-win32-x64\resources\app\dist\Control_De_Acceso_Web.html",
    "LEGENDS\release\FITNESS_LEGENDS_TERMINAL-win32-x64\resources\app\dist\terminal.html",
    "LEGENDS\release\FITNESS_LEGENDS_TERMINAL-win32-x64\resources\app\public\Control_De_Acceso_Web.html",
    "LEGENDS\release\FITNESS_LEGENDS_TERMINAL-win32-x64\resources\app\public\terminal.html"
)

foreach ($f in $files) {
    if (Test-Path $f) {
        $content = [System.IO.File]::ReadAllText((Resolve-Path $f), [System.Text.Encoding]::UTF8)

        # 1. Remove page-level bottom dark gradient
        $content = [System.Text.RegularExpressions.Regex]::Replace($content, '(?s)\{/\* Degradado oscuro inferior.*?bg-gradient-to-t from-black/90 via-black/40 to-transparent pointer-events-none" />', '{/* Fondo 100% despejado y luminoso */}')

        # 2. Remove Card 1 IMG and gradient
        $content = [System.Text.RegularExpressions.Regex]::Replace($content, '(?s)<img\s+src=\{IMG_CARD_NUEVO\}.*?<div className="absolute inset-0 bg-gradient-to-t[^>]*/>\s*', '')

        # 3. Remove Card 2 IMG and gradient
        $content = [System.Text.RegularExpressions.Regex]::Replace($content, '(?s)<img\s+src=\{IMG_CARD_VISITA\}.*?<div className="absolute inset-0 bg-gradient-to-t[^>]*/>\s*', '')

        # 4. Remove Card 3 IMG and gradient
        $content = [System.Text.RegularExpressions.Regex]::Replace($content, '(?s)<img\s+src=\{IMG_CARD_HUELLA\}.*?<div className="absolute inset-0 bg-gradient-to-t[^>]*/>\s*', '')

        # 5. Strengthen text drop shadow
        $content = $content.Replace('drop-shadow-[0_2px_8px_rgba(0,0,0,0.95)]', 'drop-shadow-[0_2px_12px_rgba(0,0,0,1)]')

        [System.IO.File]::WriteAllText((Resolve-Path $f), $content, [System.Text.Encoding]::UTF8)
        Write-Host "Updated: $f"
    }
}
