const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const rootDir = path.resolve('.');
const targetExe = path.join(rootDir, 'ACCESO_CLIENTES_LEGENDS.exe');
const lnkPath = path.join(rootDir, 'FITNESS LEGENDS - Control de Acceso.lnk');

const ps = `
$sh = New-Object -ComObject WScript.Shell;
$s = $sh.CreateShortcut('${lnkPath.replace(/'/g, "''")}');
$s.TargetPath = '${targetExe.replace(/'/g, "''")}';
$s.WorkingDirectory = '${rootDir.replace(/'/g, "''")}';
$s.Description = 'Fitness Legends Gym - Control de Acceso de Clientes';
$s.Save();
`;

execSync(`powershell -NoProfile -Command "${ps.replace(/\r?\n/g, ' ')}"`);
console.log('✓ Updated desktop shortcut to point directly to ACCESO_CLIENTES_LEGENDS.exe');
