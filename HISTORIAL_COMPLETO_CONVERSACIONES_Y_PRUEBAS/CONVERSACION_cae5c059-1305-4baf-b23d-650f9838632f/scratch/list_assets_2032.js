const { execSync } = require('child_process');
const fs = require('fs');
const git = 'C:/Users/andre/AppData/Local/Programs/Git/bin/git.exe';

const files = execSync(`"${git}" -C LEGENDS ls-tree 2032872 src/assets/`, { encoding: 'utf8' });
console.log(files);
