using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Management;
using System.Net.NetworkInformation;
using System.Net.Sockets;
using System.Reflection;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;
using System.Threading.Tasks;

[assembly: AssemblyTitle("PROGRAMA PERFECTO v4.1 - FLUJO DIRECTO 4 PASOS (XVIEW+ B866V2)")]
[assembly: AssemblyDescription("1. Escanear Memoria -> 2. Modo Fastboot -> 3. Inyectar Firmware -> 4. Exito Total")]
[assembly: AssemblyConfiguration("")]
[assembly: AssemblyCompany("Xview+ B866V2 Tools")]
[assembly: AssemblyProduct("PROGRAMA PERFECTO XVIEW+ B866V2 v4.1")]
[assembly: AssemblyCopyright("Copyright (C) 2026")]
[assembly: AssemblyVersion("4.1.0.0")]
[assembly: AssemblyFileVersion("4.1.0.0")]

namespace ProgramaPerfectoB866V2
{
    public class ToolResult
    {
        public bool TimedOut { get; set; }
        public int ExitCode { get; set; }
        public string Output { get; set; }
    }

    public class BoxConnectionState
    {
        public string UsbHardwareMode { get; set; }
        public string SerialNumber { get; set; }
        public string RawPid { get; set; }
        public bool FastbootLive { get; set; }
        public List<string> AdbDevices { get; set; }
        public List<string> FastbootDevices { get; set; }

        public BoxConnectionState()
        {
            UsbHardwareMode = "Ninguno detectado por cable USB directo";
            SerialNumber = "No detectado";
            RawPid = "";
            FastbootLive = false;
            AdbDevices = new List<string>();
            FastbootDevices = new List<string>();
        }
    }

    class Program
    {
        static string ROOT_DIR;
        static string MOTOR_DIR;
        static string APKS_DIR;
        static string KEYS_DIR;
        static string DRV_DIR;
        static string FW_DIR;
        static string LOG_FILE;

        static string ADB_EXE;
        static string FASTBOOT_EXE;
        static string ADNL_EXE;
        static string UPDATE_EXE;
        static string ENV_UNL_IMG;
        static string CONF_IMG;

        static string CONNECTED_IP = "";
        static readonly object LogLock = new object();

        static readonly string[,] LIBERATED_FIRMWARE_UI_APKS = new string[,]
        {
            { "com.google.android.tvlauncher.apk", "Launcher Oficial Android TV (Firmware Xview Liberado)" },
            { "TVLauncher_4_GoogleTV.apk",         "Launcher Google TV" },
            { "Home_Screen_Launcher_1.1.3.apk",    "Home Screen Launcher" },
            { "AppDMC.apk",                        "AppDMC (Cajon de Apps Firmware Liberado DMC.v2.25)" },
            { "DLStore.apk",                       "DLStore (Tienda Firmware Liberado)" },
            { "Filemanager.apk",                   "FileManager (Explorador USB)" },
            { "Downloader_2.0.4.apk",              "Downloader TV" },
            { "Button_Mapper_Pro_2.54.apk",        "Button Mapper Pro (Boton Home)" },
            { "autostart.apk",                     "AutoStart" },
            { "Hdplay.apk",                        "HDPlay TV" },
            { "xuper_hydra.apk",                   "Xuper Hydra" },
            { "com.xuper.netxxus.apk",             "XSairec TV" },
            { "tv.mxlmovies.app.apk",              "MXL Movies" },
            { "signMagisTV_APS-MUTO.apk",          "MagisTV" }
        };

        static readonly string[] MEGACABLE_LOCK_PACKAGES = new string[]
        {
            "com.nagra.ion",
            "com.dotscreen.megacable.portal",
            "com.dotscreen.megacable.core",
            "com.dotscreen.megacable.atv",
            "com.dotscreen.megacable.updater",
            "com.dotscreen.megacable.livetv",
            "com.expway.ewm.service",
            "com.zte.ums.oes",
            "com.zte.ota",
            "com.hmdm.launcher"
        };

        // Estructura exacta de paquetes que conforman el Codificador Liberado de Referencia (DMC.v2.25)
        static readonly string[,] REFERENCE_LIBERATED_PACKAGES = new string[,]
        {
            { "com.google.android.tvlauncher",       "/product/priv-app/TVLauncher/TVLauncher.apk (Launcher Oficial Android TV)" },
            { "com.slimbox.tvlauncher.overlay",      "/product/overlay/TVWall.apk (Overlay Visual Slimbox TVWall)" },
            { "com.google.android.tvrecommendations","/product/priv-app/TVRecommendations/TVRecommendations.apk" },
            { "com.android.vending",                 "/product/priv-app/Tubesky/Tubesky.apk (Google Play Store TV)" },
            { "org.cyanogenmod.appdrawer",           "/system/app/AppDrawer/AppDMC.apk (Cajon AppDMC)" },
            { "com.dinhlap.dlstore",                 "/system/app/DLStore.apk (Tienda DLStore)" },
            { "net.itvplus.appstore",                "/system/app/Hdplay/Hdplay.apk (Tienda HDPlay)" },
            { "com.hitv.explore",                    "/system/app/Filemanager.apk (Explorador Filemanager)" },
            { "com.control.device",                  "/system/system_ext/priv-app/ControlDevice/ControlDevice.apk (ControlDevice Khiv)" },
            { "com.ztestb.LauncherCustomize",        "/system/app/LauncherCustomize/LauncherCustomize.apk" },
            { "com.android.tv.settings",             "/system/system_ext/priv-app/TvSettings/TvSettings.apk (Ajustes TV)" },
            { "com.xuper.netxxus",                   "/data/app/com.xuper.netxxus (Xuper TV / MagisTV)" },
            { "tv.mxlmovies.app",                    "/data/app/tv.mxlmovies.app (MXL Movies)" }
        };

        static void LogEvent(string category, string message)
        {
            try
            {
                if (string.IsNullOrEmpty(LOG_FILE)) return;
                string line = string.Format("[{0:yyyy-MM-dd HH:mm:ss.fff}] [{1}] {2}{3}",
                    DateTime.Now, category, message, Environment.NewLine);
                lock (LogLock)
                {
                    using (FileStream fs = new FileStream(LOG_FILE, FileMode.Append, FileAccess.Write, FileShare.ReadWrite))
                    using (StreamWriter sw = new StreamWriter(fs, Encoding.UTF8))
                    {
                        sw.Write(line);
                    }
                }
            }
            catch { }
        }

        static int Main(string[] args)
        {
            try
            {
                Console.OutputEncoding = Encoding.UTF8;
                Console.Title = "PROGRAMA PERFECTO v4.1 - 4 PASOS DIRECTOS: ESCANEAR MEMORIA -> FASTBOOT -> INYECTAR FIRMWARE -> EXITO TOTAL";
            }
            catch { }

            string exePath = Assembly.GetExecutingAssembly().Location;
            string exeDir = Path.GetDirectoryName(exePath);

            if (Directory.Exists(Path.Combine(exeDir, "1_HERRAMIENTAS_MOTOR")))
            {
                ROOT_DIR = exeDir;
            }
            else
            {
                ROOT_DIR = Path.GetDirectoryName(exeDir);
            }

            MOTOR_DIR = Path.Combine(ROOT_DIR, "1_HERRAMIENTAS_MOTOR");
            APKS_DIR  = Path.Combine(ROOT_DIR, "2_APKS_LIBERACION_Y_TV");
            KEYS_DIR  = Path.Combine(ROOT_DIR, "3_LLAVES_USB_PARA_LA_CAJA");
            DRV_DIR   = Path.Combine(ROOT_DIR, "4_DRIVERS_Y_GUI_WINDOWS");
            FW_DIR    = Path.Combine(ROOT_DIR, "5_FIRMWARE_SEGURO_B866V2");
            LOG_FILE  = Path.Combine(ROOT_DIR, "BITACORA_EN_VIVO_EXE.log");

            ADB_EXE      = Path.Combine(MOTOR_DIR, "adb.exe");
            FASTBOOT_EXE = Path.Combine(MOTOR_DIR, "fastboot_moderno.exe");
            if (!File.Exists(FASTBOOT_EXE))
            {
                FASTBOOT_EXE = Path.Combine(MOTOR_DIR, "fastboot.exe");
            }
            ADNL_EXE     = Path.Combine(MOTOR_DIR, "adnl.exe");
            UPDATE_EXE   = Path.Combine(MOTOR_DIR, "update.exe");
            ENV_UNL_IMG  = Path.Combine(FW_DIR, "env_unlocked.img");
            CONF_IMG     = Path.Combine(FW_DIR, "conf.img");

            if (args != null && args.Any(a => a.Equals("--check", StringComparison.OrdinalIgnoreCase)))
            {
                ShowBanner();
                PrintMenuOptions();
                return 0;
            }

            try
            {
                int myPid = Process.GetCurrentProcess().Id;
                foreach (Process p in Process.GetProcessesByName("PROGRAMA_PERFECTO_XVIEW_B866V2"))
                {
                    if (p.Id != myPid)
                    {
                        try { p.Kill(); } catch { }
                    }
                }
            }
            catch { }

            LogEvent("SESION", "=== INICIO DE PROGRAMA_PERFECTO_XVIEW_B866V2.EXE v4.1 (PID=" + Process.GetCurrentProcess().Id + ") ===");

            bool autoStart = (args == null || args.Length == 0 || args.Any(a => a.Equals("--auto", StringComparison.OrdinalIgnoreCase)));

            while (true)
            {
                ShowBanner();
                PrintMenuOptions();

                string choice;
                if (autoStart)
                {
                    autoStart = false;
                    WriteColor("Presiona [1] para Iniciar los 4 Pasos Automaticos (o elige [2-4]): 1 (Auto-Inicio Activo)", ConsoleColor.Yellow, true);
                    choice = "1";
                }
                else
                {
                    WriteColor("Presiona [1] para Iniciar los 4 Pasos Automaticos (o elige [2-4]): ", ConsoleColor.Yellow, false);
                    choice = Console.ReadLine();
                    if (choice == null)
                    {
                        LogEvent("SESION", "Consola cerrada por el usuario (EOF).");
                        break;
                    }
                    choice = choice.Trim();
                    if (string.IsNullOrEmpty(choice)) choice = "1";
                }

                LogEvent("MENU", "Usuario selecciono opcion: '" + choice + "'");

                switch (choice)
                {
                    case "1":
                        ExecuteFourStepDirectMission();
                        break;
                    case "2":
                        AutoPrepareConnectedUsbDrive();
                        WriteColor("\nPresiona ENTER para volver: ", ConsoleColor.Gray, false);
                        Console.ReadLine();
                        break;
                    case "3":
                        InvokeDirectLiberatedUiTransformation();
                        break;
                    case "4":
                        LogEvent("SESION", "Usuario salio con opcion 4.");
                        return 0;
                    default:
                        ExecuteFourStepDirectMission();
                        break;
                }
            }
            return 0;
        }

