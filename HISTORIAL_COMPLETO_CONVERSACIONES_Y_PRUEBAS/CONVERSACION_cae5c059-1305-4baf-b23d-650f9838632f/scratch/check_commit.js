const { execSync } = require('child_process');
const git = 'C:/Users/andre/AppData/Local/Programs/Git/bin/git.exe';
const log = execSync(`"${git}" -C LEGENDS show --stat 9696c8e`, { encoding: 'utf8' });
console.log(log);
