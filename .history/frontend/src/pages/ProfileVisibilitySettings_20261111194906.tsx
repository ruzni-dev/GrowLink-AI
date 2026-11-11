import { useEffect, useState } from 'react';
import { ProfileVisibilityControl } from '../components/ProfileVisibilityControl';
import { useAuth } from '../context/useAuth';
import { profileApi, type Profile } from '../services/profile';
import './talent.css';

type ProfileVisibilitySettingsProps = { onNotify: (message: string) => void };

export function ProfileVisibilitySettings({ onNotify }: ProfileVisibilitySettingsProps) {
  const { token } = useAuth();
  const [profile, setProfile] = useState<Profile>();
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    let active = true;
    if (token) profileApi.getProfile(token).then((value) => { if (active) setProfile(value); }).catch(() => undefined);
    return () => { active = false; };
  }, [token]);

  async function save(visibility: Profile['visibility']) {
    if (!token) return;
    setSaving(true);
    try {
      setProfile(await profileApi.updateProfile(token, { visibility }));
      onNotify('Profile visibility updated.');
    } catch (error) {
      onNotify(error instanceof Error ? error.message : 'Unable to update profile visibility.');
    } finally {
      setSaving(false);
    }
  }

  if (!profile) return null;
  return <div className="shell profile-visibility-outside"><ProfileVisibilityControl profile={profile} onSave={save} saving={saving} /></div>;
}