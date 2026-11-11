import { useEffect, useState } from 'react';
import { AuthProvider } from './context/AuthProvider';
import { ProtectedRoute } from './components/ProtectedRoute';
import { Dashboard } from './pages/Dashboard';
import { Home } from './pages/Home';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { ProfilePage } from './pages/ProfilePage';
import { TalentDiscovery } from './pages/TalentDiscovery';
import { TalentProfile } from './pages/TalentProfile';
import { ProfileVisibilitySettings } from './pages/ProfileVisibilitySettings';

function AppContent() {
  const [path, setPath] = useState(window.location.pathname);
  const [notice, setNotice] = useState('');

  useEffect(() => {
    const handlePopState = () => setPath(window.location.pathname);
    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  const notify = (message: string) => {
    setNotice(message);
    window.setTimeout(() => setNotice(''), 3200);
  };

  const navigate = (nextPath: string) => {
    window.history.pushState({}, '', nextPath);
    setPath(nextPath);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  let page = <Home onNotify={notify} />;
  if (path === '/login') page = <Login onNavigate={navigate} onNotify={notify} />;
  if (path === '/register') page = <Register onNavigate={navigate} onNotify={notify} />;
  if (path === '/dashboard') {
    page = <ProtectedRoute onNavigate={navigate}><Dashboard onNavigate={navigate} /></ProtectedRoute>;
  }
  if (path === '/profile') {
    page = <ProtectedRoute onNavigate={navigate}><><ProfilePage onNavigate={navigate} onNotify={notify} /><ProfileVisibilitySettings onNotify={notify} /></></ProtectedRoute>;
  }
  if (path === '/talents') page = <TalentDiscovery onNavigate={navigate} />;
  const talentProfileMatch = path.match(/^\/talents\/(\d+)$/);
  if (talentProfileMatch) page = <TalentProfile id={Number(talentProfileMatch[1])} onNavigate={navigate} />;

  return (
    <div className="app-shell">
      {page}
      {notice && <div className="toast" role="status">{notice}</div>}
    </div>
  );
}

function App() {
  return <AuthProvider><AppContent /></AuthProvider>;
}

export default App;
