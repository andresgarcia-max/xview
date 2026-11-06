$file = "LEGENDS\src\components\access\IndependentTerminalApp.tsx"
$content = [System.IO.File]::ReadAllText((Resolve-Path $file), [System.Text.Encoding]::UTF8)

# Remove img and gradient in card 1
$content = [System.Text.RegularExpressions.Regex]::Replace($content, '(?s)<img\s+src=\{cardClienteNuevo\}.*?<div className="absolute inset-0 bg-gradient-to-t[^>]*/>\s*', '')
# Remove img and gradient in card 2
$content = [System.Text.RegularExpressions.Regex]::Replace($content, '(?s)<img\s+src=\{cardVisita\}.*?<div className="absolute inset-0 bg-gradient-to-t[^>]*/>\s*', '')
# Remove img and gradient in card 3
$content = [System.Text.RegularExpressions.Regex]::Replace($content, '(?s)<img\s+src=\{cardHuella\}.*?<div className="absolute inset-0 bg-gradient-to-t[^>]*/>\s*', '')

# Also remove page-level bottom dark gradient if still present
$content = [System.Text.RegularExpressions.Regex]::Replace($content, '<div className="absolute inset-0 bg-gradient-to-t from-black/90[^>]*/>\s*', '')

[System.IO.File]::WriteAllText((Resolve-Path $file), $content, [System.Text.Encoding]::UTF8)
Write-Host "Replaced via regex in $file"
