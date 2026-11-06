const fs = require('fs');
const path = require('path');

const file = path.resolve('LEGENDS/src/components/access/TurnstileTerminal.tsx');
let code = fs.readFileSync(file, 'utf8');

// 1. Update state
const oldState = `  const [bgImage, setBgImage] = useState<string>(() => {
    if (typeof window !== 'undefined' && localStorage.getItem('legends_terminal_bg_version') !== 'v9_zoom_caminadoras_8k') {
      localStorage.setItem('legends_terminal_bg_version', 'v9_zoom_caminadoras_8k');
      localStorage.setItem('legends_terminal_bg', gymBg);
      return gymBg;
    }
    return localStorage.getItem('legends_terminal_bg') || gymBg;
  });`;

const newState = `  const [bgImage, setBgImage] = useState<string>(() => {
    if (typeof window !== 'undefined') {
      try {
        const ver = localStorage.getItem('legends_turnstile_bg_version');
        if (ver !== 'v12_bulletproof_gym_bg') {
          localStorage.setItem('legends_turnstile_bg_version', 'v12_bulletproof_gym_bg');
          localStorage.removeItem('legends_terminal_bg');
          return gymBg;
        }
      } catch {}
    }
    return gymBg;
  });`;

code = code.replace(oldState, newState);

// 2. Add onError fallback to img tag
const oldImg = `<img
          src={bgImage}
          alt="Fitness Legends Gym Background"
          style={{
            filter: 'brightness(1.04) contrast(1.06) saturate(1.06)',
            objectPosition: 'center center',
          }}
          className="w-full h-full object-cover object-center"
        />`;

const newImg = `<img
          src={bgImage || gymBg}
          alt="Fitness Legends Gym Background"
          onError={(e) => {
            const target = e.currentTarget;
            if (target.src !== gymBg) {
              target.src = gymBg;
            } else {
              target.src = './gym-background.jpg';
            }
          }}
          style={{
            filter: 'brightness(1.04) contrast(1.06) saturate(1.06)',
            objectPosition: 'center center',
          }}
          className="w-full h-full object-cover object-center"
        />`;

code = code.replace(oldImg, newImg);

fs.writeFileSync(file, code, 'utf8');
console.log('✓ Successfully updated TurnstileTerminal.tsx');
