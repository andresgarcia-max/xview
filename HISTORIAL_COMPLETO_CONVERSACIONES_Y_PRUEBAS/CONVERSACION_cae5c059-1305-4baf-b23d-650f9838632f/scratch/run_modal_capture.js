const { spawn } = require('child_process');
const path = require('path');

const electronExe = path.resolve('LEGENDS/node_modules/electron/dist/electron.exe');
const script = path.resolve('C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/scratch/capture_modal.js');

const cleanEnv = { ...process.env };
delete cleanEnv.ELECTRON_RUN_AS_NODE;

const child = spawn(electronExe, [script], {
  env: cleanEnv,
  stdio: 'inherit'
});

child.on('exit', (code) => {
  console.log('Modal capture script exited with:', code);
});
