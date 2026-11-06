const fs = require('fs');

const path = 'c:/Users/andre/OneDrive/Desktop/gym/LEGENDS/src/components/access/IndependentTerminalApp.tsx';
const content = fs.readFileSync(path, 'utf8');

const lines = content.split('\n');
for (let i = 740; i < 790; i++) {
  if (lines[i].includes('Real Gym Background Photo')) {
    console.log(lines.slice(i, i + 30).join('\n'));
    break;
  }
}
