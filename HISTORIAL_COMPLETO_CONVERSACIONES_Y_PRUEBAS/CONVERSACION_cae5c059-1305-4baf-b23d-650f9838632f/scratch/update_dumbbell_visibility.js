const fs = require('fs');

const appPath = 'c:/Users/andre/OneDrive/Desktop/gym/LEGENDS/src/components/access/IndependentTerminalApp.tsx';
let appContent = fs.readFileSync(appPath, 'utf8').replace(/\r\n/g, '\n');

// 1. Update Modal 1
const oldModal1 = `<div className="relative bg-[#0d0f17]/92 backdrop-blur-2xl border-2 border-amber-500/50 rounded-3xl max-w-xl w-full p-5 sm:p-6 shadow-2xl shadow-amber-500/15 animate-in zoom-in-95 duration-200 my-auto max-h-[95vh] overflow-y-auto overflow-hidden">
            {/* Background Fitness Dumbbell Photo */}
            <div className="absolute inset-0 pointer-events-none z-0 overflow-hidden">
              <img 
                src={modalGymBg} onError={(e) => { (e.currentTarget as HTMLImageElement).src = './modal-gym-bg.jpg'; }} alt="Fondo Mancuernas Fitness" 
                className="w-full h-full object-cover object-bottom opacity-35 select-none pointer-events-none"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-[#090a0f]/95 via-[#0e1017]/85 to-[#0e1017]/90 pointer-events-none" />
              <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-transparent via-amber-400 to-transparent shadow-[0_0_15px_#f59e0b] pointer-events-none" />
            </div>`;

const newModal1 = `<div className="relative bg-[#0d0f17]/85 backdrop-blur-2xl border-2 border-amber-500/50 rounded-3xl max-w-xl w-full p-5 sm:p-6 shadow-2xl shadow-amber-500/20 animate-in zoom-in-95 duration-200 my-auto max-h-[95vh] overflow-y-auto overflow-hidden">
            {/* Background Fitness Dumbbell Photo */}
            <div className="absolute inset-0 pointer-events-none z-0 overflow-hidden">
              <img 
                src={modalGymBg} onError={(e) => { (e.currentTarget as HTMLImageElement).src = './modal-gym-bg.jpg'; }} alt="Fondo Mancuernas Fitness" 
                style={{ filter: 'brightness(1.42) contrast(1.25) saturate(1.15)' }}
                className="w-full h-full object-cover object-bottom opacity-85 select-none pointer-events-none"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-[#090a0f]/35 via-[#0e1017]/55 to-[#0e1017]/85 pointer-events-none" />
              <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-transparent via-amber-400 to-transparent shadow-[0_0_15px_#f59e0b] pointer-events-none" />
              <div className="absolute bottom-0 right-0 w-80 h-60 bg-amber-500/10 rounded-full blur-3xl pointer-events-none" />
            </div>`;

// 2. Update Modal 2
const oldModal2 = `<div className="relative bg-[#0d0f17]/92 backdrop-blur-2xl border-2 border-emerald-500/50 rounded-3xl max-w-md w-full p-6 shadow-2xl shadow-emerald-500/15 animate-in zoom-in-95 duration-200 overflow-hidden">
            <div className="absolute inset-0 pointer-events-none z-0 overflow-hidden">
              <img 
                src={modalGymBg} onError={(e) => { (e.currentTarget as HTMLImageElement).src = './modal-gym-bg.jpg'; }} alt="Fondo Mancuernas Fitness" 
                className="w-full h-full object-cover object-bottom opacity-35 select-none pointer-events-none"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-[#090a0f]/95 via-[#0e1017]/85 to-[#0e1017]/90 pointer-events-none" />
              <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-transparent via-emerald-400 to-transparent shadow-[0_0_15px_#10b981] pointer-events-none" />
            </div>`;

const newModal2 = `<div className="relative bg-[#0d0f17]/85 backdrop-blur-2xl border-2 border-emerald-500/50 rounded-3xl max-w-md w-full p-6 shadow-2xl shadow-emerald-500/20 animate-in zoom-in-95 duration-200 overflow-hidden">
            <div className="absolute inset-0 pointer-events-none z-0 overflow-hidden">
              <img 
                src={modalGymBg} onError={(e) => { (e.currentTarget as HTMLImageElement).src = './modal-gym-bg.jpg'; }} alt="Fondo Mancuernas Fitness" 
                style={{ filter: 'brightness(1.42) contrast(1.25) saturate(1.15)' }}
                className="w-full h-full object-cover object-bottom opacity-85 select-none pointer-events-none"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-[#090a0f]/35 via-[#0e1017]/55 to-[#0e1017]/85 pointer-events-none" />
              <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-transparent via-emerald-400 to-transparent shadow-[0_0_15px_#10b981] pointer-events-none" />
              <div className="absolute bottom-0 right-0 w-80 h-60 bg-emerald-500/10 rounded-full blur-3xl pointer-events-none" />
            </div>`;