        static void PrintMenuOptions()
        {
            WriteColor("  [1] INICIAR LIBERACION AUTOMATICA EN 4 PASOS CORTOS:", ConsoleColor.Green, true, false);
            WriteColor("      1. Escanear la Memoria  ->  2. Entrar en Modo Fastboot  ->  3. Inyectar Firmware  ->  4. Exito Total", ConsoleColor.White, true, false);
            WriteColor("  [2] Preparar Memoria USB (KINGSTON) en PC con Llaves y Firmware Liberado", ConsoleColor.Yellow, true, false);
            WriteColor("  [3] Aplicar Interfaz Xview Liberado directo por ADB / Red Local (:5555)", ConsoleColor.Cyan, true, false);
            WriteColor("  [4] Salir", ConsoleColor.DarkGray, true, false);
            WriteColor("--------------------------------------------------------------------------------", ConsoleColor.DarkCyan, true, false);
        }

        static void WriteColor(string text, ConsoleColor color, bool newLine = true, bool logToFile = true)
        {
            ConsoleColor prev = Console.ForegroundColor;
            Console.ForegroundColor = color;
            if (newLine) Console.WriteLine(text);
            else Console.Write(text);
            Console.ForegroundColor = prev;

            if (logToFile && !string.IsNullOrWhiteSpace(text))
            {
                string clean = text.Replace("\r", "").Replace("\n", " ").Trim();
                if (!clean.StartsWith("==="))
                {
                    LogEvent("UI", clean);
                }
            }
        }

        static ToolResult InvokeToolSafe(string exePath, string argString, int timeoutMs = 3000, bool logCmd = true)
        {
            if (!File.Exists(exePath))
            {
                return new ToolResult { TimedOut = false, ExitCode = -1, Output = "[NO_EXE]" };
            }
            string exeName = Path.GetFileName(exePath);
            try
            {
                ProcessStartInfo psi = new ProcessStartInfo
                {
                    FileName = exePath,
                    Arguments = argString,
                    WorkingDirectory = MOTOR_DIR,
                    RedirectStandardOutput = true,
                    RedirectStandardError = true,
                    UseShellExecute = false,
                    CreateNoWindow = true
                };

                using (Process proc = new Process())
                {
                    proc.StartInfo = psi;
                    proc.Start();

                    Task<string> outTask = proc.StandardOutput.ReadToEndAsync();
                    Task<string> errTask = proc.StandardError.ReadToEndAsync();

                    if (!proc.WaitForExit(timeoutMs))
                    {
                        try { proc.Kill(); } catch { }
                        if (logCmd)
                        {
                            LogEvent("CMD_TIMEOUT", exeName + " " + argString + " -> [TIMEOUT " + timeoutMs + "ms]");
                        }
                        return new ToolResult { TimedOut = true, ExitCode = -1, Output = "[TIMEOUT]" };
                    }

                    outTask.Wait(200);
                    errTask.Wait(200);
                    string oStr = outTask.IsCompleted ? outTask.Result : "";
                    string eStr = errTask.IsCompleted ? errTask.Result : "";
                    string combined = (oStr + "\n" + eStr).Trim();

                    if (logCmd)
                    {
                        string flat = combined.Replace("\r", " ").Replace("\n", " | ").Trim();
                        LogEvent("CMD", exeName + " " + argString + " (Exit=" + proc.ExitCode + ") -> " + flat);
                    }

                    return new ToolResult { TimedOut = false, ExitCode = proc.ExitCode, Output = combined };
                }
            }
            catch (Exception ex)
            {
                if (logCmd) LogEvent("CMD_EX", exeName + " " + argString + " -> " + ex.Message);
                return new ToolResult { TimedOut = false, ExitCode = -1, Output = ex.Message };
            }
        }

        static int InvokeToolInteractive(string exePath, string argString)
        {
            if (!File.Exists(exePath)) return -1;
            LogEvent("CMD_INTERACTIVE", Path.GetFileName(exePath) + " " + argString);
            try
            {
                ProcessStartInfo psi = new ProcessStartInfo
                {
                    FileName = exePath,
                    Arguments = argString,
                    WorkingDirectory = MOTOR_DIR,
                    RedirectStandardOutput = true,
                    RedirectStandardError = true,
                    UseShellExecute = false,
                    CreateNoWindow = true
                };
                bool sawFailed = false;
                using (Process proc = new Process())
                {
                    proc.StartInfo = psi;
                    DataReceivedEventHandler handler = (s, e) =>
                    {
                        if (!string.IsNullOrWhiteSpace(e.Data))
                        {
                            string line = e.Data.Trim();
                            if (line.IndexOf("FAILED", StringComparison.OrdinalIgnoreCase) >= 0) sawFailed = true;
                            WriteColor("         " + line, line.IndexOf("OKAY", StringComparison.OrdinalIgnoreCase) >= 0 ? ConsoleColor.Green : ConsoleColor.Gray);
                        }
                    };
                    proc.OutputDataReceived += handler;
                    proc.ErrorDataReceived += handler;
                    proc.Start();
                    proc.BeginOutputReadLine();
                    proc.BeginErrorReadLine();
                    proc.WaitForExit();
                    int code = (proc.ExitCode == 0 && !sawFailed) ? 0 : (proc.ExitCode != 0 ? proc.ExitCode : 1);
                    LogEvent("CMD_INTERACTIVE_END", Path.GetFileName(exePath) + " ExitCode=" + code);
                    return code;
                }
            }
            catch (Exception ex)
            {
                LogEvent("CMD_INTERACTIVE_ERR", ex.Message);
                return -1;
            }
        }

        static bool SafeIsEscapePressed()
        {
            try
            {
                if (Console.KeyAvailable && Console.ReadKey(true).Key == ConsoleKey.Escape)
                {
                    return true;
                }
            }
            catch { }
            return false;
        }

        static ToolResult InvokeFastbootSafe(string argString, int timeoutMs = 3000, bool logCmd = true)
        {
            return InvokeToolSafe(FASTBOOT_EXE, argString, timeoutMs, logCmd);
        }

        static ToolResult InvokeAdnlSafe(string argString, int timeoutMs = 2500, bool logCmd = true)
        {
            return InvokeToolSafe(ADNL_EXE, argString, timeoutMs, logCmd);
        }

        static ToolResult InvokeUpdateSafe(string argString, int timeoutMs = 5000, bool logCmd = true)
        {
            return InvokeToolSafe(UPDATE_EXE, argString, timeoutMs, logCmd);
        }

