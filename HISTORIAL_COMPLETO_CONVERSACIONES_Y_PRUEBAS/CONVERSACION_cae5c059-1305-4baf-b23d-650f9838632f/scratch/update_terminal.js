const fs = require('fs');
const path = require('path');

const file = path.resolve('LEGENDS/src/components/access/IndependentTerminalApp.tsx');
let code = fs.readFileSync(file, 'utf8');

// 1. Add UserPlus, Ticket, QrCode to lucide-react imports if not present
if (!code.includes('UserPlus,')) {
  code = code.replace(
    /BicepsFlexed,\s*RotateCw\s*\}/,
    'BicepsFlexed,\n  RotateCw,\n  UserPlus,\n  Ticket,\n  QrCode\n}'
  );
}

// 2. Replace bgImage state definition with BG_OPTIONS & safe index
const oldStateRegex = /const\s+\[bgImage,\s*setBgImage\]\s*=\s*useState<string>\(\(\)\s*=>\s*\{[\s\S]*?return\s+localStorage\.getItem\('legends_terminal_bg'\)\s*\|\|\s*gymBg;\s*\}\);/;

const newStateCode = `// Safe background wallpapers with verified multi-tier fallbacks
  const BG_OPTIONS = [
    { id: 'gymBg', name: 'Gimnasio Legends (Pared Neón & Caminadoras)', primary: gymBg, fallback: './gym-background.jpg' },
    { id: 'gymBgMotto', name: 'Mural Motivacional Legends', primary: gymBgMotto, fallback: './gym-background-motto.jpg' },
    { id: 'modalGymBg', name: 'Mancuernas Pro Dark', primary: modalGymBg, fallback: './modal-gym-bg.jpg' }
  ];

  const [bgIndex, setBgIndex] = useState<number>(() => {
    if (typeof window !== 'undefined') {
      try {
        const ver = localStorage.getItem('legends_terminal_bg_version');
        if (ver !== 'v12_bulletproof_original_bg') {
          localStorage.setItem('legends_terminal_bg_version', 'v12_bulletproof_original_bg');
          localStorage.removeItem('legends_terminal_bg');
          localStorage.setItem('legends_terminal_bg_index', '0');
          return 0;
        }
        const saved = localStorage.getItem('legends_terminal_bg_index');
        if (saved !== null) {
          const parsed = parseInt(saved, 10);
          if (!isNaN(parsed) && parsed >= 0 && parsed < BG_OPTIONS.length) return parsed;
        }
      } catch {}
    }
    return 0;
  });

  const activeBg = BG_OPTIONS[bgIndex] || BG_OPTIONS[0];
  const [currentBgSrc, setCurrentBgSrc] = useState<string>(() => activeBg.primary || activeBg.fallback);

  useEffect(() => {
    const chosen = BG_OPTIONS[bgIndex] || BG_OPTIONS[0];
    setCurrentBgSrc(chosen.primary || chosen.fallback);
  }, [bgIndex]);`;

if (oldStateRegex.test(code)) {
  code = code.replace(oldStateRegex, newStateCode);
  console.log('✓ Replaced bgImage state successfully');
} else {
  console.warn('! Old state regex did not match');
}

// 3. Replace toggleBackground
const oldToggleRegex = /const\s+toggleBackground\s*=\s*\(\)\s*=>\s*\{[\s\S]*?localStorage\.setItem\('legends_terminal_bg',\s*nextBg\);\s*\}\s*\};/;

const newToggleCode = `const toggleBackground = () => {
    setBgIndex(prev => {
      const next = (prev + 1) % BG_OPTIONS.length;
      if (typeof window !== 'undefined') {
        try {
          localStorage.setItem('legends_terminal_bg_index', String(next));
          localStorage.removeItem('legends_terminal_bg');
        } catch {}
      }
      return next;
    });
  };`;

if (oldToggleRegex.test(code)) {
  code = code.replace(oldToggleRegex, newToggleCode);
  console.log('✓ Replaced toggleBackground successfully');
} else {
  console.warn('! Old toggle regex did not match');
}

