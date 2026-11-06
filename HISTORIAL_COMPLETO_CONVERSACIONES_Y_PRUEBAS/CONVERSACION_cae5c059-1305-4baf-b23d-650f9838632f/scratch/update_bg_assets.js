const fs = require('fs');
const path = require('path');

const srcDir = 'c:/Users/andre/OneDrive/Desktop/gym/LEGENDS/src/assets';
const pubDir = 'c:/Users/andre/OneDrive/Desktop/gym/LEGENDS/public';
const distDir = 'c:/Users/andre/OneDrive/Desktop/gym/LEGENDS/dist';
const releaseAppDist = 'c:/Users/andre/OneDrive/Desktop/gym/LEGENDS/release/FITNESS_LEGENDS_TERMINAL-win32-x64/resources/app/dist';
const releaseAppPub = 'c:/Users/andre/OneDrive/Desktop/gym/LEGENDS/release/FITNESS_LEGENDS_TERMINAL-win32-x64/resources/app/public';
const rootDir = 'c:/Users/andre/OneDrive/Desktop/gym';

const bgOfficial16x9 = 'C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/scratch/bg_before_9696c8e.jpg';
const bgRealPhoto = 'C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/scratch/bg_2032872.jpg';
const bgMottoPhoto = 'C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/scratch/motto_34b1832.jpg';

function copyFileSafe(src, dst) {
  try {
    fs.mkdirSync(path.dirname(dst), { recursive: true });
    fs.copyFileSync(src, dst);
    console.log(`Copied to: ${dst}`);
  } catch (e) {
    console.error(`Failed copying to ${dst}:`, e.message);
  }
}

// 1. gym-background.jpg is the official 16:9 wall with "LEYENDS GYM CLUB FITNESS" and machines
const targetsMain = [
  path.join(srcDir, 'gym-background.jpg'),
  path.join(pubDir, 'gym-background.jpg'),
  path.join(distDir, 'gym-background.jpg'),
  path.join(releaseAppDist, 'gym-background.jpg'),
  path.join(releaseAppPub, 'gym-background.jpg'),
  path.join(rootDir, 'gym-background.jpg'),
  path.join(rootDir, 'gym-background-4k.jpg'),
  path.join(rootDir, 'gym-background-club-8k.jpg'),
  path.join(srcDir, 'gym-background-4k.jpg'),
  path.join(pubDir, 'gym-background-4k.jpg')
];
targetsMain.forEach(t => copyFileSafe(bgOfficial16x9, t));

// 2. gym-background-real.jpg is the real photo of the gym
const targetsReal = [
  path.join(srcDir, 'gym-background-real.jpg'),
  path.join(pubDir, 'gym-background-real.jpg'),
  path.join(distDir, 'gym-background-real.jpg'),
  path.join(releaseAppDist, 'gym-background-real.jpg'),
  path.join(releaseAppPub, 'gym-background-real.jpg'),
  path.join(rootDir, 'gym-background-real.jpg')
];
targetsReal.forEach(t => copyFileSafe(bgRealPhoto, t));

// 3. gym-background-motto.jpg is "TU UNICO LIMITE ERES TU"
const targetsMotto = [
  path.join(srcDir, 'gym-background-motto.jpg'),
  path.join(pubDir, 'gym-background-motto.jpg'),
  path.join(distDir, 'gym-background-motto.jpg'),
  path.join(releaseAppDist, 'gym-background-motto.jpg'),
  path.join(releaseAppPub, 'gym-background-motto.jpg'),
  path.join(rootDir, 'gym-background-motto.jpg')
];
targetsMotto.forEach(t => copyFileSafe(bgMottoPhoto, t));

console.log('Finished updating gym background assets.');
