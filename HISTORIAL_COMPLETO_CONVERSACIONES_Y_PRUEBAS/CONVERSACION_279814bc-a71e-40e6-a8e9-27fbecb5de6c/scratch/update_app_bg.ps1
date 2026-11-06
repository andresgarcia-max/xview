$file = "LEGENDS\src\App.tsx"
$content = [System.IO.File]::ReadAllText((Resolve-Path $file), [System.Text.Encoding]::UTF8)

# 1. Add import for dumbbell background
$oldImport = "import { IndependentTerminalApp } from './components/access/IndependentTerminalApp';"
$newImport = "import { IndependentTerminalApp } from './components/access/IndependentTerminalApp';`r`nimport gymDumbbellBg from './assets/gym-dumbbell-background.jpg';"
$content = $content.Replace($oldImport, $newImport)

# 2. Add Background Layer and update main in MainLayout
$oldMainLayout = @'
  return (
    <div className="min-h-screen bg-gym-bg text-gym-text flex flex-col font-sans select-none sm:select-auto">
      
      {/* Top Navbar matching Panadería Brito */}
      <Navbar 
        activeTab={activeTab} 
        setActiveTab={setActiveTab} 
        onOpenCart={() => setIsCartOpen(true)}
        onOpenNotifications={() => setIsNotificationsOpen(true)}
        onOpenSync={() => setIsSyncModalOpen(true)}
      />

      <div className="flex-1 flex overflow-hidden">
        {/* Navigation Sidebar */}
        <Sidebar activeTab={activeTab} setActiveTab={setActiveTab} />

        {/* Dynamic Main Workspace View */}
        <main className={`flex-1 ${
          activeTab === 'pos' 
            ? 'p-2 sm:p-2.5 overflow-hidden flex flex-col h-[calc(100vh-61px)]' 
            : 'overflow-y-auto p-4 sm:p-6 lg:p-8'
        } bg-gradient-to-b from-gym-bg to-[#07080c]`}>
'@

$newMainLayout = @'
  return (
    <div className="min-h-screen bg-[#07080b] text-gym-text flex flex-col font-sans select-none sm:select-auto relative overflow-hidden">
      
      {/* Background Layer: Gym Dumbbells Texture & Ambient Lighting (Opción 1) */}
      <div className="fixed inset-0 pointer-events-none z-0 overflow-hidden">
        <img
          src={gymDumbbellBg}
          alt="Gym Dumbbell Background"
          className="w-full h-full object-cover filter brightness-[0.38] contrast-[1.12] saturate-[0.85]"
        />
        {/* Soft Radial Ambient Spotlight (Cálido Ámbar Deportivo) */}
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_50%_15%,rgba(245,158,11,0.08)_0%,rgba(0,0,0,0.65)_70%)]" />
        {/* Elegant Vignette & Dark Tint to ensure high contrast and readability */}
        <div className="absolute inset-0 bg-gradient-to-b from-black/45 via-black/35 to-black/70" />
      </div>

      {/* Top Navbar matching Panadería Brito */}
      <Navbar 
        activeTab={activeTab} 
        setActiveTab={setActiveTab} 
        onOpenCart={() => setIsCartOpen(true)}
        onOpenNotifications={() => setIsNotificationsOpen(true)}
        onOpenSync={() => setIsSyncModalOpen(true)}
      />

      <div className="flex-1 flex overflow-hidden relative z-10">
        {/* Navigation Sidebar */}
        <Sidebar activeTab={activeTab} setActiveTab={setActiveTab} />

        {/* Dynamic Main Workspace View */}
        <main className={`flex-1 ${
          activeTab === 'pos' 
            ? 'p-2 sm:p-2.5 overflow-hidden flex flex-col h-[calc(100vh-61px)]' 
            : 'overflow-y-auto p-4 sm:p-6 lg:p-8'
        } bg-black/25 backdrop-blur-[0.5px]`}>
'@

# Normalize line endings
$content = $content.Replace("`r`n", "`n")
$oldMainLayout = $oldMainLayout.Replace("`r`n", "`n")
$newMainLayout = $newMainLayout.Replace("`r`n", "`n")

if ($content.Contains($oldMainLayout)) {
    $content = $content.Replace($oldMainLayout, $newMainLayout)
    $content = $content.Replace("`n", "`r`n")
    [System.IO.File]::WriteAllText((Resolve-Path $file), $content, [System.Text.Encoding]::UTF8)
    Write-Host "Updated App.tsx successfully"
} else {
    Write-Host "Could not find oldMainLayout in App.tsx"
}
