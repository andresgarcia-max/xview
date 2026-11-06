const fs = require('fs');
const jsPath = 'LEGENDS/dist/assets/index-58h3VA_v.js';
const content = fs.readFileSync(jsPath, 'utf8');

const idx = content.indexOf('legends_terminal_bg_version');
console.log('Index of legends_terminal_bg_version:', idx);
if (idx !== -1) {
  console.log('Context:', content.substring(idx - 100, idx + 400));
}



