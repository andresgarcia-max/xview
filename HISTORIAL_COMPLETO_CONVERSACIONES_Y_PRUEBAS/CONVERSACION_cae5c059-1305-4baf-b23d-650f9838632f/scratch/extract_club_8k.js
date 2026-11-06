const { execSync } = require('child_process');
const fs = require('fs');
const git = 'C:/Users/andre/AppData/Local/Programs/Git/bin/git.exe';

try {
  const buf = execSync(`"${git}" -C LEGENDS show fc04616:public/gym-background-club-8k.jpg`);
  fs.writeFileSync('C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/scratch/bg_club_8k.jpg', buf);
  console.log('Saved bg_club_8k.jpg');
} catch (e) {
  console.log('Error:', e.message);
}
