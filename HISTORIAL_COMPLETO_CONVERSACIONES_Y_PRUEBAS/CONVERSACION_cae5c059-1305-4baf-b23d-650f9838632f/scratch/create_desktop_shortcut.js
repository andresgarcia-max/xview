const path = require('path');
const { execSync } = require('child_process');

const targetExe = 'C:\\Users\\andre\\OneDrive\\Desktop\\gym\\CONTROL_DE_ACCESO.exe';
const desktopLnk = 'C:\\Users\\andre\\OneDrive\\Desktop\\CONTROL DE ACCESO - FITNESS LEGENDS.lnk';

const ps = `
$sh = New-Object -ComObject WScript.Shell;
$s = $sh.CreateShortcut('${desktopLnk.replace(/'/g, "''")}');
$s.TargetPath = '${targetExe.replace(/'/g, "''")}';
$s.WorkingDirectory = '${path.dirname(targetExe).replace(/'/g, "''")}';
$s.Description = 'Fitness Legends Gym - Control de Acceso de Clientes';
$s.Save();
`;

execSync(`powershell -NoProfile -Command "${ps.replace(/\r?\n/g, ' ')}"`);
console.log('✓ Created shortcut on Desktop');
