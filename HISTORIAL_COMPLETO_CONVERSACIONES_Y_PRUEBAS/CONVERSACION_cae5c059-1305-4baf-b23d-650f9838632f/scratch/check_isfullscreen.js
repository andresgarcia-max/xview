const fs = require('fs');

const path = 'c:/Users/andre/OneDrive/Desktop/gym/LEGENDS/src/components/access/IndependentTerminalApp.tsx';
const content = fs.readFileSync(path, 'utf8');

const lines = content.split('\n');
lines.forEach((l, i) => {
  if (l.includes('isFullScreen')) {
    console.log(`Line ${i+1}: ${l.trim()}`);
  }
});
