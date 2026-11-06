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
        $newContent = $content.Replace('relative z-10 p-3.5 rounded-2xl bg-black/40 backdrop-blur-md border border-white/10 group-hover:border-amber-400/40 transition-colors', 'relative z-10')
        $newContent = $newContent.Replace('relative z-10 p-3.5 rounded-2xl bg-black/40 backdrop-blur-md border border-white/10 group-hover:border-emerald-400/40 transition-colors', 'relative z-10')
        $newContent = $newContent.Replace('bg-gradient-to-t from-black/90 via-black/25 to-black/20 pointer-events-none', 'bg-gradient-to-t from-black/70 via-black/20 to-transparent pointer-events-none')
        if ($newContent -ne $content) {
            [System.IO.File]::WriteAllText((Resolve-Path $f), $newContent, [System.Text.Encoding]::UTF8)
            Write-Host "Updated: $f"
        } else {
            Write-Host "No changes in: $f"
        }
    }
}
