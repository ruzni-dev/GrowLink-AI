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
import { MentorDiscovery } from './pages/MentorDiscovery';
import { MentorDetails } from './pages/MentorDetails';
import { CreateMentorshipOffer } from './pages/CreateMentorshipOffer';
import { MyMentorship } from './pages/MyMentorship';

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
  if (path === '/mentors') page = <MentorDiscovery onNavigate={navigate} />;
  const mentorDetailsMatch = path.match(/^\/mentors\/(\d+)$/);
  if (mentorDetailsMatch) page = <MentorDetails id={Number(mentorDetailsMatch[1])} onNavigate={navigate} onNotify={notify} />;
  if (path === '/mentorship/create') page = <ProtectedRoute onNavigate={navigate}><CreateMentorshipOffer onNavigate={navigate} onNotify={notify} /></ProtectedRoute>;
  const editOfferMatch = path.match(/^\/mentorship\/edit\/(\d+)$/);
  if (editOfferMatch) page = <ProtectedRoute onNavigate={navigate}><CreateMentorshipOffer offerId={Number(editOfferMatch[1])} onNavigate={navigate} onNotify={notify} /></ProtectedRoute>;
  if (path === '/my-mentorship') page = <ProtectedRoute onNavigate={navigate}><MyMentorship onNavigate={navigate} onNotify={notify} /></ProtectedRoute>;

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
