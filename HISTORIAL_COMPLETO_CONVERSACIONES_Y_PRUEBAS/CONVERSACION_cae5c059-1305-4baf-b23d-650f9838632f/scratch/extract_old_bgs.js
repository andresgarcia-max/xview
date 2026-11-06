const { execSync } = require('child_process');
const fs = require('fs');
const git = 'C:/Users/andre/AppData/Local/Programs/Git/bin/git.exe';

const buf1 = execSync(`"${git}" -C LEGENDS show 9696c8e~1:src/assets/gym-background.jpg`);
fs.writeFileSync('C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/scratch/bg_before_9696c8e.jpg', buf1);

const buf2 = execSync(`"${git}" -C LEGENDS show 9696c8e~1:src/assets/gym-background-4k.jpg`);
fs.writeFileSync('C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/scratch/bg_4k_before_9696c8e.jpg', buf2);

console.log('Saved images before commit 9696c8e');