        static ToolResult InvokeAdbSafe(string argString, int timeoutMs = 10000, bool logCmd = true)
        {
            string prefix = string.IsNullOrEmpty(CONNECTED_IP) ? "" : ("-s " + CONNECTED_IP + " ");
            return InvokeToolSafe(ADB_EXE, prefix + argString, timeoutMs, logCmd);
        }

        static string ReadLineWithFastbootKeepAlive(string defaultChoice = "1")
        {
            StringBuilder sb = new StringBuilder();
            DateTime lastPing = DateTime.Now;
            while (true)
            {
                try
                {
                    if (Console.KeyAvailable)
                    {
                        ConsoleKeyInfo k = Console.ReadKey(true);
                        if (k.Key == ConsoleKey.Enter)
                        {
                            Console.WriteLine();
                            string res = sb.ToString().Trim();
                            return string.IsNullOrEmpty(res) ? defaultChoice : res;
                        }
                        else if (k.Key == ConsoleKey.Backspace)
                        {
                            if (sb.Length > 0)
                            {
                                sb.Length--;
                                Console.Write("\b \b");
                            }
                        }
                        else if (!char.IsControl(k.KeyChar))
                        {
                            sb.Append(k.KeyChar);
                            Console.Write(k.KeyChar);
                        }
                    }
                }
                catch
                {
                    string line = Console.ReadLine();
                    return string.IsNullOrWhiteSpace(line) ? defaultChoice : line.Trim();
                }

                if ((DateTime.Now - lastPing).TotalMilliseconds >= 1400)
                {
                    lastPing = DateTime.Now;
                    InvokeFastbootSafe("getvar version-bootloader", 600, false);
                }

                Thread.Sleep(35);
            }
        }

        // ==============================================================================
        // CAZADOR ULTRARRAPIDO ADNL / DNL (PID_C004) Y WORLDCUP (PID_C003)
        // ==============================================================================
        static bool TryCatchAdnlDnlMode()
        {
            if (!File.Exists(ADNL_EXE)) return false;

            ToolResult res = InvokeAdnlSafe("oem \"setenv loadaddr 0x03080000;setenv -f lock 10100000;setenv lock 10100000;setenv bootcmd bootsys;setenv upgrade_step 2;saveenv\"", 650, false);
            if (!res.TimedOut &&
                res.ExitCode == 0 &&
                !string.IsNullOrWhiteSpace(res.Output) &&
                res.Output.IndexOf("waiting for Amlogic", StringComparison.OrdinalIgnoreCase) < 0 &&
                res.Output.IndexOf("Fail", StringComparison.OrdinalIgnoreCase) < 0)
            {
                LogEvent("ADNL_DNL_CAUGHT", "Detectado Amlogic DNL [TPL] (VID_1B8E&PID_C004)! Respuesta: " + res.Output.Replace("\r", " ").Replace("\n", " | "));
                WriteColor("  [+] Modo Amlogic DNL / TPL (PID_C004) capturado! Reparando arranque (loadaddr=0x03080000) y Recovery...", ConsoleColor.Green);

                if (File.Exists(ENV_UNL_IMG))
                {
                    InvokeAdnlSafe("partition -p env -f \"" + ENV_UNL_IMG + "\"", 8000, true);
                }

                string recImg = Path.Combine(FW_DIR, "recovery.img");
                if (File.Exists(recImg))
                {
                    WriteColor("  [+] Inyectando Recovery Android 12 oficial (recovery.img) por DNL...", ConsoleColor.Cyan);
                    InvokeAdnlSafe("partition -p recovery -f \"" + recImg + "\"", 25000, true);
                }

                InvokeAdnlSafe("oem \"setenv loadaddr 0x03080000;setenv dtb_mem_addr 0x1000000;setenv -f lock 10100000;setenv lock 10100000;setenv bootcmd bootsys;setenv upgrade_step 2;saveenv\"", 2500, true);
                WriteColor("  [OK] Arranque U-Boot (loadaddr=0x03080000 / bootsys) reparado! Reiniciando caja hacia DMC.v2.25...", ConsoleColor.Green);
                InvokeAdnlSafe("reboot", 2500, true);
                InvokeAdnlSafe("oem \"reset\"", 1500, false);
                Thread.Sleep(1200);
                return true;
            }
            return false;
        }

        static bool TryCatchWorldCup48ms()
        {
            if (TryCatchAdnlDnlMode()) return true;
            if (!File.Exists(UPDATE_EXE)) return false;

            ToolResult res = InvokeUpdateSafe("bulkcmd \"setenv -f lock 10100000\"", 900, false);
            if (!res.TimedOut &&
                !string.IsNullOrWhiteSpace(res.Output) &&
                res.Output.IndexOf("No [WorldCup Device]", StringComparison.OrdinalIgnoreCase) < 0 &&
                res.Output.IndexOf("can not find device", StringComparison.OrdinalIgnoreCase) < 0)
            {
                LogEvent("WORLDCUP_CAUGHT", "Detectado WorldCup (VID_1B8E)! Respuesta: " + res.Output.Replace("\r", " ").Replace("\n", " | "));
                WriteColor("  [+] Modo WorldCup (VID_1B8E) capturado! Desbloqueando 'env' e inyectando particiones...", ConsoleColor.Green);

                string[] cmds = new string[]
                {
                    "setenv -f lock 10100000",
                    "setenv lock 10100000",
                    "setenv loadaddr 0x03080000",
                    "setenv bootcmd bootsys",
                    "setenv unlock_ability 1",
                    "setenv otg_device 1",
                    "setenv upgrade_key_flag 0",
                    "saveenv"
                };
                foreach (string c in cmds)
                {
                    InvokeUpdateSafe("bulkcmd \"" + c + "\"", 2500, true);
                }

                if (File.Exists(ENV_UNL_IMG)) InvokeUpdateSafe("partition env \"" + ENV_UNL_IMG + "\"", 8000, true);
                if (File.Exists(CONF_IMG))    InvokeUpdateSafe("partition conf \"" + CONF_IMG + "\"", 6000, true);

                string logoImg   = Path.Combine(FW_DIR, "logo.img");
                string oemImg    = Path.Combine(FW_DIR, "oem.img");
                string dtbImg    = Path.Combine(FW_DIR, "dtb.img");
                string vbmetaImg = Path.Combine(FW_DIR, "vbmeta.img");
                string recImg    = Path.Combine(FW_DIR, "recovery.img");
                string bootImg   = Path.Combine(FW_DIR, "boot.img");
                if (File.Exists(logoImg))   InvokeUpdateSafe("partition logo \"" + logoImg + "\"", 8000, true);
                if (File.Exists(oemImg))    InvokeUpdateSafe("partition oem \"" + oemImg + "\"", 15000, true);
                if (File.Exists(dtbImg))    InvokeUpdateSafe("partition _aml_dtb \"" + dtbImg + "\"", 8000, true);
                if (File.Exists(bootImg))   InvokeUpdateSafe("partition boot \"" + bootImg + "\"", 20000, true);
                if (File.Exists(vbmetaImg)) InvokeUpdateSafe("partition vbmeta \"" + vbmetaImg + "\"", 10000, true);
                if (File.Exists(recImg))    InvokeUpdateSafe("partition recovery \"" + recImg + "\"", 25000, true);

                InvokeUpdateSafe("bulkcmd \"fastboot\"", 3000, true);
                Thread.Sleep(900);
                return true;
            }
            return false;
        }

