const fs = require('fs');
const jsPath = 'LEGENDS/dist/assets/index-aHGM4Amh.js';
const content = fs.readFileSync(jsPath, 'utf8');

console.log('Includes gym-background-C2ZRMS71.jpg:', content.includes('gym-background-C2ZRMS71.jpg'));
console.log('Includes legends_terminal_bg_ver:', content.includes('legends_terminal_bg_ver'));
console.log('Includes v12_bulletproof_original_bg:', content.includes('v12_bulletproof_original_bg'));
console.log('Includes UserPlus:', content.includes('Inscripción'));
console.log('Includes Pase del Día:', content.includes('Pase del Día'));
console.log('Includes Ya Tengo Membresía:', content.includes('Ya Tengo Membresía'));
