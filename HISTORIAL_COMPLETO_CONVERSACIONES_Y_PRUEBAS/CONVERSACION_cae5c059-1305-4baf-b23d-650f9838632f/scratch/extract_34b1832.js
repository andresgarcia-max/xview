const { execSync } = require('child_process');
const fs = require('fs');
const git = 'C:/Users/andre/AppData/Local/Programs/Git/bin/git.exe';

const buf1 = execSync(`"${git}" -C LEGENDS show 34b1832:src/assets/gym-background.jpg`);
fs.writeFileSync('C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/scratch/bg_34b1832.jpg', buf1);

const buf2 = execSync(`"${git}" -C LEGENDS show 34b1832:src/assets/gym-background-motto.jpg`);
fs.writeFileSync('C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/scratch/motto_34b1832.jpg', buf2);

console.log('Saved 34b1832 images');