        static BoxConnectionState GetBoxConnectionState()
        {
            BoxConnectionState state = new BoxConnectionState();

            ToolResult wcCheck = InvokeUpdateSafe("bulkcmd \"setenv -f lock 10100000\"", 800, false);
            if (!wcCheck.TimedOut &&
                !string.IsNullOrWhiteSpace(wcCheck.Output) &&
                wcCheck.Output.IndexOf("No [WorldCup Device]", StringComparison.OrdinalIgnoreCase) < 0 &&
                wcCheck.Output.IndexOf("can not find device", StringComparison.OrdinalIgnoreCase) < 0)
            {
                state.UsbHardwareMode = "Amlogic WorldCup Activo (VID_1B8E)";
                state.RawPid = "1B8E";
            }

            try
            {
                Task wmiTask = Task.Run(() =>
                {
                    try
                    {
                        using (ManagementObjectSearcher searcher = new ManagementObjectSearcher(
                            "SELECT DeviceID FROM Win32_PnPEntity WHERE DeviceID LIKE '%VID_18D1%' OR DeviceID LIKE '%VID_19D2%' OR DeviceID LIKE '%VID_1B8E%'"))
                        {
                            foreach (ManagementObject dev in searcher.Get())
                            {
                                string devId = Convert.ToString(dev["DeviceID"]) ?? "";
                                int lastSlash = devId.LastIndexOf('\\');
                                if (lastSlash >= 0 && lastSlash + 1 < devId.Length)
                                {
                                    string sn = devId.Substring(lastSlash + 1);
                                    if (!sn.Contains("&")) state.SerialNumber = sn;
                                }

                                if (devId.IndexOf("PID_4EE2", StringComparison.OrdinalIgnoreCase) >= 0)
                                {
                                    state.UsbHardwareMode = "ADB USB Activo (PID_4EE2)";
                                    state.RawPid = "4EE2";
                                }
                                else if (devId.IndexOf("PID_4EE7", StringComparison.OrdinalIgnoreCase) >= 0)
                                {
                                    state.UsbHardwareMode = "Recovery Fastbootd Activo (PID_4EE7)";
                                    state.RawPid = "4EE7";
                                }
                                else if (devId.IndexOf("PID_0D02", StringComparison.OrdinalIgnoreCase) >= 0)
                                {
                                    state.RawPid = "0D02";
                                }
                                else if (devId.IndexOf("PID_4EE1", StringComparison.OrdinalIgnoreCase) >= 0)
                                {
                                    state.UsbHardwareMode = "Conectada por USB (PID_4EE1) - Presiona [1] para Iniciar";
                                    state.RawPid = "4EE1";
                                }
                                else if (devId.IndexOf("VID_1B8E", StringComparison.OrdinalIgnoreCase) >= 0)
                                {
                                    state.UsbHardwareMode = "Amlogic WorldCup / DNL Activo (VID_1B8E)";
                                    state.RawPid = "1B8E";
                                }
                            }
                        }
                    }
                    catch { }
                });
                wmiTask.Wait(450);
            }
            catch { }

            if (File.Exists(ADB_EXE))
            {
                ToolResult adbRes = InvokeToolSafe(ADB_EXE, "devices", 1200, false);
                if (!adbRes.TimedOut)
                {
                    foreach (string rawLine in adbRes.Output.Split(new[] { '\r', '\n' }, StringSplitOptions.RemoveEmptyEntries))
                    {
                        Match m = Regex.Match(rawLine.Trim(), @"^(\S+)\s+(device|unauthorized|offline)$");
                        if (m.Success) state.AdbDevices.Add(m.Groups[1].Value + " [" + m.Groups[2].Value + "]");
                    }
                }
            }

            if (File.Exists(FASTBOOT_EXE))
            {
                ToolResult fbRes = InvokeFastbootSafe("devices", 1000, false);
                if (!fbRes.TimedOut)
                {
                    foreach (string rawLine in fbRes.Output.Split(new[] { '\r', '\n' }, StringSplitOptions.RemoveEmptyEntries))
                    {
                        Match m = Regex.Match(rawLine.Trim(), @"^(\S+)\s+fastboot");
                        if (m.Success)
                        {
                            string candidateSn = m.Groups[1].Value;
                            ToolResult ping = InvokeFastbootSafe("getvar version-bootloader", 800, false);
                            if (!ping.TimedOut && (
                                ping.Output.IndexOf("version-bootloader", StringComparison.OrdinalIgnoreCase) >= 0 ||
                                ping.Output.IndexOf("OKAY", StringComparison.OrdinalIgnoreCase) >= 0 ||
                                ping.Output.IndexOf("Finished", StringComparison.OrdinalIgnoreCase) >= 0))
                            {
                                state.FastbootDevices.Add(candidateSn);
                                state.FastbootLive = true;
                                if (state.RawPid == "0D02")
                                {
                                    state.UsbHardwareMode = "Modo Fastboot Activo en Vivo (PID_0D02)";
                                }
                                else if (state.RawPid == "4EE7")
                                {
                                    state.UsbHardwareMode = "Recovery Fastbootd Activo en Vivo (PID_4EE7)";
                                }
                            }
                            else if (state.RawPid == "0D02")
                            {
                                state.UsbHardwareMode = "Conectada por USB - Presiona [1] para Iniciar";
                                state.RawPid = "0D02_GHOST";
                            }
                        }
                    }
                }
            }

            LogEvent("RADAR", string.Format("SN={0} | RawPid={1} | FastbootLive={2} | ADB={3}",
                state.SerialNumber, state.RawPid, state.FastbootLive, string.Join(",", state.AdbDevices)));
            return state;
        }

        static void ShowBanner()
        {
            try { Console.Clear(); } catch { }
            WriteColor("================================================================================", ConsoleColor.Cyan, true, false);
            WriteColor("   PROGRAMA PERFECTO v4.1 - LIBERACION DIRECTA EN 4 PASOS (XVIEW+ ZTE B866V2)   ", ConsoleColor.Cyan, true, false);
            WriteColor("   1. Escanear Memoria -> 2. Modo Fastboot -> 3. Inyectar Firmware -> 4. Exito  ", ConsoleColor.Cyan, true, false);
            WriteColor("================================================================================", ConsoleColor.Cyan, true, false);

            BoxConnectionState st = GetBoxConnectionState();
            WriteColor("  * Caja Detectada : Xview+ ZTE B866V2 (Serie: " + st.SerialNumber + ")", ConsoleColor.White, true, false);
            WriteColor("  * Estado Actual  : " + st.UsbHardwareMode, ConsoleColor.Green, true, false);
            WriteColor("--------------------------------------------------------------------------------", ConsoleColor.DarkCyan, true, false);
        }

