const fs = require('fs');

const path = 'C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/.system_generated/logs/transcript.jsonl';
if (!fs.existsSync(path)) {
  console.log('File does not exist');
} else {
  const lines = fs.readFileSync(path, 'utf8').trim().split('\n');
  console.log('Total transcript lines:', lines.length);
  for (let i = 0; i < lines.length; i++) {
    const l = lines[i];
    if (l.includes('media_1792857125465_bc381733') || l.includes('fondo') || l.includes('imagen')) {
      try {
        const o = JSON.parse(l);
        if (o.type === 'USER_INPUT' || o.source === 'USER_EXPLICIT') {
          console.log(`Step ${o.step_index}: [${o.type}] Content:`, o.content);
        }
      } catch (e) {}
    }
  }
}
