const fs = require('fs');

const path = 'C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/.system_generated/logs/transcript.jsonl';
const lines = fs.readFileSync(path, 'utf8').trim().split('\n');
for (let i = 264; i < 545; i++) {
  const o = JSON.parse(lines[i]);
  if (o.type === 'PLANNER_RESPONSE' && o.content && (!o.tool_calls || o.tool_calls.length === 0)) {
    console.log(`Line ${i}: ${o.content.substring(0, 300)}\n---`);
  }
}
