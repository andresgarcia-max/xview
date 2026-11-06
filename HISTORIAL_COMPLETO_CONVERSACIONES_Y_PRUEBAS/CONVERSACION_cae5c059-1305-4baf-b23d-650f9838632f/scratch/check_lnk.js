const fs = require('fs');
const { execSync } = require('child_process');

try {
  const out = execSync(`powershell -NoProfile -Command "& { $w = New-Object -ComObject WScript.Shell; Get-ChildItem -Filter '*.lnk' | ForEach-Object { $_.Name + ' -> ' + $w.CreateShortcut($_.FullName).TargetPath + ' ' + $w.CreateShortcut($_.FullName).Arguments } }"`).toString();
  console.log(out);
} catch (e) {
  console.error(e.message);
}
