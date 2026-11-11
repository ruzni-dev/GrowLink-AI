import { useAuth } from '../context/AuthContext';
import { Icon } from '../components/Icon';

type DashboardProps = { onNavigate: (path: string) => void };

export function Dashboard({ onNavigate }: DashboardProps) {
  const { user, signOut } = useAuth();
  async function handleSignOut() {
    await signOut();
    onNavigate('/');
  }

  return <main className="dashboard-page"><header className="dashboard-nav"><a className="brand" href="/" onClick={() => onNavigate('/')}><span className="brand__mark"><span>G</span></span><span className="brand__name">GrowLink <em>AI</em></span></a><div className="dashboard-nav__user"><span className="dashboard-nav__avatar">{user?.fullName.slice(0, 2).toUpperCase()}</span><span>{user?.fullName}</span><button type="button" onClick={handleSignOut}>Sign out</button></div></header><div className="shell dashboard-content"><div className="dashboard-welcome"><div><p className="eyebrow">Your community space</p><h1>Good to have you here, {user?.fullName.split(' ')[0]}.</h1><p>Your dashboard is ready for the next chapter of your GrowLink AI journey.</p></div><div className="dashboard-welcome__badge"><Icon name="spark" size={18} /> {user?.status === 'PENDING_APPROVAL' ? 'Review in progress' : 'Profile active'}</div></div><div className="dashboard-grid"><section className="dashboard-card dashboard-card--feature"><span className="dashboard-card__icon">✦</span><p className="eyebrow">Coming next</p><h2>Make your first meaningful connection.</h2><p>Explore the talent network and find people who share your curiosity, skills and goals.</p><button className="button button--primary" type="button" onClick={() => onNavigate('/#talent')}>Explore talent <Icon name="arrow" size={16} /></button></section><section className="dashboard-card"><p className="eyebrow">Profile snapshot</p><h2>{user?.role === 'USER' ? 'Community member' : user?.role}</h2><dl className="dashboard-details"><div><dt>Email</dt><dd>{user?.email}</dd></div><div><dt>Location</dt><dd>{user?.location}</dd></div><div><dt>Account status</dt><dd>{user?.status.replace('_', ' ').toLowerCase()}</dd></div></dl></section></div></div></main>;
}
