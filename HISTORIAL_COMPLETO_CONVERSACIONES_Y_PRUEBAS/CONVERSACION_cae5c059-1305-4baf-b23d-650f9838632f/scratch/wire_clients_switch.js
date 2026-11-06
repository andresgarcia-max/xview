const fs = require('fs');
const path = require('path');

// 1. Update IndependentTerminalApp.tsx
const termFile = path.resolve('LEGENDS/src/components/access/IndependentTerminalApp.tsx');
let termCode = fs.readFileSync(termFile, 'utf8');

// Add Users to imports
if (!termCode.includes('Users,')) {
  termCode = termCode.replace(
    /UserPlus,\r?\n/,
    'UserPlus,\r\n  Users,\r\n'
  );
}

// Add onOpenClients to interface
termCode = termCode.replace(
  /interface IndependentTerminalAppProps \{[\s\S]*?\}/,
  `interface IndependentTerminalAppProps {\r\n  isEmbeddedFullScreen?: boolean;\r\n  onExitFullScreen?: () => void;\r\n  onOpenClients?: () => void;\r\n}`
);

// Add onOpenClients to component params
termCode = termCode.replace(
  /export const IndependentTerminalApp: React\.FC<IndependentTerminalAppProps> = \(\{\r?\n\s*isEmbeddedFullScreen = false,\r?\n\s*onExitFullScreen,?\r?\n\}\) => \{/,
  `export const IndependentTerminalApp: React.FC<IndependentTerminalAppProps> = ({\r\n  isEmbeddedFullScreen = false,\r\n  onExitFullScreen,\r\n  onOpenClients,\r\n}) => {`
);

// Add CARTERA DE CLIENTES button next to CAMBIAR FONDO
const targetButtons = `<div className="fixed top-4 right-4 z-40 flex items-center gap-2">`;
const replacementButtons = `<div className="fixed top-4 right-4 z-40 flex items-center gap-2">
          {onOpenClients && (
            <button
              onClick={onOpenClients}
              className="px-3.5 py-2.5 rounded-2xl bg-gradient-to-r from-amber-500/25 to-amber-600/35 hover:from-amber-500/40 hover:to-amber-600/50 backdrop-blur-md border border-amber-400/70 text-amber-300 hover:text-white font-black text-xs sm:text-sm flex items-center gap-2 shadow-[0_0_20px_rgba(245,158,11,0.35)] transition-all cursor-pointer transform hover:scale-105 active:scale-95 select-none"
              title="Abrir Cartera de Clientes & Membresías sin cerrar el programa"
            >
              <Users className="w-4 h-4 text-amber-400" />
              <span className="font-black">CARTERA DE CLIENTES</span>
            </button>
          )}`;

if (termCode.includes(targetButtons) && !termCode.includes('CARTERA DE CLIENTES')) {
  termCode = termCode.replace(targetButtons, replacementButtons);
}

fs.writeFileSync(termFile, termCode, 'utf8');
console.log('✓ Successfully updated IndependentTerminalApp.tsx');

// 2. Update App.tsx
const appFile = path.resolve('LEGENDS/src/App.tsx');
let appCode = fs.readFileSync(appFile, 'utf8');

// Import Users and Zap if not present
if (!appCode.includes('import { Users, Zap }')) {
  appCode = `import { Users, Zap } from 'lucide-react';\r\n` + appCode;
}

// Add isClientsMode helper
if (!appCode.includes('export const isClientsMode')) {
  const targetMode = `export const isTerminalMode = (): boolean => {`;
  const newMode = `export const isClientsMode = (): boolean => {
  if (typeof window === 'undefined') return false;
  const searchParams = new URLSearchParams(window.location.search);
  if (searchParams.get('mode') === 'clients' || searchParams.get('tab') === 'clients') return true;
  return false;
};

export const isTerminalMode = (): boolean => {`;
  appCode = appCode.replace(targetMode, newMode);
}

// Update AppContent component
const targetAppContent = `const AppContent: React.FC = () => {
  const { isAuthenticated } = useGym();
  const [terminalMode] = useState<boolean>(() => isTerminalMode());

  // Independent Turnstile terminal mode for separate computer
  if (terminalMode) {
    return <IndependentTerminalApp />;
  }

  if (!isAuthenticated) {
    return <LoginScreen />;
  }

  return <MainLayout />;
};`;

const newAppContent = `const AppContent: React.FC = () => {
  const { isAuthenticated } = useGym();
  const [terminalView, setTerminalView] = useState<'terminal' | 'clients' | 'main'>(() => {
    if (isClientsMode()) return 'clients';
    if (isTerminalMode()) return 'terminal';
    return 'main';
  });

  if (terminalView === 'terminal') {
    return <IndependentTerminalApp onOpenClients={() => setTerminalView('clients')} />;
  }

  if (terminalView === 'clients') {
    return (
      <div className="min-h-screen bg-[#07080b] text-gym-text flex flex-col font-sans select-none sm:select-auto relative overflow-hidden">
        {/* Ambient background */}
        <div className="fixed inset-0 pointer-events-none z-0 overflow-hidden">
          <img
            src={gymDumbbellBg}
            alt="Gym Dumbbell Background"
            className="w-full h-full object-cover filter brightness-[0.38] contrast-[1.12] saturate-[0.85]"
          />
          <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_50%_15%,rgba(245,158,11,0.08)_0%,rgba(0,0,0,0.65)_70%)]" />
          <div className="absolute inset-0 bg-gradient-to-b from-black/45 via-black/35 to-black/70" />
        </div>

        {/* Top Navbar for quick navigation back to Terminal */}
        <header className="bg-[#09090b]/95 backdrop-blur-xl border-b border-amber-500/30 sticky top-0 z-40 px-4 sm:px-6 py-2.5 shadow-2xl flex items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-black border border-amber-500/40 p-1 flex items-center justify-center shadow-[0_0_15px_rgba(245,158,11,0.2)]">
              <img src="./logo.png" onError={(e) => { (e.currentTarget as HTMLImageElement).src = '/logo.png'; }} alt="Legends Logo" className="w-full h-full object-contain" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-black text-lg tracking-wider text-white">CARTERA DE CLIENTES & MEMBRESÍAS</span>
                <span className="text-[10px] uppercase font-black px-2 py-0.5 rounded-md bg-amber-500/20 text-amber-300 border border-amber-500/40">VIP PRO</span>
              </div>
              <p className="text-[10px] text-zinc-400 font-medium">Expedientes de Socios, Vigencias, Huellas y Renovaciones</p>
            </div>
          </div>

          <div className="flex items-center gap-2 sm:gap-3">
            <button
              onClick={() => setTerminalView('terminal')}
              className="px-4 py-2.5 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-black font-black text-xs sm:text-sm flex items-center gap-2 shadow-lg shadow-amber-500/30 transition-transform active:scale-95 cursor-pointer"
              title="Volver a la pantalla de Acceso de Clientes / Torniquetes"
            >
              <Zap className="w-4 h-4 text-black fill-current" />
              <span>⚡ VOLVER A TERMINAL DE ACCESO</span>
            </button>
          </div>
        </header>

        {/* Client Management Workspace */}
        <main className="flex-1 overflow-y-auto p-4 sm:p-6 lg:p-8 max-w-7xl w-full mx-auto relative z-10">
          <ClientList />
        </main>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <LoginScreen />;
  }

  return <MainLayout />;
};`;

appCode = appCode.replace(targetAppContent, newAppContent);
fs.writeFileSync(appFile, appCode, 'utf8');
console.log('✓ Successfully updated App.tsx');
