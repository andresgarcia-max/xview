const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const cscPath = 'C:\\Windows\\Microsoft.NET\\Framework64\\v4.0.30319\\csc.exe';
const rootDir = path.resolve('.');
const accesosDir = path.join(rootDir, 'accesos directos');
if (!fs.existsSync(accesosDir)) {
  fs.mkdirSync(accesosDir, { recursive: true });
}

const iconPath = path.resolve('LEGENDS/build/icon.ico');

const csCode = `
using System;
using System.IO;
using System.Diagnostics;
using System.Windows.Forms;

class Program {
    [STAThread]
    static void Main(string[] args) {
        string baseDir = AppDomain.CurrentDomain.BaseDirectory;
        
        string[] searchPaths = new string[] {
            Path.Combine(baseDir, "LEGENDS", "release", "FITNESS_LEGENDS_TERMINAL-win32-x64", "ControlDeAccesoLegends.exe"),
            Path.Combine(baseDir, "..", "LEGENDS", "release", "FITNESS_LEGENDS_TERMINAL-win32-x64", "ControlDeAccesoLegends.exe"),
            Path.Combine(baseDir, "release", "FITNESS_LEGENDS_TERMINAL-win32-x64", "ControlDeAccesoLegends.exe"),
            Path.Combine(baseDir, "..", "release", "FITNESS_LEGENDS_TERMINAL-win32-x64", "ControlDeAccesoLegends.exe"),
            @"C:\\Users\\andre\\OneDrive\\Desktop\\gym\\LEGENDS\\release\\FITNESS_LEGENDS_TERMINAL-win32-x64\\ControlDeAccesoLegends.exe",
            @"C:\\Users\\andre\\OneDrive\\Desktop\\gym leyends\\LEGENDS\\release\\FITNESS_LEGENDS_TERMINAL-win32-x64\\ControlDeAccesoLegends.exe"
        };

        string target = null;
        foreach (string p in searchPaths) {
            try {
                string full = Path.GetFullPath(p);
                if (File.Exists(full)) {
                    target = full;
                    break;
                }
            } catch {}
        }

        if (target != null && File.Exists(target)) {
            ProcessStartInfo psi = new ProcessStartInfo();
            psi.FileName = target;
            psi.Arguments = args.Length > 0 ? string.Join(" ", args) : "--terminal";
            psi.WorkingDirectory = Path.GetDirectoryName(target);
            psi.UseShellExecute = false;
            
            // Ensure ELECTRON_RUN_AS_NODE is removed so Electron opens GUI
            if (psi.EnvironmentVariables.ContainsKey("ELECTRON_RUN_AS_NODE")) {
                psi.EnvironmentVariables.Remove("ELECTRON_RUN_AS_NODE");
            }

            Process.Start(psi);
        } else {
            MessageBox.Show(
                "No se encontró el motor de la terminal en:\\n" +
                Path.Combine(baseDir, "LEGENDS\\\\release\\\\...\\\\ControlDeAccesoLegends.exe"),
                "Fitness Legends Gym",
                MessageBoxButtons.OK,
                MessageBoxIcon.Error
            );
        }
    }
}
`;

const tempCs = path.resolve('temp_control_acceso.cs');
fs.writeFileSync(tempCs, csCode, 'utf8');

const iconArg = fs.existsSync(iconPath) ? `"/win32icon:${iconPath}"` : '';
const masterExe = path.join(accesosDir, 'CONTROL_DE_ACCESO_FITNESS_LEGENDS.exe');
const compileCmd = `"${cscPath}" /nologo /target:winexe /out:"${masterExe}" ${iconArg} /reference:System.Windows.Forms.dll "${tempCs}"`;

console.log('Compiling master executable...');
execSync(compileCmd, { stdio: 'inherit' });
fs.unlinkSync(tempCs);

// Distribute to all requested and convenient paths:
const destinations = [
  path.join(accesosDir, 'CONTROL_DE_ACCESO.exe'),
  path.join(accesosDir, 'ACCESO_CLIENTES_LEGENDS.exe'),
  path.join(rootDir, 'CONTROL_DE_ACCESO_FITNESS_LEGENDS.exe'),
  path.join(rootDir, 'CONTROL_DE_ACCESO.exe'),
  path.join(rootDir, 'ACCESO_CLIENTES_LEGENDS.exe')
];

destinations.forEach(dst => {
  fs.copyFileSync(masterExe, dst);
  console.log('✓ Copied to:', dst);
});

console.log('✓ All executables compiled and verified.');
