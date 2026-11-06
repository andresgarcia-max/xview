const fs = require('fs');

function getJpegDimensions(path) {
  const buf = fs.readFileSync(path);
  let i = 2;
  while (i < buf.length) {
    if (buf[i] !== 0xFF) break;
    const marker = buf[i + 1];
    if (marker === 0xC0 || marker === 0xC2) {
      const height = buf.readUInt16BE(i + 5);
      const width = buf.readUInt16BE(i + 7);
      return { width, height };
    }
    const len = buf.readUInt16BE(i + 2);
    i += 2 + len;
  }
  return null;
}

console.log('bg_before_9696c8e.jpg:', getJpegDimensions('C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/scratch/bg_before_9696c8e.jpg'));
console.log('bg_2032872.jpg:       ', getJpegDimensions('C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/scratch/bg_2032872.jpg'));
console.log('motto_34b1832.jpg:    ', getJpegDimensions('C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/scratch/motto_34b1832.jpg'));
