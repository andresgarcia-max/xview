$file = "LEGENDS\src\components\access\IndependentTerminalApp.tsx"
$content = [System.IO.File]::ReadAllText((Resolve-Path $file), [System.Text.Encoding]::UTF8)

# 1. Fix git conflict
$conflictRegex = '(?s)style=\{\{\s*<<<<<<< HEAD.*?>>>>>>> origin/main\s*\}\}'
$fixedStyle = "style={{`r`n            filter: 'brightness(1.06) contrast(1.06) saturate(1.06)',`r`n            objectPosition: bgImage === gymBg ? 'center 38%' : 'center center',`r`n          }}"
$content = [System.Text.RegularExpressions.Regex]::Replace($content, $conflictRegex, $fixedStyle)

# 2. Remove bottom black gradient on page
$content = $content.Replace(
    '<div className="absolute inset-0 bg-gradient-to-t from-black/90 via-black/40 to-transparent pointer-events-none" />',
    '{/* Fondo despejado sin oscurecer */}'
)

# 3. Card 1: Remove img and gradient
$card1Old = @'
          {/* Opción 1: CLIENTE NUEVO */}
          <div 
            onClick={() => setShowNewMemberModal(true)}
            className="group relative h-[300px] sm:h-[330px] lg:h-[350px] max-h-[40vh] rounded-3xl overflow-hidden border-2 border-amber-400/60 hover:border-amber-400 transition-all duration-500 shadow-[0_0_25px_rgba(245,158,11,0.25)] hover:shadow-[0_0_45px_rgba(245,158,11,0.55)] flex flex-col justify-between p-5 cursor-pointer transform hover:-translate-y-2 select-none"
          >
            <img 
              src={cardClienteNuevo} 
              alt="Cliente Nuevo Gym" 
              className="absolute inset-0 w-full h-full object-cover object-center group-hover:scale-108 transition-transform duration-700 brightness-95 group-hover:brightness-105"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent pointer-events-none" />
'@

$card1New = @'
          {/* Opción 1: CLIENTE NUEVO */}
          <div 
            onClick={() => setShowNewMemberModal(true)}
            className="group relative h-[300px] sm:h-[330px] lg:h-[350px] max-h-[40vh] rounded-3xl overflow-hidden border-2 border-amber-400/70 hover:border-amber-400 transition-all duration-500 shadow-[0_0_25px_rgba(245,158,11,0.25)] hover:shadow-[0_0_45px_rgba(245,158,11,0.55)] flex flex-col justify-between p-5 cursor-pointer transform hover:-translate-y-2 select-none bg-transparent"
          >
'@

# 4. Card 2: Remove img and gradient
$card2Old = @'
          {/* Opción 2: QUIERO VISITA */}
          <div 
            onClick={() => setShowVisitModal(true)}
            className="group relative h-[300px] sm:h-[330px] lg:h-[350px] max-h-[40vh] rounded-3xl overflow-hidden border-2 border-emerald-400/60 hover:border-emerald-400 transition-all duration-500 shadow-[0_0_25px_rgba(16,185,129,0.25)] hover:shadow-[0_0_45px_rgba(16,185,129,0.55)] flex flex-col justify-between p-5 cursor-pointer transform hover:-translate-y-2 select-none"
          >
            <img 
              src={cardVisita} 
              alt="Pase de Visita Gym" 
              className="absolute inset-0 w-full h-full object-cover object-center group-hover:scale-108 transition-transform duration-700 brightness-95 group-hover:brightness-105"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent pointer-events-none" />
'@

$card2New = @'
          {/* Opción 2: QUIERO VISITA */}
          <div 
            onClick={() => setShowVisitModal(true)}
            className="group relative h-[300px] sm:h-[330px] lg:h-[350px] max-h-[40vh] rounded-3xl overflow-hidden border-2 border-emerald-400/70 hover:border-emerald-400 transition-all duration-500 shadow-[0_0_25px_rgba(16,185,129,0.25)] hover:shadow-[0_0_45px_rgba(16,185,129,0.55)] flex flex-col justify-between p-5 cursor-pointer transform hover:-translate-y-2 select-none bg-transparent"
          >
'@

# 5. Card 3: Remove img and gradient
$card3Old = @'
          {/* Opción 3: YA TENGO MEMBRESÍA (ACCESO DIRECTO) */}
          <div 
            onClick={() => setShowDirectAccessModal(true)}
            className="group relative h-[300px] sm:h-[330px] lg:h-[350px] max-h-[40vh] rounded-3xl overflow-hidden border-2 border-amber-400/70 hover:border-amber-400 transition-all duration-500 shadow-[0_0_25px_rgba(245,158,11,0.3)] hover:shadow-[0_0_50px_rgba(245,158,11,0.6)] flex flex-col justify-between p-5 cursor-pointer transform hover:-translate-y-2 select-none"
          >
            <img 
              src={cardHuella} 
              alt="Lector Biométrico de Huella Gym" 
              className="absolute inset-0 w-full h-full object-cover object-center group-hover:scale-108 transition-transform duration-700 brightness-95 group-hover:brightness-105"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent pointer-events-none" />
'@

$card3New = @'
          {/* Opción 3: YA TENGO MEMBRESÍA (ACCESO DIRECTO) */}
          <div 
            onClick={() => setShowDirectAccessModal(true)}
            className="group relative h-[300px] sm:h-[330px] lg:h-[350px] max-h-[40vh] rounded-3xl overflow-hidden border-2 border-amber-400/70 hover:border-amber-400 transition-all duration-500 shadow-[0_0_25px_rgba(245,158,11,0.3)] hover:shadow-[0_0_50px_rgba(245,158,11,0.6)] flex flex-col justify-between p-5 cursor-pointer transform hover:-translate-y-2 select-none bg-transparent"
          >
'@

# Normalize CRLF for replacement
$content = $content.Replace("`r`n", "`n")
$card1Old = $card1Old.Replace("`r`n", "`n")
$card1New = $card1New.Replace("`r`n", "`n")
$card2Old = $card2Old.Replace("`r`n", "`n")
$card2New = $card2New.Replace("`r`n", "`n")
$card3Old = $card3Old.Replace("`r`n", "`n")
$card3New = $card3New.Replace("`r`n", "`n")

$content = $content.Replace($card1Old, $card1New)
$content = $content.Replace($card2Old, $card2New)
$content = $content.Replace($card3Old, $card3New)

# Enhance drop shadow on text
$content = $content.Replace('drop-shadow-[0_2px_8px_rgba(0,0,0,0.95)]', 'drop-shadow-[0_2px_12px_rgba(0,0,0,1)]')

# Restore CRLF
$content = $content.Replace("`n", "`r`n")
[System.IO.File]::WriteAllText((Resolve-Path $file), $content, [System.Text.Encoding]::UTF8)
Write-Host "Updated IndependentTerminalApp.tsx"
