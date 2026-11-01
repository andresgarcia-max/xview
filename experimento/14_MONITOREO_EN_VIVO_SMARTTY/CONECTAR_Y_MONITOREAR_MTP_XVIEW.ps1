# ==============================================================================
# CONECTAR_Y_MONITOREAR_MTP_XVIEW.ps1
# Conecta al 100% la sesión WPD/MTP de Xview+ (USB\VID_18D1&PID_4EE1 / 4EE2),
# destraba ventanas de explorer.exe colgadas en "Conectándose al dispositivo..."
# y verifica los volúmenes de almacenamiento expuestos por MtpServer.
# ==============================================================================

$ErrorActionPreference = "SilentlyContinue"

# 1. Detener cualquier bucle fastboot.exe en segundo plano que cause contención USB (HRESULT 0x8007001F)
Get-Process -Name "fastboot" -ErrorAction SilentlyContinue | Stop-Process -Force

# 2. Verificar presencia PnP del Xview+
$pnp = Get-PnpDevice -PresentOnly | Where-Object { $_.InstanceId -match "VID_18D1&PID_4EE" }
if (-not $pnp) {
    Write-Host "[!] Xview+ no detectado en el bus USB." -ForegroundColor Red
    exit 1
}
foreach ($d in $pnp) {
    Write-Host "[+] USB Detectado: $($d.FriendlyName) | $($d.InstanceId) | Status: $($d.Status) | Class: $($d.Class)" -ForegroundColor Green
}

# 3. Consultar sesión MTP/WPD en tiempo real mediante IPortableDevice
$code = @'
using System;
using System.Runtime.InteropServices;

[StructLayout(LayoutKind.Sequential)]
public struct PROPERTYKEY {
    public Guid fmtid;
    public uint pid;
}

[ComImport, Guid("0af10cec-2ecd-4b92-9581-34f6ae0637f3")]
public class PortableDeviceManagerClass { }

