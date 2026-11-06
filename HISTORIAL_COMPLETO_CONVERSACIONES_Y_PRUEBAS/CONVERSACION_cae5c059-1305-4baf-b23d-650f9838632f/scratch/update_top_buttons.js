const fs = require('fs');

const path = 'c:/Users/andre/OneDrive/Desktop/gym/LEGENDS/src/components/access/IndependentTerminalApp.tsx';
let content = fs.readFileSync(path, 'utf8').replace(/\r\n/g, '\n');

const target = `      {/* BOTÓN PANTALLA COMPLETA (VISIBLE SOLO CUANDO NO ESTÁ EN PANTALLA COMPLETA) */}
      {!isFullScreen && (
        <div className="fixed top-4 right-4 z-40 flex items-center gap-2">
          {onOpenClients && (
            <button
              onClick={onOpenClients}
              className="px-3.5 py-2.5 rounded-2xl bg-gradient-to-r from-amber-500/25 to-amber-600/35 hover:from-amber-500/40 hover:to-amber-600/50 backdrop-blur-md border border-amber-400/70 text-amber-300 hover:text-white font-black text-xs sm:text-sm flex items-center gap-2 shadow-[0_0_20px_rgba(245,158,11,0.35)] transition-all cursor-pointer transform hover:scale-105 active:scale-95 select-none"
              title="Abrir Cartera de Clientes & Membresías sin cerrar el programa"
            >
              <Users className="w-4 h-4 text-amber-400" />
              <span className="font-black">CARTERA DE CLIENTES</span>
            </button>
          )}
          <button
            onClick={toggleBackground}
            className="px-3.5 py-2.5 rounded-2xl bg-black/70 hover:bg-black/90 backdrop-blur-md border border-white/20 text-white font-bold text-xs sm:text-sm flex items-center gap-2 shadow-2xl transition-all cursor-pointer transform hover:scale-105 active:scale-95 select-none"
            title="Cambiar imagen de fondo (Mancuernas / Estudio / Neón)"
          >
            <Image className="w-4 h-4 text-amber-400" />
            <span className="hidden sm:inline">CAMBIAR FONDO</span>
          </button>
          <button
            onClick={toggleFullScreen}
            className="px-4 py-2.5 rounded-2xl bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-white font-black text-xs sm:text-sm flex items-center gap-2 shadow-2xl shadow-amber-500/40 transition-all cursor-pointer transform hover:scale-105 active:scale-95 select-none"
            title="Activar Pantalla Completa"
          >
            <Maximize2 className="w-4 h-4" />
            <span>PANTALLA COMPLETA</span>
          </button>
        </div>
      )}`;

const replacement = `      {/* BOTONES SUPERIORES DE ACCESO RÁPIDO */}
      <div className="fixed top-4 right-4 z-40 flex items-center gap-2">
        {onOpenClients && (
          <button
            onClick={onOpenClients}
            className="px-3.5 py-2.5 rounded-2xl bg-gradient-to-r from-amber-500/25 to-amber-600/35 hover:from-amber-500/40 hover:to-amber-600/50 backdrop-blur-md border border-amber-400/70 text-amber-300 hover:text-white font-black text-xs sm:text-sm flex items-center gap-2 shadow-[0_0_20px_rgba(245,158,11,0.35)] transition-all cursor-pointer transform hover:scale-105 active:scale-95 select-none"
            title="Abrir Cartera de Clientes & Membresías sin cerrar el programa"
          >
            <Users className="w-4 h-4 text-amber-400" />
            <span className="font-black">CARTERA DE CLIENTES</span>
          </button>
        )}
        <button
          onClick={toggleBackground}
          className="px-3.5 py-2.5 rounded-2xl bg-black/70 hover:bg-black/90 backdrop-blur-md border border-white/20 text-white font-bold text-xs sm:text-sm flex items-center gap-2 shadow-2xl transition-all cursor-pointer transform hover:scale-105 active:scale-95 select-none"
          title="Cambiar imagen de fondo oficial del gimnasio"
        >
          <Image className="w-4 h-4 text-amber-400" />
          <span className="hidden sm:inline">CAMBIAR FONDO</span>
        </button>
        <button
          onClick={toggleFullScreen}
          className="px-4 py-2.5 rounded-2xl bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-white font-black text-xs sm:text-sm flex items-center gap-2 shadow-2xl shadow-amber-500/40 transition-all cursor-pointer transform hover:scale-105 active:scale-95 select-none"
          title={isFullScreen ? "Salir de Pantalla Completa" : "Activar Pantalla Completa"}
        >
          {isFullScreen ? <Minimize2 className="w-4 h-4" /> : <Maximize2 className="w-4 h-4" />}
          <span>{isFullScreen ? "SALIR PANTALLA" : "PANTALLA COMPLETA"}</span>
        </button>
      </div>`;

if (content.includes(target)) {
  fs.writeFileSync(path, content.replace(target, replacement), 'utf8');
  console.log('Successfully updated top action buttons in IndependentTerminalApp.tsx');
} else {
  console.error('Target not found in IndependentTerminalApp.tsx');
}
