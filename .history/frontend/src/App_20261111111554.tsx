import { useEffect, useState } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { Dashboard } from './pages/Dashboard';
import { Home } from './pages/Home';
import { Login } from './pages/Login';
import { Register } from './pages/Register';

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
