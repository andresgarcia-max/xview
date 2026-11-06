const fs = require('fs');

const path = 'c:/Users/andre/OneDrive/Desktop/gym/LEGENDS/src/components/access/IndependentTerminalApp.tsx';
const content = fs.readFileSync(path, 'utf8');

const lines = content.split('\n');
console.log(lines.slice(1515, 1605).join('\n'));
