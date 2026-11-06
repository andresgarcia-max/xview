const { execSync } = require('child_process');
const git = 'C:/Users/andre/AppData/Local/Programs/Git/bin/git.exe';
const app = execSync(`"${git}" -C LEGENDS show 2032872:src/components/access/IndependentTerminalApp.tsx`, { encoding: 'utf8' });

const lines = app.split('\n');
console.log(lines.slice(670, 715).join('\n'));
