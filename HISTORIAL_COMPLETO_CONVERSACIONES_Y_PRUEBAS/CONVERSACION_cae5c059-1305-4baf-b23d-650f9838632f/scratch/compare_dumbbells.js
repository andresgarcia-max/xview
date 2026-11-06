const fs = require('fs');

const p1 = 'c:/Users/andre/OneDrive/Desktop/gym/LEGENDS/src/assets/modal-gym-bg.jpg';
const p2 = 'c:/Users/andre/OneDrive/Desktop/gym/LEGENDS/src/assets/gym-dumbbell-background.jpg';

console.log('modal-gym-bg.jpg size:', fs.statSync(p1).size);
console.log('gym-dumbbell-background.jpg size:', fs.statSync(p2).size);
