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
            Process.Start(psi);
        } else {
            MessageBox.Show(
                "No se encontró el ejecutable principal en ninguna de las rutas esperadas:\\n" +
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

// 1. Output to "accesos directos\CONTROL_DE_ACCESO_FITNESS_LEGENDS.exe"
const outExe1 = path.join(accesosDir, 'CONTROL_DE_ACCESO_FITNESS_LEGENDS.exe');
const cmd1 = `"${cscPath}" /nologo /target:winexe /out:"${outExe1}" ${iconArg} /reference:System.Windows.Forms.dll "${tempCs}"`;
console.log('Compiling to:', outExe1);
execSync(cmd1, { stdio: 'inherit' });

// 2. Output to root "CONTROL_DE_ACCESO_FITNESS_LEGENDS.exe"
const outExe2 = path.join(rootDir, 'CONTROL_DE_ACCESO_FITNESS_LEGENDS.exe');
fs.copyFileSync(outExe1, outExe2);
console.log('Copied to:', outExe2);

// 3. Also output "ACCESO_CLIENTES_LEGENDS.exe" into "accesos directos"
const outExe3 = path.join(accesosDir, 'ACCESO_CLIENTES_LEGENDS.exe');
fs.copyFileSync(outExe1, outExe3);
console.log('Copied to:', outExe3);

// 4. Also update the one in root
const outExe4 = path.join(rootDir, 'ACCESO_CLIENTES_LEGENDS.exe');
fs.copyFileSync(outExe1, outExe4);
console.log('Copied to:', outExe4);

fs.unlinkSync(tempCs);
console.log('✓ All executables generated successfully.');
