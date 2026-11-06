const fs = require('fs');

const path = 'C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/.system_generated/logs/transcript.jsonl';
const lines = fs.readFileSync(path, 'utf8').trim().split('\n');
lines.forEach(l => {
  try {
    const o = JSON.parse(l);
    if (o.step_index >= 264 && o.step_index <= 300) {
      if (o.type === 'PLANNER_RESPONSE' && o.content) {
        console.log(`Step ${o.step_index}:`, o.content.substring(0, 300));
      }
    }
  } catch (e) {}
});
