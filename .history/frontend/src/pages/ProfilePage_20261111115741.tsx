import { useCallback, useEffect, useMemo, useState } from 'react';
import { useAuth } from '../context/useAuth';
import { Icon } from '../components/Icon';
import { EducationSection } from '../components/EducationSection';
import { ExperienceSection } from '../components/ExperienceSection';
import { InterestsSection } from '../components/InterestsSection';
import { ProfileCompletionIndicator } from '../components/ProfileCompletionIndicator';
import { ProfileHeader } from '../components/ProfileHeader';
import { ProfileInformationForm } from '../components/ProfileInformationForm';
import { SkillsSection } from '../components/SkillsSection';
import { profileApi, type Education, type Experience, type Interest, type Profile, type Skill } from '../services/profile';

type ProfilePageProps = { onNavigate: (path: string) => void; onNotify: (message: string) => void };

export function ProfilePage({ onNavigate, onNotify }: ProfilePageProps) {
  const { token, signOut } = useAuth();
  const [profile, setProfile] = useState<Profile>();
  const [education, setEducation] = useState<Education[]>([]);
  const [skills, setSkills] = useState<Skill[]>([]);
  const [experience, setExperience] = useState<Experience[]>([]);
  const [availableInterests, setAvailableInterests] = useState<Interest[]>([]);
  const [selectedInterests, setSelectedInterests] = useState<Interest[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    if (!token) return;
    setLoading(true); setError('');
    try {
      const [nextProfile, nextEducation, nextSkills, nextExperience, nextInterests, nextSelected] = await Promise.all([profileApi.getProfile(token), profileApi.getEducation(token), profileApi.getSkills(token), profileApi.getExperience(token), profileApi.getInterests(token), profileApi.getUserInterests(token)]);
      setProfile(nextProfile); setEducation(nextEducation); setSkills(nextSkills); setExperience(nextExperience); setAvailableInterests(nextInterests); setSelectedInterests(nextSelected);
    } catch (loadError) { setError(loadError instanceof Error ? loadError.message : 'Unable to load your profile.'); } finally { setLoading(false); }
  }, [token]);
  // oxlint-disable-next-line react(set-state-in-effect)
  useEffect(() => { void load(); }, [load]);
  const completion = useMemo(() => { if (!profile) return 0; const sections = [Boolean(profile.location && profile.country), Boolean(profile.bio), education.length > 0, skills.length > 0, experience.length > 0, selectedInterests.length > 0]; return Math.round(sections.filter(Boolean).length / sections.length * 100); }, [education.length, experience.length, profile, selectedInterests.length, skills.length]);
  async function perform(action: () => Promise<void>, message: string) { setSaving(true); try { await action(); await load(); onNotify(message); } catch (saveError) { onNotify(saveError instanceof Error ? saveError.message : 'Unable to save changes.'); } finally { setSaving(false); } }
  if (loading) return <main className="auth-loading"><span className="loading-spinner" />Loading your profile...</main>;
  if (!profile || !token) return <main className="auth-loading"><span>{error || 'Your profile could not be loaded.'}</span><button className="button button--primary" type="button" onClick={() => onNavigate('/login')}>Return to sign in</button></main>;

  return <main className="profile-page"><header className="dashboard-nav"><a className="brand" href="/dashboard" onClick={(event) => { event.preventDefault(); onNavigate('/dashboard'); }}><span className="brand__mark"><span>G</span></span><span className="brand__name">GrowLink <em>AI</em></span></a><div className="profile-nav__links"><a href="/dashboard" onClick={(event) => { event.preventDefault(); onNavigate('/dashboard'); }}>Dashboard</a><button type="button" onClick={async () => { await signOut(); onNavigate('/'); }}>Sign out</button></div></header><div className="shell profile-content"><ProfileHeader profile={profile} onSignOut={async () => { await signOut(); onNavigate('/'); }} /><div className="profile-layout"><aside><ProfileCompletionIndicator percentage={completion} /><div className="profile-aside-note"><Icon name="spark" size={17} /><p>Your profile is yours to shape. Add the details that help the right people find you.</p></div></aside><div className="profile-sections"><ProfileInformationForm profile={profile} onSave={(data) => perform(async () => { const updated = await profileApi.updateProfile(token, data); setProfile(updated); }, 'Personal information saved.')} saving={saving} /><EducationSection items={education} onSave={(data, id) => perform(async () => { if (id) await profileApi.updateEducation(token, id, data); else await profileApi.addEducation(token, data); }, 'Education saved.')} onDelete={(id) => perform(async () => { await profileApi.deleteEducation(token, id); }, 'Education removed.')} /><SkillsSection items={skills} onSave={(data, id) => perform(async () => { if (id) await profileApi.updateSkill(token, id, data); else await profileApi.addSkill(token, data); }, 'Skill saved.')} onDelete={(id) => perform(async () => { await profileApi.deleteSkill(token, id); }, 'Skill removed.')} /><ExperienceSection items={experience} onSave={(data, id) => perform(async () => { if (id) await profileApi.updateExperience(token, id, data); else await profileApi.addExperience(token, data); }, 'Experience saved.')} onDelete={(id) => perform(async () => { await profileApi.deleteExperience(token, id); }, 'Experience removed.')} /><InterestsSection available={availableInterests} selected={selectedInterests} onSave={(ids) => perform(async () => { const updated = await profileApi.updateInterests(token, ids); setSelectedInterests(updated); }, 'Interests saved.')} /></div></div></div></main>;
}
