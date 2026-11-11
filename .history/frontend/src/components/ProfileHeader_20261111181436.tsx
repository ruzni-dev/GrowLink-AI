import type { Profile } from '../services/profile';
import { Icon } from './Icon';

type ProfileHeaderProps = { profile: Profile; onSignOut: () => void };

export function ProfileHeader({ profile, onSignOut }: ProfileHeaderProps) {
  return <header className="profile-header"><div className="profile-header__avatar">{profile.fullName.slice(0, 2).toUpperCase()}</div><div className="profile-header__identity"><p className="eyebrow">Your public profile</p><h1>{profile.fullName}</h1><p><Icon name="map" size={14} /> {profile.location || 'Add your location'} {profile.country && ` · ${profile.country}`}</p></div><div className="profile-header__actions"><button className="button button--outline" type="button" onClick={onSignOut}>Sign out</button></div></header>;
}
