const fs = require('fs');
const files = [
  'ACCESO_WEB_FITNESS_LEGENDS.html',
  'Control_De_Acceso_Web.html',
  'LEGENDS/public/terminal.html',
  'LEGENDS/public/Control_De_Acceso_Web.html'
];

for (const f of files) {
  if (fs.existsSync(f)) {
    let content = fs.readFileSync(f, 'utf8');
    const oldTag = '<img \n              src={selectedBg}\n              alt="Fondo Gym Legends"';
    const newTag = '<img \n              src={selectedBg}\n              onError={(e) => { e.currentTarget.src = "gym-background.jpg"; }}\n              alt="Fondo Gym Legends"';
    if (content.includes(oldTag)) {
      content = content.replace(oldTag, newTag);
      fs.writeFileSync(f, content, 'utf8');
      console.log(`✓ Added fallback to ${f}`);
    }
  }
}
