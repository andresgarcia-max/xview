param(
    [switch]$Loop,
    [int]$IntervalSeconds = 2
)

$ErrorActionPreference = "SilentlyContinue"
$OutDir = "c:\Users\andre\OneDrive\Desktop\xview\experimento\14_MONITOREO_EN_VIVO_SMARTTY"
New-Item -ItemType Directory -Path $OutDir -Force | Out-Null

Add-Type @'
using System;
using System.IO;
using System.Text;
using System.Collections.Generic;
using System.Runtime.InteropServices;

public class SmarttyFullMonitor {
    [DllImport("kernel32.dll", SetLastError = true)]
    public static extern IntPtr OpenProcess(uint dwDesiredAccess, bool bInheritHandle, int dwProcessId);

    [DllImport("kernel32.dll", SetLastError = true)]
    public static extern bool ReadProcessMemory(IntPtr hProcess, IntPtr lpBaseAddress, byte[] lpBuffer, int dwSize, out int lpNumberOfBytesRead);

    [DllImport("kernel32.dll", SetLastError = true)]
    public static extern int VirtualQueryEx(IntPtr hProcess, IntPtr lpAddress, out MEMORY_BASIC_INFORMATION lpBuffer, uint dwLength);

    [DllImport("kernel32.dll", SetLastError = true)]
    public static extern bool CloseHandle(IntPtr hObject);

    [StructLayout(LayoutKind.Sequential)]
    public struct MEMORY_BASIC_INFORMATION {
        public IntPtr BaseAddress;
        public IntPtr AllocationBase;
        public uint AllocationProtect;
        public IntPtr RegionSize;
        public uint State;
        public uint Protect;
        public uint Type;
    }

    const uint PROCESS_QUERY_INFORMATION = 0x0400;
    const uint PROCESS_VM_READ = 0x0010;
    const uint MEM_COMMIT = 0x1000;
    const uint MEM_PRIVATE = 0x20000;
    const uint PAGE_NOACCESS = 0x01;
    const uint PAGE_GUARD = 0x100;

    public static void ScanMemory(int pid, List<string> asciiBlocks, List<string> utf16Blocks) {
        IntPtr hProc = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, false, pid);
        if (hProc == IntPtr.Zero) return;

