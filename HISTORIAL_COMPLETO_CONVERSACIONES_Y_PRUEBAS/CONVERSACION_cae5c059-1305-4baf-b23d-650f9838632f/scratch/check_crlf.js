const fs = require('fs');
const filePath = 'LEGENDS/src/components/access/IndependentTerminalApp.tsx';
const content = fs.readFileSync(filePath, 'utf8');
const idx = content.indexOf('RotateCw');
console.log('Line endings:', JSON.stringify(content.slice(idx - 20, idx + 40)));
