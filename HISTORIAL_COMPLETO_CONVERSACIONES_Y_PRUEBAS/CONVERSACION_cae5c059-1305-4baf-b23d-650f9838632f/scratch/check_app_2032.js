const { execSync } = require('child_process');
const git = 'C:/Users/andre/AppData/Local/Programs/Git/bin/git.exe';
const app = execSync(`"${git}" -C LEGENDS show 2032872:src/components/access/IndependentTerminalApp.tsx`, { encoding: 'utf8' });

// Look for image handling and styling
const lines = app.split('\n');
lines.forEach((l, i) => {
  if (l.includes('gymBg') || l.includes('img') || l.includes('Background') || l.includes('object-cover')) {
    console.log(`Line ${i+1}: ${l}`);
  }
});