        try {
            long address = 0;
            long maxAddress = 0x7FFFFFFF0000L;
            MEMORY_BASIC_INFORMATION mbi;
            int mbiSize = Marshal.SizeOf(typeof(MEMORY_BASIC_INFORMATION));

            while (address < maxAddress) {
                int ret = VirtualQueryEx(hProc, (IntPtr)address, out mbi, (uint)mbiSize);
                if (ret == 0) break;

                long regionSize = mbi.RegionSize.ToInt64();
                if (mbi.State == MEM_COMMIT && mbi.Type == MEM_PRIVATE &&
                    (mbi.Protect & PAGE_NOACCESS) == 0 && (mbi.Protect & PAGE_GUARD) == 0 &&
                    regionSize > 0 && regionSize <= 64 * 1024 * 1024) {

                    byte[] buf = new byte[(int)regionSize];
                    int bytesRead = 0;
                    if (ReadProcessMemory(hProc, mbi.BaseAddress, buf, buf.Length, out bytesRead) && bytesRead > 0) {
                        ExtractAsciiRuns(buf, bytesRead, asciiBlocks);
                        ExtractUtf16Runs(buf, bytesRead, utf16Blocks);
                    }
                }
                address = mbi.BaseAddress.ToInt64() + regionSize;
            }
        } finally {
            CloseHandle(hProc);
        }
    }

    private static bool LooksLikeUartOrTerminal(string s) {
        if (s.Length < 10) return false;
        // Filter out obvious .NET metadata / XML / file paths unless it has UART markers
        if (s.IndexOf('\n') >= 0 || s.IndexOf('\r') >= 0) return true;
        if (s.IndexOf("[   ", StringComparison.Ordinal) >= 0) return true;
        string[] markers = new string[] {
            "G12A:", "BL2", "BL31", "BL32", "U-Boot", "Linux", "Kernel", "androidboot",
            "aml_", "meson", "mmcblk", "dwc_otg", "dwc3", "GPIO", "InUsbBurn", "noSof",
            "systemcontrol", "bdctrl", "avb_", "vbmeta", "recovery", "reboot", "fastboot",
            "ZTE", "ztestb", "Megacable", "B866V2", "g12a", "console", "init:", "selinux",
            "EXT4-fs", "fscrypt", "emmc", "amlkey", "Uncompressing", "Starting kernel",
            "Hit Enter", "bootcmd", "storeboot", "wipe", "root@", "shell@", ":/ #", ":/ $",
            "uid=0", "dmesg", "logcat", "getprop", "setprop", "pm ", "am ", "partitions",
            "dtb", "edid", "hdmi", "rx", "tx", "ttyS0", "baud", "ddr", "rpmb", "FIP"
        };
        for (int i = 0; i < markers.Length; i++) {
            if (s.IndexOf(markers[i], StringComparison.OrdinalIgnoreCase) >= 0) return true;
        }
        return false;
    }

    private static void ExtractAsciiRuns(byte[] buf, int len, List<string> results) {
        int start = -1;
        for (int i = 0; i < len; i++) {
            byte b = buf[i];
            bool valid = (b >= 0x20 && b <= 0x7E) || b == 0x0A || b == 0x0D || b == 0x09 || b == 0x1B;
            if (valid) {
                if (start == -1) start = i;
            } else {
                if (start != -1) {
                    int runLen = i - start;
                    if (runLen >= 16) {
                        string s = Encoding.ASCII.GetString(buf, start, runLen);
                        if (LooksLikeUartOrTerminal(s)) results.Add(s);
                    }
                    start = -1;
                }
            }
        }
    }

    private static void ExtractUtf16Runs(byte[] buf, int len, List<string> results) {
        int start = -1;
        for (int i = 0; i + 1 < len; i += 2) {
            byte b0 = buf[i];
            byte b1 = buf[i + 1];
            bool valid = b1 == 0x00 && ((b0 >= 0x20 && b0 <= 0x7E) || b0 == 0x0A || b0 == 0x0D || b0 == 0x09);
            if (valid) {
                if (start == -1) start = i;
            } else {
                if (start != -1) {
                    int runBytes = i - start;
                    if (runBytes >= 20) {
                        string s = Encoding.Unicode.GetString(buf, start, runBytes);
                        if (LooksLikeUartOrTerminal(s)) results.Add(s);
                    }
                    start = -1;
                }
            }
        }
    }
}
'@

$seenLines  = New-Object 'System.Collections.Generic.HashSet[string]'
$seenBlocks = New-Object 'System.Collections.Generic.HashSet[string]'

$liveLogPath    = Join-Path $OutDir "CAPTURA_EN_VIVO_SMARTTY.log"
$asciiBlockPath = Join-Path $OutDir "BLOQUES_ASCII_COMPLETOS_SESION.txt"
$utf16BlockPath = Join-Path $OutDir "BLOQUES_UTF16_COMPLETOS_SESION.txt"
$statusPath     = Join-Path $OutDir "ESTADO_MONITOR.txt"

if (Test-Path $liveLogPath) {
    Get-Content $liveLogPath | ForEach-Object {
        if ($_ -match '^\[[^\]]+\]\s+(.*)$') { [void]$seenLines.Add($Matches[1]) }
    }
}

$passNumber = 0