// 4. Replace background img tag and wrapper
const oldImgRegex = /<div className="fixed inset-0 pointer-events-none z-0 overflow-hidden">\s*<img\s+src=\{bgImage\}[\s\S]*?className="w-full h-full object-cover"\s*\/>/;

const newImgCode = `<div className="fixed inset-0 pointer-events-none z-0 overflow-hidden">
        <img
          key={currentBgSrc}
          src={currentBgSrc}
          alt="Fitness Legends Gym Background"
          onError={() => {
            const chosen = BG_OPTIONS[bgIndex] || BG_OPTIONS[0];
            if (currentBgSrc !== chosen.fallback) {
              setCurrentBgSrc(chosen.fallback);
            } else if (currentBgSrc !== './gym-background.jpg') {
              setCurrentBgSrc('./gym-background.jpg');
            }
          }}
          style={{
            filter: 'brightness(1.06) contrast(1.06) saturate(1.06)',
            objectPosition: bgIndex === 0 ? 'center 38%' : 'center center',
          }}
          className="w-full h-full object-cover"
        />`;

if (oldImgRegex.test(code)) {
  code = code.replace(oldImgRegex, newImgCode);
  console.log('✓ Replaced terminal background img tag successfully');
} else {
  console.warn('! Old img regex did not match');
}

// 5. Replace card 1 badge: ?? InscripciÃ³n
code = code.replace(
  /<div className="relative z-10 self-end px-3\.5 py-1 rounded-full bg-black\/75 backdrop-blur-md border border-amber-400\/80 flex items-center gap-1\.5 text-white font-black text-xs uppercase tracking-wider shadow-lg drop-shadow-\[0_1px_2px_rgba\(0,0,0,0\.9\)\]">\s*<span>\?\?<\/span>\s*<span className="tracking-wider">[^<]*<\/span>\s*<\/div>/,
  `<div className="relative z-10 self-end px-3.5 py-1 rounded-full bg-black/75 backdrop-blur-md border border-amber-400/80 flex items-center gap-1.5 text-white font-black text-xs uppercase tracking-wider shadow-lg drop-shadow-[0_1px_2px_rgba(0,0,0,0.9)]">
              <UserPlus className="w-3.5 h-3.5 text-amber-400" />
              <span className="tracking-wider">Inscripción</span>
            </div>`
);

// 6. Replace card 2 badge: ??? Pase del DÃ­a
code = code.replace(
  /<div className="relative z-10 self-end px-3\.5 py-1 rounded-full bg-black\/75 backdrop-blur-md border border-emerald-400\/80 flex items-center gap-1\.5 text-white font-black text-xs uppercase tracking-wider shadow-lg drop-shadow-\[0_1px_2px_rgba\(0,0,0,0\.9\)\]">\s*<span>\?\?\?<\/span>\s*<span className="tracking-wider">[^<]*<\/span>\s*<\/div>/,
  `<div className="relative z-10 self-end px-3.5 py-1 rounded-full bg-black/75 backdrop-blur-md border border-emerald-400/80 flex items-center gap-1.5 text-white font-black text-xs uppercase tracking-wider shadow-lg drop-shadow-[0_1px_2px_rgba(0,0,0,0.9)]">
              <Ticket className="w-3.5 h-3.5 text-emerald-400" />
              <span className="tracking-wider">Pase del Día</span>
            </div>`
);

// Card 2 circle icon:
code = code.replace(
  /<span className="w-8 h-8 rounded-full bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-400 hover:to-emerald-500 text-white flex items-center justify-center font-black text-base shadow-lg group-hover:scale-110 transition-transform">\s*\?\s*<\/span>/,
  `<span className="w-8 h-8 rounded-full bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-400 hover:to-emerald-500 text-white flex items-center justify-center font-black text-base shadow-lg group-hover:scale-110 transition-transform">
                  &rarr;
                </span>`
);

