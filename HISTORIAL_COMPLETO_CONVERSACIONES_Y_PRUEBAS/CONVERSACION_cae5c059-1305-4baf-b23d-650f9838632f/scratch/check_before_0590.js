const { execSync } = require('child_process');
const git = 'C:/Users/andre/AppData/Local/Programs/Git/bin/git.exe';

const show = execSync(`"${git}" -C LEGENDS show 0590957~1:src/components/access/IndependentTerminalApp.tsx`, { encoding: 'utf8' });
const lines = show.split('\n');

for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes('Opción 1') || lines[i].includes('Cliente Nuevo')) {
    console.log(lines.slice(i, i + 35).join('\n'));
    break;
  }
}
