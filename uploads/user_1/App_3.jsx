import { useState, useEffect } from "react";
import { filesApi }   from "./api/fileApi";
import { useToast }   from "./hooks/useToast";
import { useStorage } from "./hooks/useStorage";
import { useUser }    from "./hooks/useUser";

import { Sidebar }       from "./components/layout/Sidebar";
import { Header }        from "./components/layout/Header";
import { Toast }         from "./components/ui/Toast";
import { AiPanel }       from "./components/ui/AiPanel";
import { UploadModal }   from "./components/modals/UploadModal";
import { ProfileModal }  from "./components/modals/ProfileModal";
import { SettingsModal } from "./components/modals/SettingsModal";

import { LoginPage }   from "./pages/LoginPage";
import { HomePage }    from "./pages/HomePage";
import { MyDrivePage } from "./pages/MyDrivePage";
import { RecentPage }  from "./pages/RecentPage";
import { StarredPage } from "./pages/StarredPage";
import { SpamPage }    from "./pages/SpamPage";
import { BinPage }     from "./pages/BinPage";
import { StoragePage } from "./pages/StoragePage";

// ── Dark mode CSS variables ────────────────────────────────────────────────────
const LIGHT_CSS = `
  :root {
    --bg:        #F8F9FA;
    --surface:   #FFFFFF;
    --border:    #E0E0E0;
    --text:      #202124;
    --text-sub:  #5F6368;
    --primary:   #1A73E8;
  }
  body { background: var(--bg); color: var(--text); }
`;

const DARK_CSS = `
  :root {
    --bg:        #1a1a1a;
    --surface:   #2d2d2d;
    --border:    #3d3d3d;
    --text:      #e8eaed;
    --text-sub:  #9aa0a6;
    --primary:   #8ab4f8;
  }
  body { background: var(--bg) !important; color: var(--text) !important; }
  aside, header, .modal-surface { background: var(--surface) !important; border-color: var(--border) !important; }
`;

export default function App() {
  const [activePage,    setActivePage]    = useState("home");
  const [viewMode,      setViewMode]      = useState("grid");
  const [showUpload,    setShowUpload]    = useState(false);
  const [showProfile,   setShowProfile]   = useState(false);
  const [showSettings,  setShowSettings]  = useState(false);
  const [showAi,        setShowAi]        = useState(false);
  const [selected,      setSelected]      = useState(new Set());
  const [searchResults, setSearchResults] = useState(null);

  const { toasts, toast, removeToast }             = useToast();
  const { storage, refreshStorage }                = useStorage();
  const { user, setUser, loading: userLoading }    = useUser();

  // ── Apply dark mode whenever user.theme changes ───────────────────────────
  useEffect(() => {
    let styleTag = document.getElementById("theme-style");
    if (!styleTag) {
      styleTag = document.createElement("style");
      styleTag.id = "theme-style";
      document.head.appendChild(styleTag);
    }
    styleTag.textContent = user?.theme === "dark" ? DARK_CSS : LIGHT_CSS;
  }, [user?.theme]);

  const navigate = (page) => {
    setActivePage(page);
    setSelected(new Set());
    setSearchResults(null);
  };

  const handleLogin  = (u) => setUser(u);
  const handleLogout = () => { setUser(null); setActivePage("home"); };

  const handleUpload = async (file, parentFolderId) => {
    try {
      await filesApi.upload(file, parentFolderId);
      toast(`"${file.name}" uploaded`, "success");
      refreshStorage();
    } catch {
      toast(`Failed to upload "${file.name}"`, "error");
      throw new Error("upload failed");
    }
  };

  const handleDeleteSelected = async () => {
    for (const id of selected) {
      try { await filesApi.softDelete(id); } catch { /* skip */ }
    }
    toast(`${selected.size} file(s) moved to bin`, "success");
    setSelected(new Set());
    refreshStorage();
  };

  const sharedProps = { viewMode, selected, setSelected, toast };

  const renderPage = () => {
    switch (activePage) {
      case "home":    return <HomePage    onNavigate={navigate} toast={toast} />;
      case "drive":   return <MyDrivePage {...sharedProps} onRefreshStorage={refreshStorage} searchResults={searchResults} />;
      case "recent":  return <RecentPage  {...sharedProps} />;
      case "starred": return <StarredPage {...sharedProps} />;
      case "spam":    return <SpamPage    {...sharedProps} />;
      case "bin":     return <BinPage     {...sharedProps} onRefreshStorage={refreshStorage} />;
      case "storage": return <StoragePage storage={storage} />;
      default:        return <HomePage    onNavigate={navigate} toast={toast} />;
    }
  };

  const BASE_STYLE = `
    @import url('https://fonts.googleapis.com/css2?family=Google+Sans:wght@400;500;600&display=swap');
    *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
    body { font-family: 'Google Sans', Arial, sans-serif; }
    ::-webkit-scrollbar { width: 6px; height: 6px; }
    ::-webkit-scrollbar-track { background: transparent; }
    ::-webkit-scrollbar-thumb { background: #BDC1C6; border-radius: 99px; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `;

  if (userLoading) {
    return (
      <>
        <style>{BASE_STYLE + LIGHT_CSS}</style>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "center", height: "100vh" }}>
          <div style={{ width: 36, height: 36, border: "3px solid #E0E0E0", borderTopColor: "#1A73E8", borderRadius: "50%", animation: "spin 0.7s linear infinite" }} />
        </div>
      </>
    );
  }

  if (!user) {
    return (
      <>
        <style>{BASE_STYLE + LIGHT_CSS}</style>
        <LoginPage onLogin={handleLogin} />
        <Toast toasts={toasts} remove={removeToast} />
      </>
    );
  }

  return (
    <>
      <style>{BASE_STYLE}</style>

      <div style={{ display: "flex", height: "100vh", overflow: "hidden", background: "var(--bg, #F8F9FA)" }}>
        <Sidebar
          active={activePage}
          onNavigate={navigate}
          storage={storage}
          onUpload={() => setShowUpload(true)}
        />

        <div style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
          <Header
            user={user}
            onSearchResults={r => { setSearchResults(r); if (r) setActivePage("drive"); }}
            viewMode={viewMode}    setViewMode={setViewMode}
            selected={selected}    onDeleteSelected={handleDeleteSelected}
            onOpenProfile={() => setShowProfile(true)}
            onOpenSettings={() => setShowSettings(true)}
            onToggleAi={() => setShowAi(o => !o)}
            aiOpen={showAi}
            onLogout={handleLogout}
          />
          <main style={{ flex: 1, overflowY: "auto" }}>
            {renderPage()}
          </main>
        </div>

        {showAi && <AiPanel onClose={() => setShowAi(false)} user={user} />}
      </div>

      {showUpload   && <UploadModal   onClose={() => setShowUpload(false)}   onUpload={handleUpload} />}
      {showProfile  && <ProfileModal  onClose={() => setShowProfile(false)}  user={user} toast={toast} onSave={setUser} />}
      {showSettings && <SettingsModal onClose={() => setShowSettings(false)} user={user} toast={toast} onSave={u => { setUser(u); }} />}

      <Toast toasts={toasts} remove={removeToast} />
    </>
  );
}