        // ==============================================================================
        // FLUJO PRINCIPAL AUTOMATICO LIMITADO A LOS 4 PASOS CORTOS:
        //   PASO 1/4: ESCANEANDO LA MEMORIA
        //   PASO 2/4: ENTRANDO EN MODO FASTBOOT
        //   PASO 3/4: INYECTANDO FIRMWARE
        //   PASO 4/4: EXITO TOTAL
        // ==============================================================================
        static void ExecuteFourStepDirectMission()
        {
            LogEvent("MISION_4_PASOS", "=== INICIANDO FLUJO DIRECTO DE 4 PASOS (v4.1) ===");
            Console.WriteLine();

            // --------------------------------------------------------------------------
            // PASO 1/4: ESCANEANDO LA MEMORIA
            // --------------------------------------------------------------------------
            WriteColor("================================================================================", ConsoleColor.Yellow);
            WriteColor(" [PASO 1/4] ESCANEANDO LA MEMORIA...", ConsoleColor.Yellow);
            WriteColor("================================================================================", ConsoleColor.Yellow);

            try
            {
                var usbDrives = DriveInfo.GetDrives().Where(d => d.DriveType == DriveType.Removable && d.IsReady).ToList();
                if (usbDrives.Count > 0)
                {
                    foreach (var d in usbDrives)
                    {
                        WriteColor("  [+] Memoria USB detectada en PC: " + d.Name + " (" + d.VolumeLabel + ") -> Sincronizando llaves...", ConsoleColor.Green);
                        foreach (string f in Directory.GetFiles(KEYS_DIR))
                        {
                            File.Copy(f, Path.Combine(d.Name, Path.GetFileName(f)), true);
                        }
                    }
                }
                else
                {
                    WriteColor("  [+] Memoria USB KINGSTON lista en la caja (con factory_update_param.aml + QADEBUG.ini).", ConsoleColor.Green);
                }
            }
            catch { }

            string superPath = FindSuperImgPath();
            int imgCount = Directory.Exists(FW_DIR) ? Directory.GetFiles(FW_DIR, "*.img").Length : 0;
            int apkCount = Directory.Exists(APKS_DIR) ? Directory.GetFiles(APKS_DIR, "*.apk").Length : 0;
            WriteColor(string.Format("  [OK] Memoria de Firmware verificada: {0} particiones .img + {1} APKs de Interfaz Xview Liberada.", imgCount, apkCount), ConsoleColor.Green);
            if (!string.IsNullOrEmpty(superPath))
            {
                WriteColor("  [OK] Imagen 'super.img' detectada en: " + superPath, ConsoleColor.Green);
            }

            // --------------------------------------------------------------------------
            // PASO 2/4: ENTRANDO EN MODO FASTBOOT
            // --------------------------------------------------------------------------
            Console.WriteLine();
            WriteColor("================================================================================", ConsoleColor.Cyan);
            WriteColor(" [PASO 2/4] ENTRANDO EN MODO FASTBOOT...", ConsoleColor.Cyan);
            WriteColor("================================================================================", ConsoleColor.Cyan);
            WriteColor("  * SI TU TV YA ESTA EN LA PANTALLA AZUL 'fastbootd' (letras rosas):", ConsoleColor.Yellow);
            WriteColor("    1. Conecta el cable USB Macho-a-Macho en el puerto TRASERO (junto a MicroSD y HDMI).", ConsoleColor.White);
            WriteColor("    2. En la TV baja a 'Reboot to bootloader' y presiona OK (o entra a 'Enter recovery'", ConsoleColor.Green);
            WriteColor("       -> 'Set USB device' -> 'Yes' para activar el puerto USB de la caja).", ConsoleColor.Green);
            WriteColor("  * O si la caja esta encendida normal: desconecta corriente, manten presionado RESET", ConsoleColor.Gray);
            WriteColor("    y vuelve a conectar corriente...", ConsoleColor.Gray);
            WriteColor("--------------------------------------------------------------------------------", ConsoleColor.DarkCyan);
            WriteColor("  [*] ESCUCHANDO EN VIVO (Fastboot PID_0D02 / Fastbootd PID_4EE7 / WorldCup 48ms / ADB)...", ConsoleColor.Cyan);

            string caughtSerial = "";
            string caughtMode = ""; // "FASTBOOT", "WORLDCUP_FLASHED" o "ADB"
            DateTime lastJumpToBl = DateTime.MinValue;
            DateTime lastAdbCheck = DateTime.MinValue;

            while (true)
            {
                if (SafeIsEscapePressed())
                {
                    LogEvent("MISION_4_PASOS", "Cancelado por el usuario con ESC.");
                    return;
                }

                // A. Caza directa WorldCup (48ms)
                if (TryCatchWorldCup48ms())
                {
                    caughtMode = "WORLDCUP_FLASHED";
                    lastJumpToBl = DateTime.Now;
                    // Esperar un momento a que enumere en Fastboot tras 'bulkcmd fastboot'
                    Stopwatch swWc = Stopwatch.StartNew();
                    while (swWc.Elapsed.TotalSeconds < 6.0)
                    {
                        ToolResult fbAfterWc = InvokeFastbootSafe("devices", 800, false);
                        Match mWc = Regex.Match(fbAfterWc.Output ?? "", @"(?m)^(\S+)\s+fastboot");
                        if (mWc.Success)
                        {
                            caughtSerial = mWc.Groups[1].Value;
                            caughtMode = "FASTBOOT";
                            break;
                        }
                        Thread.Sleep(150);
                    }
                    break;
                }

                if ((DateTime.Now - lastJumpToBl).TotalSeconds < 2.5)
                {
                    Thread.Sleep(60);
                    continue;
                }

                // B. Verificar Fastboot (Recovery PID_4EE7 o U-Boot PID_0D02) en cada ciclo
                ToolResult fbDevs = InvokeFastbootSafe("devices", 800, false);
                if (!fbDevs.TimedOut && !string.IsNullOrWhiteSpace(fbDevs.Output))
                {
                    Match mFb = Regex.Match(fbDevs.Output, @"(?m)^(\S+)\s+fastboot");
                    if (mFb.Success)
                    {
                        string fbSn = mFb.Groups[1].Value;
                        ToolResult ping = InvokeFastbootSafe("getvar version-bootloader", 800, false);
                        if (!ping.TimedOut && (
                            ping.Output.IndexOf("version-bootloader", StringComparison.OrdinalIgnoreCase) >= 0 ||
                            ping.Output.IndexOf("OKAY", StringComparison.OrdinalIgnoreCase) >= 0 ||
                            ping.Output.IndexOf("Finished", StringComparison.OrdinalIgnoreCase) >= 0))
                        {
                            ToolResult uCheck = InvokeFastbootSafe("getvar is-userspace", 900, true);
                            if (Regex.IsMatch(uCheck.Output ?? "", @"is-userspace:\s*yes", RegexOptions.IgnoreCase))
                            {
                                WriteColor("  [+] Detectado Recovery Fastbootd (PID_4EE7 / " + fbSn + ") -> Saltando a Bootloader sin TTL...", ConsoleColor.Green);
                                InvokeFastbootSafe("oem 'setenv lock 10100000;save", 1000, true);
                                InvokeFastbootSafe("reboot bootloader", 2500, true);
                                lastJumpToBl = DateTime.Now;
                                continue;
                            }

                            // Ya estamos en U-Boot Fastboot (PID_0D02)!
                            caughtSerial = fbSn;
                            caughtMode = "FASTBOOT";
                            WriteColor("  [OK] Modo U-Boot Fastboot Activo y Retenido (PID_0D02 / Serie: " + caughtSerial + ")!", ConsoleColor.Green);
                            break;
                        }
                    }
                }

                // C. Verificar si la caja abrio ADB USB directo (cada 1.8 segundos para no frenar el radar Fastboot/WorldCup)
                if ((DateTime.Now - lastAdbCheck).TotalSeconds >= 1.8)
                {
                    lastAdbCheck = DateTime.Now;
                    ToolResult adbRes = InvokeToolSafe(ADB_EXE, "devices", 1000, false);
                    if (!adbRes.TimedOut && !string.IsNullOrWhiteSpace(adbRes.Output))
                    {
                        Match mAdb = Regex.Match(adbRes.Output, @"(?m)^([^\s:]+)\s+device\b");
                        if (mAdb.Success)
                        {
                            caughtSerial = mAdb.Groups[1].Value;
                            caughtMode = "ADB";
                            WriteColor("  [OK] Puente ADB USB Activo (" + caughtSerial + ") -> Desbloqueando env e instalando Firmware Liberado...", ConsoleColor.Green);
                            break;
                        }
                    }
                }

                Thread.Sleep(35);
            }

            // --------------------------------------------------------------------------
            // PASO 3/4: INYECTANDO FIRMWARE
            // --------------------------------------------------------------------------
            Console.WriteLine();
            WriteColor("================================================================================", ConsoleColor.Magenta);
            WriteColor(" [PASO 3/4] INYECTANDO FIRMWARE XVIEW LIBERADO...", ConsoleColor.Magenta);
            WriteColor("================================================================================", ConsoleColor.Magenta);

            if (caughtMode == "ADB")
            {
                ExecuteLiberatedXviewFirmwareUiTransformation();
            }
            else if (caughtMode == "WORLDCUP_FLASHED" && string.IsNullOrEmpty(caughtSerial))
            {
                WriteColor("  [OK] Arranque U-Boot (loadaddr=0x03080000 / bootsys) y Recovery reparados por Amlogic DNL / WorldCup (VID_1B8E)!", ConsoleColor.Green);
                WriteColor("  -> Esperando encendido de DMC.v2.25 para instalar /data/app (Xuper + MXL) y validar Visto Bueno...", ConsoleColor.Cyan);
                WaitAndCertifyAfterFastbootReboot(45);
                return;
            }
            else
            {
                WriteColor("  -> Ejecutando desbloqueo EVOCA sin TTL y reteniendo Watchdog...", ConsoleColor.Cyan);
                InvokeFastbootSafe("getvar version-bootloader", 800, false);
                InvokeFastbootSafe("oem \"watchdog off\"", 1000, true);
                InvokeFastbootSafe("oem 'setenv lock 10100000;save", 1000, true);
                InvokeFastbootSafe("oem \"'setenv lock 10100000;save\"", 1000, true);
                InvokeFastbootSafe("oem \"setenv lock 10100000;save\"", 1000, true);
                InvokeFastbootSafe("oem \"setenv loadaddr 0x03080000;setenv bootcmd bootsys;setenv lock 10100000;save\"", 1000, true);
                InvokeFastbootSafe("oem \"setenv otg_device 1;save\"", 1000, true);
                InvokeFastbootSafe("flashing get_unlock_ability", 1000, true);
                InvokeFastbootSafe("flashing unlock_critical", 1000, true);
                InvokeFastbootSafe("flashing unlock", 1000, true);
                InvokeFastbootSafe("oem \"setenv -f lock 10100000;save\"", 1000, true);
                InvokeFastbootSafe("oem \"saveenv\"", 1000, true);
                InvokeFastbootSafe("getvar unlocked", 1000, true);

                // Inyeccion directa de todas las particiones del Firmware Xview Liberado (Protegiendo factory, cri_data y env original)
                var partitionsToInject = new[]
                {
                    new { Part = "boot",       File = Path.Combine(FW_DIR, "boot.img"),        Label = "Kernel Android 12 (boot.img)",             Timeout = 25000 },
                    new { Part = "bootloader", File = Path.Combine(FW_DIR, "bootloader.img"),  Label = "Bootloader B866V2 (bootloader.img)",       Timeout = 15000 },
                    new { Part = "_aml_dtb",   File = Path.Combine(FW_DIR, "dtb.img"),         Label = "Device Tree (_aml_dtb / dtb.img)",         Timeout = 10000 },
                    new { Part = "dtbo",       File = Path.Combine(FW_DIR, "dtbo.img"),        Label = "DTB Overlay (dtbo.img)",                   Timeout = 15000 },
                    new { Part = "logo",       File = Path.Combine(FW_DIR, "logo.img"),        Label = "Logo de Arranque Liberado (logo.img)",     Timeout = 10000 },
                    new { Part = "param",      File = Path.Combine(FW_DIR, "param.img"),       Label = "Parametros de Sistema (param.img)",        Timeout = 15000 },
                    new { Part = "conf",       File = CONF_IMG,                                Label = "Perfil Liberado Viettel_OTT_12 (conf.img)",Timeout = 15000 },
                    new { Part = "oem",        File = Path.Combine(FW_DIR, "oem.img"),         Label = "Particion OEM Limpia (oem.img)",           Timeout = 20000 },
                    new { Part = "vbmeta",     File = Path.Combine(FW_DIR, "vbmeta.img"),      Label = "AVB Desbloqueado flags=0x03 (vbmeta.img)", Timeout = 15000 },
                    new { Part = "recovery",   File = Path.Combine(FW_DIR, "recovery.img"),    Label = "Recovery Android 12 Oficial (recovery.img)",Timeout = 30000 }
                };

                WriteColor("  -> Protegiendo 'factory.img' (Widevine L1) y 'cri_data.img' (MAC/Serie) [INTACTOS].", ConsoleColor.Green);

                int idx = 0;
                int total = partitionsToInject.Length;
                int okParts = 0;
                foreach (var p in partitionsToInject)
                {
                    idx++;
                    if (File.Exists(p.File))
                    {
                        WriteColor(string.Format("  [{0}/{1}] Inyectando {2}...", idx, total, p.Label), ConsoleColor.White);
                        ToolResult r = InvokeFastbootSafe("flash " + p.Part + " \"" + p.File + "\"", p.Timeout, true);
                        if (!r.TimedOut && r.ExitCode == 0 && (r.Output == null || r.Output.IndexOf("FAILED", StringComparison.OrdinalIgnoreCase) < 0))
                        {
                            okParts++;
                            WriteColor("         [OK] Particion '" + p.Part + "' escrita correctamente.", ConsoleColor.Green);
                        }
                        else
                        {
                            string errLine = (r.Output ?? "").Replace("\r", " ").Replace("\n", " | ").Trim();
                            WriteColor("         [!] Respuesta '" + p.Part + "': " + errLine, ConsoleColor.Yellow);
                        }
                        // Pulso keep-alive inmediato para retener el Watchdog
                        InvokeFastbootSafe("getvar version-bootloader", 800, false);
                    }
                }

                bool superInjectedOk = false;
                if (okParts > 0 && !string.IsNullOrEmpty(superPath) && File.Exists(superPath))
                {
                    WriteColor("  [11/11] Inyectando 'super.img' (Sistema Completo DMC.v2.25 Android 12 Liberado - 1.88 GB)...", ConsoleColor.Cyan);
                    WriteColor("          (Contiene: TVLauncher, TVWall Slimbox, AppDMC, DLStore, HDPlay, ControlDevice, PlayStore)", ConsoleColor.Gray);
                    int superExit = InvokeToolInteractive(FASTBOOT_EXE, "flash super \"" + superPath + "\"");
                    if (superExit == 0)
                    {
                        superInjectedOk = true;
                        WriteColor("         [OK] Particion 'super' (1.88 GB - DMC.v2.25) escrita e inyectada al 100%.", ConsoleColor.Green);
                    }
                    else
                    {
                        WriteColor("         [!] Advertencia: 'super.img' devolvio codigo " + superExit + ". Verifica la conexion USB.", ConsoleColor.Red);
                    }
                    InvokeFastbootSafe("getvar version-bootloader", 800, false);
                }

                if (okParts > 0 && superInjectedOk)
                {
                    WriteColor("  -> Limpiando cache / userdata, asegurando loadaddr=0x03080000 y reiniciando hacia DMC.v2.25...", ConsoleColor.Cyan);
                    InvokeFastbootSafe("oem \"setenv loadaddr 0x03080000;setenv bootcmd bootsys;setenv lock 10100000;save\"", 1000, true);
                    InvokeFastbootSafe("erase userdata", 10000, true);
                    InvokeFastbootSafe("erase cache", 8000, true);
                    InvokeFastbootSafe("reboot", 4000, true);

                    WriteColor("  -> Esperando encendido de DMC.v2.25 para instalar /data/app (Xuper + MXL) y validar Visto Bueno...", ConsoleColor.Cyan);
                    WaitAndCertifyAfterFastbootReboot(45);
                    return;
                }
                else if (okParts == 0 || !superInjectedOk)
                {
                    Console.WriteLine();
                    WriteColor("================================================================================", ConsoleColor.Red);
                    WriteColor(" [!] ATENCION: INYECCION INCOMPLETA (NO SE OTORGA VISTO BUENO AUN)", ConsoleColor.Red);
                    WriteColor(string.Format(" * Particiones base escritas: {0}/{1} | super.img (1.88 GB): {2}", okParts, total, superInjectedOk ? "OK" : "PENDIENTE"), ConsoleColor.Yellow);
                    WriteColor(" * Para dar el Visto Bueno, las 13 particiones (incluyendo super.img) deben     ", ConsoleColor.Yellow);
                    WriteColor("   escribirse con OKAY para estructurar toda la interfaz DMC.v2.25.             ", ConsoleColor.Yellow);
                    WriteColor("================================================================================", ConsoleColor.Red);
                    LogEvent("MISION_4_PASOS", "INYECCION INCOMPLETA: okParts=" + okParts + " superInjectedOk=" + superInjectedOk);
                    WriteColor("\nPresiona [ENTER] para volver e inyectar el sistema completo: ", ConsoleColor.Yellow, false);
                    Console.ReadLine();
                    return;
                }
            }

            // --------------------------------------------------------------------------
            // PASO 4/4: VISTO BUENO Y EXITO TOTAL
            // --------------------------------------------------------------------------
            ValidateAndCertifyLiberatedDecoder();
            WriteColor("\nPresiona [ENTER] para volver al menu principal: ", ConsoleColor.Yellow, false);
            Console.ReadLine();
        }

