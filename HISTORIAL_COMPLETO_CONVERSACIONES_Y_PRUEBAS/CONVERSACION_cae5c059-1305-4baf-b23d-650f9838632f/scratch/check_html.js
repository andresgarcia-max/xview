const fs = require('fs');

const path1 = 'c:/Users/andre/OneDrive/Desktop/gym/repositorios/legends-gym/public/Control_De_Acceso_Web.html';
const content = fs.readFileSync(path1, 'utf8');

console.log('Size of file 1:', content.length);
const scripts = content.match(/<script[\s\S]*?<\/script>/gi);
console.log('Number of scripts:', scripts ? scripts.length : 0);

// Search for bg
const lines = content.split('\n');
lines.forEach((l, i) => {
  if (l.includes('BG_') || l.includes('gym-background') || l.includes('Fitness Legends Gym Background') || l.includes('WhatsApp')) {
    console.log(`Line ${i+1}: ${l.substring(0, 150)}`);
  }
});
