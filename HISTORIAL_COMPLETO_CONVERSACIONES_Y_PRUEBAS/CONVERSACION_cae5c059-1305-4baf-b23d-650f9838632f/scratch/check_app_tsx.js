const fs = require('fs');

const path = 'c:/Users/andre/OneDrive/Desktop/gym/LEGENDS/src/App.tsx';
const content = fs.readFileSync(path, 'utf8');

const lines = content.split('\n');
for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes('IndependentTerminalApp') || lines[i].includes('terminalView')) {
    console.log(lines.slice(Math.max(0, i - 5), Math.min(lines.length, i + 25)).join('\n'));
    break;
  }
}