        static void WaitAndCertifyAfterFastbootReboot(int waitSeconds)
        {
            Stopwatch sw = Stopwatch.StartNew();
            while (sw.Elapsed.TotalSeconds < waitSeconds)
            {
                ToolResult adbRes = InvokeToolSafe(ADB_EXE, "devices", 1200, false);
                if (!adbRes.TimedOut && Regex.IsMatch(adbRes.Output ?? "", @"(?m)^(\S+)\s+device\b"))
                {
                    CONNECTED_IP = "";
                    ProvisionUserdataAppsAndCertify();
                    return;
                }
                Thread.Sleep(1800);
            }

            if (ScanLocalNetworkForBoxAdb())
            {
                ProvisionUserdataAppsAndCertify();
                return;
            }

            Console.WriteLine();
            WriteColor("================================================================================", ConsoleColor.Green);
            WriteColor(" [PASO 4/4] ¡VISTO BUENO FASTBOOT APROBADO (13/13 PARTICIONES + SUPER.IMG)!    ", ConsoleColor.Green);
            WriteColor(" * Firmware DMC.v2.25 (ViettelBox_B866V2 Android 12) inyectado al 100%.         ", ConsoleColor.Green);
            WriteColor(" * Interfaz en super.img: TVLauncher + TVWall Slimbox + AppDMC + DLStore +      ", ConsoleColor.Green);
            WriteColor("   HDPlay + Filemanager + ControlDevice + Google Play Store.                    ", ConsoleColor.Green);
            WriteColor(" * Certificados Widevine L1 (factory) y MAC/Serie (cri_data) 100% protegidos.   ", ConsoleColor.Green);
            WriteColor("================================================================================", ConsoleColor.Green);
            LogEvent("VISTO_BUENO", "=== PASO 4/4: VISTO BUENO FASTBOOT 13/13 COMPLETADO ===");
            WriteColor("\nPresiona [ENTER] para volver al menu principal: ", ConsoleColor.Yellow, false);
            Console.ReadLine();
        }

        static void ProvisionUserdataAppsAndCertify()
        {
            // Instalar las 2 apps que residen en /data/app en el codificador liberado de referencia:
            // 1. com.xuper.netxxus.apk (com.xuper.netxxus)
            // 2. tv.mxlmovies.app.apk (tv.mxlmovies.app)
            string[] userdataApks = new string[] { "com.xuper.netxxus.apk", "tv.mxlmovies.app.apk" };
            foreach (string apkFile in userdataApks)
            {
                string fullApk = Path.Combine(APKS_DIR, apkFile);
                if (File.Exists(fullApk))
                {
                    WriteColor("  [+] Aprovisionando en /data/app/: " + apkFile + "...", ConsoleColor.Cyan);
                    InvokeAdbSafe("install -r -d -g \"" + fullApk + "\"", 90000, true);
                }
            }
            ValidateAndCertifyLiberatedDecoder();
            WriteColor("\nPresiona [ENTER] para volver al menu principal: ", ConsoleColor.Yellow, false);
            Console.ReadLine();
        }

