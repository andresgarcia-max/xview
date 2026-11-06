const fs = require('fs');

const content = fs.readFileSync('LEGENDS/public/Control_De_Acceso_Web.html', 'utf8');

function extractBase64(varName, outFile) {
  const marker = 'const ' + varName + ' = "data:image/jpeg;base64,';
  const start = content.indexOf(marker);
  if (start === -1) {
    console.log(varName, 'not found');
    return;
  }
  const dataStart = start + marker.length;
  const end = content.indexOf('"', dataStart);
  const base64Data = content.substring(dataStart, end);
  const buffer = Buffer.from(base64Data, 'base64');
  fs.writeFileSync(outFile, buffer);
  console.log(`Saved ${varName} to ${outFile} (${buffer.length} bytes)`);
}

extractBase64('BG_PHOTO_1', 'C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/extracted_bg1.jpg');
extractBase64('BG_PHOTO_2', 'C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/extracted_bg2.jpg');
extractBase64('BG_PHOTO_3', 'C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/extracted_bg3.jpg');
