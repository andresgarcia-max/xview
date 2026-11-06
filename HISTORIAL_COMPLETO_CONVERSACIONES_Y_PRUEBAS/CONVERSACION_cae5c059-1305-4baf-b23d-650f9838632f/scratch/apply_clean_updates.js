const fs = require('fs');
const path = require('path');

const file = path.resolve('LEGENDS/src/components/access/IndependentTerminalApp.tsx');
let code = fs.readFileSync(file, 'utf8');

// 1. Add icons to lucide-react import
code = code.replace(
  /BicepsFlexed\r?\n\} from 'lucide-react';/,
  `BicepsFlexed,\r\n  RotateCw,\r\n  UserPlus,\r\n  Ticket,\r\n  QrCode\r\n} from 'lucide-react';`
);

// 2. Replace bgImage state
const oldStateBlock = `  const [bgImage, setBgImage] = useState<string>(() => {
    if (typeof window !== 'undefined' && localStorage.getItem('legends_terminal_bg_version') !== 'v9_zoom_caminadoras_8k') {
      localStorage.setItem('legends_terminal_bg_version', 'v9_zoom_caminadoras_8k');
      localStorage.setItem('legends_terminal_bg', gymBg);
      return gymBg;
    }
    return localStorage.getItem('legends_terminal_bg') || gymBg;
  });`;

const newStateBlock = `  // Bulletproof gym background wallpapers with multi-tier fallbacks
  const BG_OPTIONS = [
    { id: 'gymBg', name: 'Gimnasio Legends (Pared Neón & Caminadoras)', primary: gymBg, fallback: './gym-background.jpg' },
    { id: 'gymBgMotto', name: 'Mural Motivacional Legends', primary: gymBgMotto, fallback: './gym-background-motto.jpg' },
    { id: 'modalGymBg', name: 'Mancuernas Pro Dark', primary: modalGymBg, fallback: './modal-gym-bg.jpg' }
  ];

  const [bgIndex, setBgIndex] = useState<number>(() => {
    if (typeof window !== 'undefined') {
      try {
        const ver = localStorage.getItem('legends_terminal_bg_ver');
        if (ver !== 'v12_bulletproof_original_bg') {
          localStorage.setItem('legends_terminal_bg_ver', 'v12_bulletproof_original_bg');
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

code = code.replace(oldStateBlock, newStateBlock);

// 3. Replace toggleBackground
const oldToggle = `  const toggleBackground = () => {
    const nextBg = bgImage === gymBg ? gymBgMotto : (bgImage === gymBgMotto ? modalGymBg : gymBg);
    setBgImage(nextBg);
    if (typeof window !== 'undefined') {
      localStorage.setItem('legends_terminal_bg', nextBg);
    }
  };`;

const newToggle = `  const toggleBackground = () => {
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

code = code.replace(oldToggle, newToggle);

// 4. Replace background img tag
const oldImgTag = `      {/* Real Gym Background Photo */}
      <div className="fixed inset-0 pointer-events-none z-0 overflow-hidden">
        <img
          src={bgImage}
          alt="Fitness Legends Gym Background"
          style={{
            filter: 'brightness(1.05) contrast(1.08) saturate(1.06)',
            objectPosition: 'center 22%',
          }}
          className="w-full h-full object-cover"
        />`;

const newImgTag = `      {/* Real Gym Background Photo */}
      <div className="fixed inset-0 pointer-events-none z-0 overflow-hidden">
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
            filter: 'brightness(1.06) contrast(1.08) saturate(1.06)',
            objectPosition: bgIndex === 0 ? 'center 35%' : 'center center',
          }}
          className="w-full h-full object-cover"
        />`;

code = code.replace(oldImgTag, newImgTag);

// 5. Replace Card 1 Badge
code = code.replace(
  /<span>💪<\/span>\s*<span className="tracking-wider">Inscripción<\/span>/,
  `<UserPlus className="w-3.5 h-3.5 text-amber-400" />\r\n              <span className="tracking-wider">Inscripción</span>`
);

// 6. Replace Card 2 Badge
code = code.replace(
  /<span>🎟️<\/span>\s*<span className="tracking-wider">Pase del Día<\/span>/,
  `<Ticket className="w-3.5 h-3.5 text-emerald-400" />\r\n              <span className="tracking-wider">Pase del Día</span>`
);

// 7. Replace Card 3 Badge & Button
code = code.replace(
  /<span>👆<\/span>\s*<span className="tracking-wider">Acceso Directo<\/span>/,
  `<Zap className="w-3.5 h-3.5 text-amber-400" />\r\n              <span className="tracking-wider">Acceso Directo</span>`
);

code = code.replace(
  /<span className="w-8 h-8 rounded-full bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-white flex items-center justify-center font-black text-base shadow-lg group-hover:scale-110 transition-transform">[\s\S]*?<\/span>\s*<\/div>\s*<\/div>\s*<\/div>\s*<\/div>/,
  `<span className="w-8 h-8 rounded-full bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-white flex items-center justify-center font-black text-base shadow-lg group-hover:scale-110 transition-transform">\r\n                <Fingerprint className="w-4 h-4 text-white" />\r\n              </span>\r\n            </div>\r\n          </div>\r\n        </div>\r\n      </div>`
);

// 8. Add modalGymBg fallback
code = code.replace(
  /src=\{modalGymBg\}\s+alt="Fondo Mancuernas Fitness"/g,
  `src={modalGymBg} onError={(e) => { (e.currentTarget as HTMLImageElement).src = './modal-gym-bg.jpg'; }} alt="Fondo Mancuernas Fitness"`
);

fs.writeFileSync(file, code, 'utf8');
console.log('✓ Applied surgical updates to IndependentTerminalApp.tsx');
