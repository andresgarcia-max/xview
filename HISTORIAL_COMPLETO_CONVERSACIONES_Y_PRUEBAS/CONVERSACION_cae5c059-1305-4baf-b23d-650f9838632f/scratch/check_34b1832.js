const { execSync } = require('child_process');
const git = 'C:/Users/andre/AppData/Local/Programs/Git/bin/git.exe';

const show = execSync(`"${git}" -C LEGENDS show 34b1832:src/components/access/IndependentTerminalApp.tsx`, { encoding: 'utf8' });
const lines = show.split('\n');
console.log('Total lines:', lines.length);

// Check background and cards
for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes('cardClienteNuevo') || lines[i].includes('IMG_CARD') || lines[i].includes('bgImage') || lines[i].includes('gymBg')) {
    console.log(`L${i+1}: ${lines[i].trim()}`);
  }
}
