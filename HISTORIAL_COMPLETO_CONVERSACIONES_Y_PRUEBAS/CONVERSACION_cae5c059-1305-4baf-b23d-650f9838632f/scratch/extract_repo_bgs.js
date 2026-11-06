const fs = require('fs');

const path = 'c:/Users/andre/OneDrive/Desktop/gym/repositorios/legends-gym/public/Control_De_Acceso_Web.html';
const content = fs.readFileSync(path, 'utf8');

const m1 = content.match(/const\s+BG_PHOTO_1\s*=\s*["']([^"']+)["']/);
const m2 = content.match(/const\s+BG_PHOTO_2\s*=\s*["']([^"']+)["']/);
const m3 = content.match(/const\s+BG_PHOTO_3\s*=\s*["']([^"']+)["']/);

if (m1) {
  const data = m1[1].replace(/^data:image\/jpeg;base64,/, '');
  fs.writeFileSync('C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/scratch/repo_bg1.jpg', Buffer.from(data, 'base64'));
  console.log('Saved repo_bg1.jpg, size:', data.length);
}
if (m2) {
  const data = m2[1].replace(/^data:image\/jpeg;base64,/, '');
  fs.writeFileSync('C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/scratch/repo_bg2.jpg', Buffer.from(data, 'base64'));
  console.log('Saved repo_bg2.jpg, size:', data.length);
}
if (m3) {
  const data = m3[1].replace(/^data:image\/jpeg;base64,/, '');
  fs.writeFileSync('C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/scratch/repo_bg3.jpg', Buffer.from(data, 'base64'));
  console.log('Saved repo_bg3.jpg, size:', data.length);
}
