const { execSync } = require('child_process');
const fs = require('fs');
const git = 'C:/Users/andre/AppData/Local/Programs/Git/bin/git.exe';

const buf = execSync(`"${git}" -C LEGENDS show 2032872:src/assets/gym-background.jpg`);
fs.writeFileSync('C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/scratch/bg_2032872.jpg', buf);
console.log('Saved bg_2032872.jpg');
