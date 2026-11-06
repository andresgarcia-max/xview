const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const cscPath = 'C:\\Windows\\Microsoft.NET\\Framework64\\v4.0.30319\\csc.exe';
const rootDir = path.resolve('.');
const iconPath = path.resolve('LEGENDS/build/icon.ico');
const outputExe = path.resolve('ACCESO_CLIENTES_LEGENDS.exe');

const csCode = `
using System;
using System.IO;
using System.Diagnostics;
using System.Windows.Forms;

class Program {
    [STAThread]
    static void Main(string[] args) {
        string baseDir = AppDomain.CurrentDomain.BaseDirectory;
        string target = Path.Combine(baseDir, "LEGENDS", "release", "FITNESS_LEGENDS_TERMINAL-win32-x64", "ControlDeAccesoLegends.exe");
        if (!File.Exists(target)) {
            target = Path.Combine(baseDir, "release", "FITNESS_LEGENDS_TERMINAL-win32-x64", "ControlDeAccesoLegends.exe");
        }
        if (File.Exists(target)) {
            ProcessStartInfo psi = new ProcessStartInfo();
            psi.FileName = target;
            // Launch directly into client access terminal mode
            psi.Arguments = "--terminal";
            psi.WorkingDirectory = Path.GetDirectoryName(target);
            psi.UseShellExecute = false;
            Process.Start(psi);
        } else {
            MessageBox.Show("No se encontró el motor de la terminal en:\\n" + target, "Fitness Legends", MessageBoxButtons.OK, MessageBoxIcon.Error);
        }
    }
}
`;

const tempCs = path.resolve('temp_launcher.cs');
fs.writeFileSync(tempCs, csCode, 'utf8');

const iconArg = fs.existsSync(iconPath) ? `"/win32icon:${iconPath}"` : '';
const cmd = `"${cscPath}" /nologo /target:winexe /out:"${outputExe}" ${iconArg} /reference:System.Windows.Forms.dll "${tempCs}"`;
console.log('Compiling:', cmd);
execSync(cmd, { stdio: 'inherit' });
fs.unlinkSync(tempCs);

console.log('✓ Successfully created:', outputExe);
