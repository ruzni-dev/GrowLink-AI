import { useState } from 'react';
import type { Profile } from '../services/profile';

type ProfileVisibilityControlProps = { profile: Profile; onSave: (visibility: Profile['visibility']) => Promise<void>; saving: boolean };

export function ProfileVisibilityControl({ profile, onSave, saving }: ProfileVisibilityControlProps) {
  const [visibility, setVisibility] = useState(profile.visibility ?? 'PRIVATE');
  return <section className="profile-card profile-visibility"><div className="profile-card__heading"><div><p className="eyebrow">Discovery privacy</p><h2>Profile visibility</h2></div><span className="profile-card__count">02</span></div><p>Private profiles never appear in search. Public profiles share your name, photo, introduction, location, skills, education, experience and interests.</p><div className="profile-visibility__controls"><label className="profile-field"><span>Who can discover this profile?</span><select value={visibility} onChange={(event) => setVisibility(event.target.value as Profile['visibility'])}><option value="PRIVATE">Private</option><option value="PUBLIC">Public</option></select></label><button className="button button--primary" type="button" disabled={saving || visibility === profile.visibility} onClick={() => onSave(visibility)}>Save visibility</button></div></section>;
}