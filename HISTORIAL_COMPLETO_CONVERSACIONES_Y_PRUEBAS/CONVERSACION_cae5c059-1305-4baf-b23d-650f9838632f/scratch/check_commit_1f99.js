const { execSync } = require('child_process');
const git = 'C:/Users/andre/AppData/Local/Programs/Git/bin/git.exe';
const log = execSync(`"${git}" -C LEGENDS show --stat 1f99c7e`, { encoding: 'utf8' });
console.log(log);
