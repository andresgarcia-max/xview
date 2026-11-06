const fs = require('fs');
const path = require('path');

function getImagesInDir(dir) {
  if (!fs.existsSync(dir)) return [];
  const files = fs.readdirSync(dir, { withFileTypes: true });
  let res = [];
  for (const f of files) {
    if (f.name === 'node_modules' || f.name === '.git' || f.name === 'dist' || f.name === 'release') continue;
    const full = path.join(dir, f.name);
    if (f.isDirectory()) {
      res = res.concat(getImagesInDir(full));
    } else if (/\.(jpg|jpeg|png|webp)$/i.test(f.name)) {
      const stat = fs.statSync(full);
      res.push({ name: f.name, size: stat.size, path: full });
    }
  }
  return res;
}

const list = [
  ...getImagesInDir('.'),
  ...getImagesInDir('LEGENDS/src/assets'),
  ...getImagesInDir('LEGENDS/public'),
  ...getImagesInDir('repositorios/legends-gym/src/assets'),
  ...getImagesInDir('repositorios/legends-gym/public')
];

// De-duplicate by full path
const seen = new Set();
const unique = list.filter(item => {
  if (seen.has(item.path)) return false;
  seen.add(item.path);
  return true;
});

console.log(JSON.stringify(unique, null, 2));
