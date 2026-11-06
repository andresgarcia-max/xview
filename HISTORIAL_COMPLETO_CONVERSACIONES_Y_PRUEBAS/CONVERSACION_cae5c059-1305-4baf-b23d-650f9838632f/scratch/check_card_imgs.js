const fs = require('fs');

const path = 'c:/Users/andre/OneDrive/Desktop/gym/LEGENDS/src/components/access/IndependentTerminalApp.tsx';
const content = fs.readFileSync(path, 'utf8');

const lines = content.split('\n');
for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes('cardClienteNuevo') || lines[i].includes('cardVisita') || lines[i].includes('cardHuella')) {
    console.log(`Line ${i+1}: ${lines[i]}`);
    console.log(lines.slice(Math.max(0, i-2), Math.min(lines.length, i+15)).join('\n'));
    console.log('---');
  }
}
