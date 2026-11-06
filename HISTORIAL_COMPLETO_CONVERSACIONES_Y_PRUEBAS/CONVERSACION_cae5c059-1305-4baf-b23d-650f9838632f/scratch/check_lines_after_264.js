const fs = require('fs');

const path = 'C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/.system_generated/logs/transcript.jsonl';
const lines = fs.readFileSync(path, 'utf8').trim().split('\n');
let targetLine = -1;
for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes('quiero la imagen de fondo que tenia anteriormente')) {
    targetLine = i;
    break;
  }
}
console.log('Target line index:', targetLine);
for (let i = targetLine; i < Math.min(targetLine + 25, lines.length); i++) {
  const o = JSON.parse(lines[i]);
  console.log(`Line ${i} (step ${o.step_index}) [${o.type}]:`, o.content ? o.content.substring(0, 120) : (o.tool_calls ? o.tool_calls.map(tc => tc.name).join(', ') : 'no content'));
}
