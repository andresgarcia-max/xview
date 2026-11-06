const { spawn } = require('child_process');
const path = require('path');

const exe = path.resolve('LEGENDS/release/FITNESS_LEGENDS_TERMINAL-win32-x64/ControlDeAccesoLegends.exe');
console.log('Spawning:', exe);

const child = spawn(exe, ['--terminal'], {
  env: { ...process.env, ELECTRON_ENABLE_LOGGING: '1' },
  stdio: 'pipe'
});

child.stdout.on('data', d => console.log('STDOUT:', d.toString()));
child.stderr.on('data', d => console.error('STDERR:', d.toString()));

child.on('exit', (code, sig) => {
  console.log('Exited with code:', code, 'signal:', sig);
});

setTimeout(() => {
  console.log('Still running after 5s? PID:', child.pid);
  child.kill();
}, 5000);
