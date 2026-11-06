const fs = require('fs');

const path = 'c:/Users/andre/OneDrive/Desktop/gym/repositorios/legends-gym/public/Control_De_Acceso_Web.html';
const content = fs.readFileSync(path, 'utf8');

const lines = content.split('\n');
lines.forEach((l, i) => {
  if (l.includes('CLIENTE NUEVO') || l.includes('QUIERO VISITA') || l.includes('YA TENGO MEMBRES')) {
    console.log(`Line ${i+1}: ${l}`);
    console.log(lines.slice(Math.max(0, i-5), Math.min(lines.length, i+15)).join('\n'));
    console.log('---');
  }
});