// 3. Update Modal 3
const oldModal3 = `<div className="relative bg-[#0d0f17]/92 backdrop-blur-2xl border-2 border-amber-500/50 rounded-3xl max-w-lg w-full p-6 shadow-2xl shadow-amber-500/15 animate-in zoom-in-95 duration-200 overflow-hidden">
            <div className="absolute inset-0 pointer-events-none z-0 overflow-hidden">
              <img 
                src={modalGymBg} onError={(e) => { (e.currentTarget as HTMLImageElement).src = './modal-gym-bg.jpg'; }} alt="Fondo Mancuernas Fitness" 
                className="w-full h-full object-cover object-bottom opacity-35 select-none pointer-events-none"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-[#090a0f]/95 via-[#0e1017]/85 to-[#0e1017]/90 pointer-events-none" />
              <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-transparent via-amber-400 to-transparent shadow-[0_0_15px_#f59e0b] pointer-events-none" />
            </div>`;

const newModal3 = `<div className="relative bg-[#0d0f17]/85 backdrop-blur-2xl border-2 border-amber-500/50 rounded-3xl max-w-lg w-full p-6 shadow-2xl shadow-amber-500/20 animate-in zoom-in-95 duration-200 overflow-hidden">
            <div className="absolute inset-0 pointer-events-none z-0 overflow-hidden">
              <img 
                src={modalGymBg} onError={(e) => { (e.currentTarget as HTMLImageElement).src = './modal-gym-bg.jpg'; }} alt="Fondo Mancuernas Fitness" 
                style={{ filter: 'brightness(1.42) contrast(1.25) saturate(1.15)' }}
                className="w-full h-full object-cover object-bottom opacity-85 select-none pointer-events-none"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-[#090a0f]/35 via-[#0e1017]/55 to-[#0e1017]/85 pointer-events-none" />
              <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-transparent via-amber-400 to-transparent shadow-[0_0_15px_#f59e0b] pointer-events-none" />
              <div className="absolute bottom-0 right-0 w-80 h-60 bg-amber-500/10 rounded-full blur-3xl pointer-events-none" />
            </div>`;

let count = 0;
if (appContent.includes(oldModal1)) {
  appContent = appContent.replace(oldModal1, newModal1);
  count++;
}
if (appContent.includes(oldModal2)) {
  appContent = appContent.replace(oldModal2, newModal2);
  count++;
}
if (appContent.includes(oldModal3)) {
  appContent = appContent.replace(oldModal3, newModal3);
  count++;
}

fs.writeFileSync(appPath, appContent, 'utf8');
console.log(`Replaced ${count} modals in IndependentTerminalApp.tsx`);

// Also update scripts/build-single-html.cjs
const buildPath = 'c:/Users/andre/OneDrive/Desktop/gym/LEGENDS/scripts/build-single-html.cjs';
let buildContent = fs.readFileSync(buildPath, 'utf8').replace(/\r\n/g, '\n');

buildContent = buildContent.replace(
  /style=\{\{\s*filter:\s*'brightness\(1\.25\)\s*contrast\(1\.12\)'\s*\}\}\s*className="w-full h-full object-cover object-bottom opacity-65 select-none pointer-events-none"\s*\/>\s*<div className="absolute inset-0 bg-gradient-to-t from-\[#090a0f\]\/55 via-\[#0e1017\]\/65 to-\[#0e1017\]\/80 pointer-events-none"/g,
  `style={{ filter: 'brightness(1.42) contrast(1.25) saturate(1.15)' }}
                    className="w-full h-full object-cover object-bottom opacity-85 select-none pointer-events-none"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-[#090a0f]/35 via-[#0e1017]/55 to-[#0e1017]/85 pointer-events-none" />
                  <div className="absolute bottom-0 right-0 w-80 h-60 bg-amber-500/10 rounded-full blur-3xl pointer-events-none"`
);

fs.writeFileSync(buildPath, buildContent, 'utf8');
console.log('Updated build-single-html.cjs');
