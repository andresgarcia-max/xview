const fs = require('fs');

const path = 'c:/Users/andre/OneDrive/Desktop/gym/LEGENDS/src/App.tsx';
let content = fs.readFileSync(path, 'utf8');

const target = `const AppContent: React.FC = () => {
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

const replacement = `const AppContent: React.FC = () => {
  const { isAuthenticated } = useGym();
  const [terminalMode] = useState<boolean>(() => isTerminalMode());
  const [terminalView, setTerminalView] = useState<'terminal' | 'clients'>('terminal');

  // Independent Turnstile terminal mode for separate computer
  if (terminalMode) {
    if (terminalView === 'clients') {
      return (
        <div className="min-h-screen bg-slate-950 text-white flex flex-col">
          <div className="p-4 bg-slate-900 border-b border-amber-500/30 flex items-center justify-between shadow-xl">
            <div className="flex items-center gap-3">
              <span className="text-xl font-black text-amber-400">FITNESS LEGENDS</span>
              <span className="text-xs bg-amber-500/20 text-amber-300 border border-amber-500/40 px-2 py-0.5 rounded-full font-bold uppercase">
                Cartera de Clientes & Membresías
              </span>
            </div>
            <button
              onClick={() => setTerminalView('terminal')}
              className="px-4 py-2 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-black font-black text-xs uppercase tracking-wider shadow-glow-gold transition-all cursor-pointer"
            >
              ⚡ Volver a Terminal de Acceso
            </button>
          </div>
          <div className="flex-1 p-4 sm:p-6 overflow-y-auto">
            <ClientList />
          </div>
        </div>
      );
    }
    return <IndependentTerminalApp onOpenClients={() => setTerminalView('clients')} />;
  }

  if (!isAuthenticated) {
    return <LoginScreen />;
  }

  return <MainLayout />;
};`;

const normalizedContent = content.replace(/\r\n/g, '\n');
const normalizedTarget = target.replace(/\r\n/g, '\n');

if (normalizedContent.includes(normalizedTarget)) {
  const updated = normalizedContent.replace(normalizedTarget, replacement);
  fs.writeFileSync(path, updated, 'utf8');
  console.log('Successfully updated AppContent in App.tsx');
} else {
  console.error('Target not found in App.tsx');
}
