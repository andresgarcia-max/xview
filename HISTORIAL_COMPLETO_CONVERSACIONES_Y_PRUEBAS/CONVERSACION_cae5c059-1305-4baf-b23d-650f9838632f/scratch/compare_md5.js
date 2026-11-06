const fs = require('fs');
const crypto = require('crypto');

function md5(path) {
  return crypto.createHash('md5').update(fs.readFileSync(path)).digest('hex');
}

console.log('bg_before_9696c8e.jpg:', md5('C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/scratch/bg_before_9696c8e.jpg'));
console.log('bg_34b1832.jpg:       ', md5('C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/scratch/bg_34b1832.jpg'));
console.log('repo_bg1.jpg:         ', md5('C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/scratch/repo_bg1.jpg'));