[ComImport, Guid("a1567595-4c2f-4574-a6fa-ecef917b9a40"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
public interface IPortableDeviceManager {
    [PreserveSig] int GetDevices([In, Out, MarshalAs(UnmanagedType.LPArray, ArraySubType = UnmanagedType.LPWStr)] string[] pPnPDeviceIDs, ref uint pcPnPDeviceIDs);
    [PreserveSig] int RefreshDeviceList();
}

[ComImport, Guid("728a21c5-3d9e-48d7-9810-864848f0f404")]
public class PortableDeviceClass { }

[ComImport, Guid("0c15d503-d017-47ce-9016-7b3f978721cc")]
public class PortableDeviceValuesClass { }

[ComImport, Guid("625e2df8-6392-4cf0-9ad1-3cfa5f17775c"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
public interface IPortableDevice {
    [PreserveSig] int Open([MarshalAs(UnmanagedType.LPWStr)] string pszPnPDeviceID, [MarshalAs(UnmanagedType.Interface)] object pClientInfo);
    [PreserveSig] int SendCommand(uint dwFlags, [MarshalAs(UnmanagedType.Interface)] object pParameters, [MarshalAs(UnmanagedType.Interface)] out object ppResults);
    [PreserveSig] int Content([MarshalAs(UnmanagedType.Interface)] out IPortableDeviceContent ppContent);
    [PreserveSig] int Capabilities([MarshalAs(UnmanagedType.Interface)] out object ppCapabilities);
    [PreserveSig] int Cancel();
    [PreserveSig] int Close();
}

[ComImport, Guid("6a96ed84-7c73-4480-9938-bf5af477d426"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
public interface IPortableDeviceContent {
    [PreserveSig] int EnumObjects(uint dwFlags, [MarshalAs(UnmanagedType.LPWStr)] string pszParentObjectID, [MarshalAs(UnmanagedType.Interface)] object pFilter, [MarshalAs(UnmanagedType.Interface)] out IEnumPortableDeviceObjectIDs ppenum);
    [PreserveSig] int Properties([MarshalAs(UnmanagedType.Interface)] out IPortableDeviceProperties ppProperties);
}

[ComImport, Guid("10ece955-cf41-4728-bfa0-41eedf1bbf19"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
public interface IEnumPortableDeviceObjectIDs {
    [PreserveSig] int Next(uint cObjects, [Out, MarshalAs(UnmanagedType.LPArray, ArraySubType = UnmanagedType.LPWStr, SizeParamIndex = 0)] string[] pObjIDs, out uint pcFetched);
}

[ComImport, Guid("7f6d695c-03df-4439-a809-59266beee3a6"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
public interface IPortableDeviceProperties {
    [PreserveSig] int GetSupportedProperties([MarshalAs(UnmanagedType.LPWStr)] string pszObjectID, [MarshalAs(UnmanagedType.Interface)] out object ppKeys);
    [PreserveSig] int GetPropertyAttributes([MarshalAs(UnmanagedType.LPWStr)] string pszObjectID, ref PROPERTYKEY Key, [MarshalAs(UnmanagedType.Interface)] out object ppAttributes);
    [PreserveSig] int GetValues([MarshalAs(UnmanagedType.LPWStr)] string pszObjectID, [MarshalAs(UnmanagedType.Interface)] object pKeys, [MarshalAs(UnmanagedType.Interface)] out IPortableDeviceValues ppValues);
}

[ComImport, Guid("6848f6f2-3155-4f86-b6f5-263eeeab3143"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
public interface IPortableDeviceValues {
    [PreserveSig] int GetCount(ref uint pcelt);
    [PreserveSig] int GetAt(uint index, ref PROPERTYKEY pKey, IntPtr pValue);
    [PreserveSig] int SetValue(ref PROPERTYKEY key, IntPtr pValue);
    [PreserveSig] int GetValue(ref PROPERTYKEY key, IntPtr pValue);
    [PreserveSig] int SetStringValue(ref PROPERTYKEY key, [MarshalAs(UnmanagedType.LPWStr)] string Value);
    [PreserveSig] int GetStringValue(ref PROPERTYKEY key, [MarshalAs(UnmanagedType.LPWStr)] out string pValue);
}

public class XviewWpdConnector {
    public static int CheckAndOpen() {
        var mgr = (IPortableDeviceManager)new PortableDeviceManagerClass();
        mgr.RefreshDeviceList();
        uint count = 0;
        mgr.GetDevices(null, ref count);
        if (count == 0) return -1;
        string[] devIds = new string[count];
        mgr.GetDevices(devIds, ref count);

        var dev = (IPortableDevice)new PortableDeviceClass();
        var clientInfo = new PortableDeviceValuesClass();
        int hr = dev.Open(devIds[0], clientInfo);
        Console.WriteLine("[+] Sesion MTP Open HR: 0x" + hr.ToString("X8"));
        if (hr != 0) return -2;

        IPortableDeviceContent content;
        dev.Content(out content);
        IPortableDeviceProperties props;
        content.Properties(out props);
        IPortableDeviceValues vals;
        props.GetValues("DEVICE", null, out vals);

        Guid devFmt = new Guid("26d4979a-e643-4626-9e2b-736dc0c92fdc");
        PROPERTYKEY pkMfg = new PROPERTYKEY { fmtid = devFmt, pid = 7 };
        PROPERTYKEY pkMod = new PROPERTYKEY { fmtid = devFmt, pid = 8 };
        PROPERTYKEY pkPro = new PROPERTYKEY { fmtid = devFmt, pid = 6 };
        PROPERTYKEY pkSer = new PROPERTYKEY { fmtid = devFmt, pid = 9 };
        string mfg = "", mod = "", pro = "", ser = "";
        vals.GetStringValue(ref pkMfg, out mfg);
        vals.GetStringValue(ref pkMod, out mod);
        vals.GetStringValue(ref pkPro, out pro);
        vals.GetStringValue(ref pkSer, out ser);
        Console.WriteLine(string.Format("[+] Dispositivo: {0} {1} | Protocolo: {2} | ID MTP: {3}", mfg, mod, pro, ser));

        IEnumPortableDeviceObjectIDs enumIds;
        content.EnumObjects(0, "DEVICE", null, out enumIds);
        string[] objIds = new string[16];
        uint fetched = 0;
        enumIds.Next(16, objIds, out fetched);
        Console.WriteLine("[+] Volumenes de almacenamiento expuestos en MTP: " + fetched);
        for (int i = 0; i < fetched; i++) {
            Console.WriteLine("    -> Storage ObjectID: " + objIds[i]);
        }
        dev.Close();
        return (int)fetched;
    }
}
'@
Add-Type -TypeDefinition $code
$storages = [XviewWpdConnector]::CheckAndOpen()

# 4. Refrescar / Reabrir ventana de Este equipo > Xview+
$shell = New-Object -ComObject Shell.Application
foreach ($win in @($shell.Windows())) {
    try {
        if ($win.LocationName -match "Xview") {
            $win.Quit()
        }
    } catch {}
}
Start-Sleep -Milliseconds 400
$thisPC = $shell.NameSpace(17)
$xviewItem = $thisPC.Items() | Where-Object { $_.Name -match "Xview" }
if ($xviewItem) {
    $xviewItem.InvokeVerb("open")
    Write-Host "[+] Ventana de Explorador 'Este equipo > Xview+' abierta y sincronizada." -ForegroundColor Cyan
}