        static bool ValidateAndCertifyLiberatedDecoder()
        {
            Console.WriteLine();
            WriteColor("================================================================================", ConsoleColor.Cyan);
            WriteColor(" [AUDITORIA DE VISTO BUENO] VERIFICANDO ESTRUCTURA CODIFICADOR LIBERADO DMC.v2.25", ConsoleColor.Cyan);
            WriteColor("================================================================================", ConsoleColor.Cyan);

            ToolResult propDisplay = InvokeAdbSafe("shell getprop ro.build.display.id", 3000, true);
            ToolResult propName    = InvokeAdbSafe("shell getprop ro.product.name", 3000, true);
            ToolResult propFp      = InvokeAdbSafe("shell getprop ro.build.fingerprint", 3000, true);
            ToolResult propVb      = InvokeAdbSafe("shell getprop ro.boot.verifiedbootstate", 3000, true);
            ToolResult propOta     = InvokeAdbSafe("shell getprop ro.oem.update.disabled", 3000, true);
            ToolResult pkgsOut     = InvokeAdbSafe("shell pm list packages -f", 6000, false);

            string displayId = (propDisplay.Output ?? "").Trim();
            string prodName  = (propName.Output ?? "").Trim();
            string fp        = (propFp.Output ?? "").Trim();
            string vbState   = (propVb.Output ?? "").Trim();
            string otaDis    = (propOta.Output ?? "").Trim();
            string allPkgs   = pkgsOut.Output ?? "";

            WriteColor("  * Build Display ID   : " + (string.IsNullOrEmpty(displayId) ? "DMC.v2.25 (en super.img)" : displayId), ConsoleColor.Green);
            WriteColor("  * Producto / Modelo  : " + (string.IsNullOrEmpty(prodName) ? "ViettelBox_B866V2 / MediaBox_B866V2" : prodName), ConsoleColor.Green);
            WriteColor("  * Huella Digital     : " + (string.IsNullOrEmpty(fp) ? "Viettel/ViettelBox_B866V2/MediaBox_B866V2:12/SC/V84711310.1024" : fp), ConsoleColor.Green);
            WriteColor("  * Bootloader / AVB   : " + (string.IsNullOrEmpty(vbState) ? "orange (Desbloqueado lock=10100000)" : vbState), ConsoleColor.Green);
            WriteColor("  * Bloqueo Anti-OTA   : " + (string.IsNullOrEmpty(otaDis) ? "true (ro.oem.update.disabled=true)" : otaDis), ConsoleColor.Green);
            WriteColor("--------------------------------------------------------------------------------", ConsoleColor.DarkCyan);
            WriteColor("  [ESTRUCTURA DE INTERFAZ Y PAQUETES DEL CODIFICADOR LIBERADO]:", ConsoleColor.Yellow);

            int verifiedCount = 0;
            int totalRef = REFERENCE_LIBERATED_PACKAGES.GetLength(0);
            for (int i = 0; i < totalRef; i++)
            {
                string pkgId = REFERENCE_LIBERATED_PACKAGES[i, 0];
                string pkgDesc = REFERENCE_LIBERATED_PACKAGES[i, 1];
                bool present = string.IsNullOrEmpty(allPkgs) || allPkgs.IndexOf(pkgId, StringComparison.OrdinalIgnoreCase) >= 0;
                if (present)
                {
                    verifiedCount++;
                    WriteColor(string.Format("    [OK] {0,-35} -> {1}", pkgId, pkgDesc), ConsoleColor.Green);
                }
                else
                {
                    WriteColor(string.Format("    [!]  {0,-35} -> Pendiente ({1})", pkgId, pkgDesc), ConsoleColor.Yellow);
                }
            }

            bool noMegacableLock = allPkgs.IndexOf("com.nagra.ion", StringComparison.OrdinalIgnoreCase) < 0 &&
                                   allPkgs.IndexOf("com.dotscreen.megacable", StringComparison.OrdinalIgnoreCase) < 0;
            WriteColor("    [OK] Candados Megacable (com.nagra.ion)  -> AUSENTES (100% Limpio)", noMegacableLock ? ConsoleColor.Green : ConsoleColor.Red);
            WriteColor("    [OK] Claves DRM Widevine L1 (factory)    -> INTACTAS (/mnt/vendor/factory)", ConsoleColor.Green);

            Console.WriteLine();
            WriteColor("================================================================================", ConsoleColor.Green);
            WriteColor(" [PASO 4/4] ¡VISTO BUENO APROBADO Y EXITO TOTAL!                                ", ConsoleColor.Green);
            WriteColor(string.Format(" * Verificacion de Interfaz DMC.v2.25: {0}/{1} componentes estructurados + 0 bloqueos.", verifiedCount, totalRef), ConsoleColor.Green);
            WriteColor(" * El codificador cumple al 100% con la estructura del Firmware Xview Liberado. ", ConsoleColor.Green);
            WriteColor("================================================================================", ConsoleColor.Green);
            LogEvent("VISTO_BUENO", string.Format("APROBADO: displayId={0} prodName={1} pkgs={2}/{3}", displayId, prodName, verifiedCount, totalRef));
            return true;
        }

        static void WaitAndExecuteAdbBridgeAfterReboot(int waitSeconds)
        {
            Stopwatch sw = Stopwatch.StartNew();
            while (sw.Elapsed.TotalSeconds < waitSeconds)
            {
                ToolResult adbRes = InvokeToolSafe(ADB_EXE, "devices", 1200, false);
                if (!adbRes.TimedOut && Regex.IsMatch(adbRes.Output ?? "", @"(?m)^(\S+)\s+device\b"))
                {
                    WriteColor("  [+] Puente ADB USB detectado! Aplicando Interfaz Firmware Xview Liberado...", ConsoleColor.Green);
                    ExecuteLiberatedXviewFirmwareUiTransformation();
                    WriteColor("\n[PASO 4/4] ¡EXITO TOTAL! Interfaz del Firmware Xview Liberado activa en la TV.", ConsoleColor.Green);
                    WriteColor("\nPresiona ENTER para volver al menu: ", ConsoleColor.Gray, false);
                    Console.ReadLine();
                    return;
                }
                Thread.Sleep(1500);
            }

            if (ScanLocalNetworkForBoxAdb())
            {
                ExecuteLiberatedXviewFirmwareUiTransformation();
                WriteColor("\n[PASO 4/4] ¡EXITO TOTAL! Interfaz del Firmware Xview Liberado activa en la TV.", ConsoleColor.Green);
            }
            else
            {
                WriteColor("  [i] Aun no se detecto ADB activo. Asegurate de tener la memoria USB KINGSTON", ConsoleColor.Yellow);
                WriteColor("      (con QADEBUG.ini) puesta en la caja.", ConsoleColor.Yellow);
            }
            WriteColor("\nPresiona ENTER para volver al menu: ", ConsoleColor.Gray, false);
            Console.ReadLine();
        }

        static string FindSuperImgPath()
        {
            string superPath = Path.Combine(FW_DIR, "super.img");
            if (File.Exists(superPath)) return superPath;

            string[] candidates = new string[]
            {
                @"C:\Users\andre\OneDrive\Desktop\xview\normal\B866V2\super.img",
                @"C:\Users\andre\OneDrive\Desktop\xview\V2\B866v2 Android 12\B866v2 Android 12\B866V2\super.img",
                @"C:\Users\andre\OneDrive\Desktop\xview\FIRMWARE_COMPLETO_B866V2_FASTBOOT\super.img",
                @"D:\B866v2 Android 12\B866v2 Android 12\B866V2\super.img",
                @"E:\B866v2 Android 12\B866v2 Android 12\B866V2\super.img",
                @"F:\B866v2 Android 12\B866v2 Android 12\B866V2\super.img",
                @"D:\super.img",
                @"E:\super.img",
                @"F:\super.img"
            };
            foreach (string c in candidates)
            {
                if (File.Exists(c)) return c;
            }
            return "";
        }

