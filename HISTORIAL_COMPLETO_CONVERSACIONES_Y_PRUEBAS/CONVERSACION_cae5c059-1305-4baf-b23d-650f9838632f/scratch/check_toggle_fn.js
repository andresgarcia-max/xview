const fs = require('fs');

const path = 'c:/Users/andre/OneDrive/Desktop/gym/LEGENDS/src/components/access/IndependentTerminalApp.tsx';
const content = fs.readFileSync(path, 'utf8');

const lines = content.split('\n');
for (let i = 640; i < 685; i++) {
  if (lines[i].includes('toggleBackground')) {
    console.log(lines.slice(i - 2, i + 20).join('\n'));
    break;
  }
}
