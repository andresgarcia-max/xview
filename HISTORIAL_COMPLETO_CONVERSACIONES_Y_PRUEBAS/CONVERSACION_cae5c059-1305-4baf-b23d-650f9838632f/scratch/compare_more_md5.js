const fs = require('fs');
const crypto = require('crypto');

function md5(path) {
  try {
    return crypto.createHash('md5').update(fs.readFileSync(path)).digest('hex');
  } catch (e) {
    return 'NOT_FOUND';
  }
}

console.log('motto_34b1832.jpg: ', md5('C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/scratch/motto_34b1832.jpg'));
console.log('repo_bg2.jpg:      ', md5('C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/scratch/repo_bg2.jpg'));
console.log('WhatsApp ...24 AM: ', md5('c:/Users/andre/OneDrive/Desktop/gym/LEGENDS/src/assets/WhatsApp Image 2026-10-19 at 8.32.24 AM.jpeg'));
console.log('bg_2032872.jpg:    ', md5('C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/scratch/bg_2032872.jpg'));
console.log('bg_4k_before_9696: ', md5('C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/scratch/bg_4k_before_9696c8e.jpg'));
