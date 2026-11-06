const fs = require('fs');
['LEGENDS/index.html', 'LEGENDS/dist/index.html', 'LEGENDS/release/FITNESS_LEGENDS_TERMINAL-win32-x64/resources/app/dist/index.html'].forEach(p => {
  if (fs.existsSync(p)) {
    let html = fs.readFileSync(p, 'utf8');
    html = html.replace(/<title>.*?<\/title>/, '<title>LEYENDS FITNESS GYM • Sistema ERP & Control Biométrico</title>');
    fs.writeFileSync(p, html, 'utf8');
    console.log('Fixed title in', p);
  }
});
