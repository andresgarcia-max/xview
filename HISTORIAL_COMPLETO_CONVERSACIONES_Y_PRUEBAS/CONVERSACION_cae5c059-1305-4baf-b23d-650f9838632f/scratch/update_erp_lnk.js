const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const rootDir = path.resolve('.');
const targetExe = path.join(rootDir, 'LEGENDS', 'release', 'FITNESS_LEGENDS_TERMINAL-win32-x64', 'FitnessLegends.exe');
const lnkPath = path.join(rootDir, 'FITNESS LEGENDS - Sistema ERP.lnk');

const ps = `
$sh = New-Object -ComObject WScript.Shell;
$s = $sh.CreateShortcut('${lnkPath.replace(/'/g, "''")}');
$s.TargetPath = '${targetExe.replace(/'/g, "''")}';
$s.Arguments = '--main';
$s.WorkingDirectory = '${path.dirname(targetExe).replace(/'/g, "''")}';
$s.Description = 'Fitness Legends Gym - Sistema Central ERP';
$s.Save();
`;

execSync(`powershell -NoProfile -Command "${ps.replace(/\r?\n/g, ' ')}"`);
console.log('✓ Updated ERP shortcut');