// 7. Replace card 3 badge: ?? Acceso Directo
code = code.replace(
  /<div className="relative z-10 self-end px-3\.5 py-1 rounded-full bg-black\/75 backdrop-blur-md border border-amber-400\/80 flex items-center gap-1\.5 text-white font-black text-xs uppercase tracking-wider shadow-lg drop-shadow-\[0_1px_2px_rgba\(0,0,0,0\.9\)\]">\s*<span>\?\?<\/span>\s*<span className="tracking-wider">[^<]*<\/span>\s*<\/div>/,
  `<div className="relative z-10 self-end px-3.5 py-1 rounded-full bg-black/75 backdrop-blur-md border border-amber-400/80 flex items-center gap-1.5 text-white font-black text-xs uppercase tracking-wider shadow-lg drop-shadow-[0_1px_2px_rgba(0,0,0,0.9)]">
              <Zap className="w-3.5 h-3.5 text-amber-400" />
              <span className="tracking-wider">Acceso Directo</span>
            </div>`
);

// Card 3 Title & circle icon:
code = code.replace(/Ya Tengo Membres[^<]*/g, 'Ya Tengo Membresía');
code = code.replace(
  /<span className="w-8 h-8 rounded-full bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-white flex items-center justify-center font-black text-base shadow-lg group-hover:scale-110 transition-transform">\s*\?\?\s*<\/span>/,
  `<span className="w-8 h-8 rounded-full bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-white flex items-center justify-center font-black text-base shadow-lg group-hover:scale-110 transition-transform">
                  <Fingerprint className="w-4 h-4 text-white" />
                </span>`
);

// 8. Add onError fallback to modalGymBg usages
code = code.replace(
  /src=\{modalGymBg\}\s+alt="Fondo Mancuernas Fitness"/g,
  `src={modalGymBg} onError={(e) => { (e.currentTarget as HTMLImageElement).src = './modal-gym-bg.jpg'; }} alt="Fondo Mancuernas Fitness"`
);

// 9. Fix Mojibake throughout file
const replacements = [
  ['Inscripcin', 'Inscripción'],
  ['InscripciÃ³n', 'Inscripción'],
  ['Cumpleaos', 'Cumpleaños'],
  ['CumpleaÃ±os', 'Cumpleaños'],
  ['Opcin', 'Opción'],
  ['OpciÃ³n', 'Opción'],
  ['Estǭndar', 'Estándar'],
  ['EstÃ¡ndar', 'Estándar'],
  ['Membresa', 'Membresía'],
  ['Membres?a', 'Membresía'],
  ['MembresÃ­a', 'Membresía'],
  ['MembresÃ A', 'Membresía'],
  ['dirjase', 'diríjase'],
  ['Da', 'Día'],
  ['DÃ­a', 'Día'],
  ['DÃ A', 'Día'],
  ['Nen', 'Neón'],
  ['NeÃ³n', 'Neón'],
  ['cǭlido', 'cálido'],
  ['ǭmbar', 'ámbar'],
  ['Garca', 'García'],
  ['Electrnico', 'Electrónico'],
  ['TelǸfono', 'Teléfono'],
  ['das', 'días'],
  ['Escǭner', 'Escáner'],
  ['BiomǸtrico', 'Biométrico'],
  ['BIOM%TRICO', 'BIOMÉTRICO'],
  ['ESC?NER', 'ESCÁNER'],
  ['aqu', 'aquí'],
  ['Estacin', 'Estación'],
  ['ptico', 'óptico'],
  ['resolucin', 'resolución'],
  ['?ndice', 'Índice'],
  ['%XITO', 'ÉXITO'],
  ['Seal', 'Señal'],
  ['"ptima', 'Óptima'],
  ['rǭpido', 'rápido'],
  ['Validacin', 'Validación'],
  ['cǸdula', 'cédula'],
  ['?', '•']
];

for (const [bad, good] of replacements) {
  code = code.split(bad).join(good);
}

fs.writeFileSync(file, code, 'utf8');
console.log('✓ Successfully updated IndependentTerminalApp.tsx');
