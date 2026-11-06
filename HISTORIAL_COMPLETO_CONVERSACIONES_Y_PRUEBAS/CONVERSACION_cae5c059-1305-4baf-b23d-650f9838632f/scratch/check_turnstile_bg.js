const fs = require('fs');

const path = 'c:/Users/andre/OneDrive/Desktop/gym/LEGENDS/src/components/access/TurnstileTerminal.tsx';
const content = fs.readFileSync(path, 'utf8');

const lines = content.split('\n');
console.log(lines.slice(20, 65).join('\n'));