function Invoke-SinglePass {
    $script:passNumber++
    $procs = Get-Process SmarTTY* -ErrorAction SilentlyContinue
    $comPorts = Get-CimInstance Win32_PnPEntity -ErrorAction SilentlyContinue | Where-Object { $_.Name -match 'COM\d+' } | Select-Object -ExpandProperty Name
    $now = Get-Date -Format "yyyy-MM-dd HH:mm:ss.fff"

    if (-not $procs) {
        "[$now] [Pass #$script:passNumber] En espera de SmarTTY.exe... | Lineas acumuladas: $($seenLines.Count) | Puertos: $($comPorts -join ', ')" | Set-Content $statusPath -Encoding UTF8
        return
    }

    foreach ($p in $procs) {
        $asciiBlocks = New-Object 'System.Collections.Generic.List[string]'
        $utf16Blocks = New-Object 'System.Collections.Generic.List[string]'
        [SmarttyFullMonitor]::ScanMemory($p.Id, $asciiBlocks, $utf16Blocks)

        $newLinesCount = 0
        $newBlocksCount = 0
        $newLinesBuffer = New-Object 'System.Collections.Generic.List[string]'
        $newAsciiBuffer = New-Object 'System.Collections.Generic.List[string]'
        $newUtf16Buffer = New-Object 'System.Collections.Generic.List[string]'

        foreach ($blk in $asciiBlocks) {
            $trimmedBlk = $blk.Trim()
            if ($trimmedBlk.Length -ge 20 -and $seenBlocks.Add("A:" + $trimmedBlk)) {
                $newAsciiBuffer.Add("=== [$now PID=$($p.Id) ASCII LEN=$($trimmedBlk.Length)] ===`r`n$trimmedBlk`r`n")
                $newBlocksCount++
            }
            foreach ($ln in ($trimmedBlk -split "`r?`n")) {
                $c = $ln.Trim()
                if ($c.Length -ge 4 -and $seenLines.Add($c)) {
                    $newLinesBuffer.Add("[$now] [ASCII] $c")
                    $newLinesCount++
                }
            }
        }

        foreach ($blk in $utf16Blocks) {
            $trimmedBlk = $blk.Trim()
            if ($trimmedBlk.Length -ge 20 -and $seenBlocks.Add("U:" + $trimmedBlk)) {
                $newUtf16Buffer.Add("=== [$now PID=$($p.Id) UTF16 LEN=$($trimmedBlk.Length)] ===`r`n$trimmedBlk`r`n")
                $newBlocksCount++
            }
            foreach ($ln in ($trimmedBlk -split "`r?`n")) {
                $c = $ln.Trim()
                if ($c.Length -ge 4 -and $seenLines.Add($c)) {
                    $newLinesBuffer.Add("[$now] [UTF16] $c")
                    $newLinesCount++
                }
            }
        }

        if ($newLinesBuffer.Count -gt 0) {
            Add-Content -Path $liveLogPath -Value $newLinesBuffer -Encoding UTF8
        }
        if ($newAsciiBuffer.Count -gt 0) {
            Add-Content -Path $asciiBlockPath -Value $newAsciiBuffer -Encoding UTF8
        }
        if ($newUtf16Buffer.Count -gt 0) {
            Add-Content -Path $utf16BlockPath -Value $newUtf16Buffer -Encoding UTF8
        }

        $statusLine = "[$now] [Pass #$script:passNumber] Monitoreando SmarTTY PID=$($p.Id) WS=$([math]::Round($p.WorkingSet64/1MB,1))MB | Lineas unicas: $($seenLines.Count) (+$newLinesCount) | Bloques: $($seenBlocks.Count) (+$newBlocksCount) | Puertos: $($comPorts -join ', ')"
        $statusLine | Set-Content $statusPath -Encoding UTF8
        if ($newLinesCount -gt 0) {
            Write-Host $statusLine
        }
        [GC]::Collect()
    }
}

if ($Loop) {
    Write-Host "Iniciando monitoreo continuo en vivo de SmarTTY.exe cada $IntervalSeconds segundos..."
    while ($true) {
        Invoke-SinglePass
        Start-Sleep -Seconds $IntervalSeconds
    }
} else {
    Invoke-SinglePass
    Get-Content $statusPath
}
