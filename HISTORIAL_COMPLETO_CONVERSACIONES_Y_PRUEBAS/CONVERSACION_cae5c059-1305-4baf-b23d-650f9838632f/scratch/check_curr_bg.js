const fs = require('fs');

const path = 'c:/Users/andre/OneDrive/Desktop/gym/LEGENDS/src/assets/gym-background.jpg';
const stat = fs.statSync(path);
console.log('Current gym-background.jpg size:', stat.size);
