const { app, BrowserWindow } = require('electron');
const path = require('path');
const fs = require('fs');

app.commandLine.appendSwitch('disable-http-cache');

app.whenReady().then(async () => {
  const win = new BrowserWindow({
    width: 1280,
    height: 900,
    show: false,
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      webSecurity: false
    }
  });

  const indexPath = path.resolve('LEGENDS/release/FITNESS_LEGENDS_TERMINAL-win32-x64/resources/app/dist/index.html');
  await win.loadFile(indexPath, { query: { mode: 'terminal' } });

  // Wait 2.5 seconds for React to mount
  await new Promise(r => setTimeout(r, 2500));

  // Click the first card ("Cliente Nuevo")
  await win.webContents.executeJavaScript(`
    const cards = document.querySelectorAll('.grid > div');
    if (cards && cards[0]) {
      cards[0].click();
    }
  `);

  // Wait for modal animation to settle
  await new Promise(r => setTimeout(r, 1200));

  const image = await win.capturePage();
  const outPath = 'C:/Users/andre/.gemini/antigravity/brain/cae5c059-1305-4baf-b23d-650f9838632f/modal_captured.png';
  fs.writeFileSync(outPath, image.toPNG());
  console.log('✓ Captured modal screenshot to:', outPath);

  app.quit();
});
