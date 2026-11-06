const fs = require('fs');

const path = 'c:/Users/andre/OneDrive/Desktop/gym/LEGENDS/src/App.tsx';
const content = fs.readFileSync(path, 'utf8');

const lines = content.split('\n');
console.log(lines.slice(120, 210).join('\n'));