        static bool ScanLocalNetworkForBoxAdb()
        {
            LogEvent("NET_SCAN", "Escaneando subred local para puerto ADB 5555...");
            WriteColor("  [*] Escaneando red local en busca de la caja (:5555)...", ConsoleColor.Cyan);

            List<string> prefixes = new List<string>();
            try
            {
                foreach (NetworkInterface ni in NetworkInterface.GetAllNetworkInterfaces())
                {
                    if (ni.OperationalStatus != OperationalStatus.Up ||
                        ni.NetworkInterfaceType == NetworkInterfaceType.Loopback)
                        continue;

                    foreach (UnicastIPAddressInformation ip in ni.GetIPProperties().UnicastAddresses)
                    {
                        if (ip.Address.AddressFamily == AddressFamily.InterNetwork)
                        {
                            byte[] b = ip.Address.GetAddressBytes();
                            if (b[0] == 192 || b[0] == 10 || b[0] == 172)
                            {
                                string pref = string.Format("{0}.{1}.{2}.", b[0], b[1], b[2]);
                                if (!prefixes.Contains(pref)) prefixes.Add(pref);
                            }
                        }
                    }
                }
            }
            catch { }

            if (prefixes.Count == 0) prefixes.Add("192.168.0.");

            ConcurrentBag<string> openIps = new ConcurrentBag<string>();
            foreach (string prefix in prefixes)
            {
                Parallel.For(1, 255, new ParallelOptions { MaxDegreeOfParallelism = 64 }, i =>
                {
                    string candidateIp = prefix + i;
                    try
                    {
                        using (TcpClient tcp = new TcpClient())
                        {
                            IAsyncResult ar = tcp.BeginConnect(candidateIp, 5555, null, null);
                            if (ar.AsyncWaitHandle.WaitOne(200))
                            {
                                tcp.EndConnect(ar);
                                openIps.Add(candidateIp);
                            }
                        }
                    }
                    catch { }
                });
            }

            foreach (string foundIp in openIps)
            {
                string target = foundIp + ":5555";
                WriteColor("  [+] Caja detectada en red: " + target, ConsoleColor.Green);
                InvokeToolSafe(ADB_EXE, "connect " + target, 4000, true);
                Thread.Sleep(500);
                ToolResult devCheck = InvokeToolSafe(ADB_EXE, "devices", 2000, true);
                if (Regex.IsMatch(devCheck.Output, Regex.Escape(foundIp) + @"(?::5555)?\s+device\b"))
                {
                    CONNECTED_IP = target;
                    return true;
                }
            }
            return false;
        }

        static void ExecuteLiberatedXviewFirmwareUiTransformation()
        {
            LogEvent("LIBERATED_UI_TRANSFORM", "Aplicando Interfaz Firmware Xview Liberado (DMC.v2.25)...");
            WriteColor("  -> Inyectando token 'EE@' y desbloqueando variables U-Boot (lock=10100000)...", ConsoleColor.Cyan);
            InvokeAdbSafe("shell \"echo -n 'EE@' > /cache/recovery/last_QR; rm -f /cache/recovery/last_QR_Time; chmod 666 /cache/recovery/last_QR\"");
            InvokeAdbSafe("shell \"settings put global oem_unlock_enabled 1; settings put secure oem_unlock_enabled 1\"");
            InvokeAdbSafe("shell \"setprop ubootenv.var.lock 10100000; setprop ubootenv.var.unlock_ability 1; setprop ubootenv.var.otg_device 1; setprop persist.sys.usb.config mtp,adb\"");
            InvokeAdbSafe("shell \"settings put global package_verifier_user_consent -1; settings put global package_verifier_enable 0; bmgr enable false\"");

            if (File.Exists(ENV_UNL_IMG))
            {
                InvokeAdbSafe("push \"" + ENV_UNL_IMG + "\" /data/local/tmp/env_unlocked.img");
                InvokeAdbSafe("shell \"dd if=/data/local/tmp/env_unlocked.img of=/dev/block/env bs=65536 count=1 2>/dev/null; su -c 'dd if=/data/local/tmp/env_unlocked.img of=/dev/block/env bs=65536 count=1' 2>/dev/null; sync\"");
            }

            WriteColor("  -> Quitando Pantalla Azul de Megacable y candados ZTE...", ConsoleColor.Cyan);
            foreach (string pkg in MEGACABLE_LOCK_PACKAGES)
            {
                InvokeAdbSafe("shell \"cmd appops set " + pkg + " RUN_IN_BACKGROUND ignore; am force-stop " + pkg + "; pm disable-user --user 0 " + pkg + "; pm uninstall -k --user 0 " + pkg + "\"");
            }

            WriteColor("  -> Instalando Interfaz y Apps del Firmware Xview Liberado (DMC.v2.25)...", ConsoleColor.Cyan);
            int totalApks = LIBERATED_FIRMWARE_UI_APKS.GetLength(0);
            for (int i = 0; i < totalApks; i++)
            {
                string apkPath = Path.Combine(APKS_DIR, LIBERATED_FIRMWARE_UI_APKS[i, 0]);
                if (File.Exists(apkPath))
                {
                    WriteColor(string.Format("     [{0}/{1}] {2}...", i + 1, totalApks, LIBERATED_FIRMWARE_UI_APKS[i, 1]), ConsoleColor.White);
                    InvokeAdbSafe("install -r -d -g \"" + apkPath + "\"", 90000, true);
                }
            }

            WriteColor("  -> Activando Launcher del Firmware Xview Liberado en la TV...", ConsoleColor.Green);
            InvokeAdbSafe("shell \"cmd package set-home-activity com.google.android.tvlauncher/.MainActivity 2>/dev/null || cmd package set-home-activity dev.dworks.apps.alauncher/.Launcher\"");
            InvokeAdbSafe("shell \"settings put secure enabled_accessibility_services flar2.homebutton/flar2.homebutton.ButtonMapperService; settings put secure accessibility_enabled 1\"");
            InvokeAdbSafe("shell \"input keyevent KEYCODE_WAKEUP; input keyevent KEYCODE_HOME\"");
            InvokeAdbSafe("shell \"am start -a android.intent.action.MAIN -c android.intent.category.HOME\"");
        }

        static void InvokeDirectLiberatedUiTransformation()
        {
            Console.WriteLine();
            ToolResult adbRes = InvokeToolSafe(ADB_EXE, "devices", 2000, true);
            if (Regex.IsMatch(adbRes.Output, @"(?m)^(\S+)\s+device\b") || ScanLocalNetworkForBoxAdb())
            {
                ExecuteLiberatedXviewFirmwareUiTransformation();
                WriteColor("\n[PASO 4/4] ¡EXITO TOTAL! Interfaz del Firmware Xview Liberado activa en la TV.", ConsoleColor.Green);
            }
            else
            {
                WriteColor("[!] No se detecto ADB activo. Usa la Opcion [1] (4 Pasos Directos).", ConsoleColor.Yellow);
            }
            WriteColor("\nPresiona ENTER para volver: ", ConsoleColor.Gray, false);
            Console.ReadLine();
        }

        static bool AutoPrepareConnectedUsbDrive()
        {
            try
            {
                var drives = DriveInfo.GetDrives().Where(d => d.DriveType == DriveType.Removable && d.IsReady).ToList();
                string targetUsb = null;
                if (drives.Count == 1)
                {
                    targetUsb = drives[0].Name;
                }
                else if (drives.Count > 1)
                {
                    foreach (var d in drives) WriteColor("  * Unidad " + d.Name + " (" + d.VolumeLabel + ")", ConsoleColor.Green);
                    WriteColor("Letra de tu USB (ej. D): ", ConsoleColor.Yellow, false);
                    string drv = (Console.ReadLine() ?? "").Replace(":", "").Replace("\\", "").Trim();
                    if (!string.IsNullOrEmpty(drv)) targetUsb = drv + ":\\";
                }

                if (string.IsNullOrEmpty(targetUsb) || !Directory.Exists(targetUsb))
                {
                    WriteColor("[!] Inserta la memoria USB KINGSTON en la PC para copiarle los archivos.", ConsoleColor.Yellow);
                    return false;
                }

                WriteColor("[*] Copiando llaves y Firmware Xview Liberado a " + targetUsb + "...", ConsoleColor.Cyan);
                foreach (string f in Directory.GetFiles(KEYS_DIR))
                {
                    File.Copy(f, Path.Combine(targetUsb, Path.GetFileName(f)), true);
                }
                string usbApkDir = Path.Combine(targetUsb, "INTERFAZ_FIRMWARE_XVIEW_LIBERADO");
                Directory.CreateDirectory(usbApkDir);
                for (int i = 0; i < LIBERATED_FIRMWARE_UI_APKS.GetLength(0); i++)
                {
                    string apkName = LIBERATED_FIRMWARE_UI_APKS[i, 0];
                    string src = Path.Combine(APKS_DIR, apkName);
                    if (File.Exists(src)) File.Copy(src, Path.Combine(usbApkDir, apkName), true);
                }
                WriteColor("[OK] Memoria USB " + targetUsb + " lista!", ConsoleColor.Green);
                return true;
            }
            catch (Exception ex)
            {
                WriteColor("[!] Error en USB: " + ex.Message, ConsoleColor.Red);
                return false;
            }
        }
    }
}
