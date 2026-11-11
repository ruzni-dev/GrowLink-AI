import { useEffect, useState, type FormEvent } from 'react';
import { useAuth } from '../context/useAuth';
import { profileApi, type Skill } from '../services/profile';
import { mentorshipCategories, mentorshipService, type MentorshipMode, type OfferPayload } from '../services/mentorshipService';
import './mentorship.css';

type CreateMentorshipOfferProps = { onNavigate: (path: string) => void; onNotify: (message: string) => void };
const initialForm = { title: '', description: '', category: '', skillIds: [] as number[], experience: '', availability: '', preferredMode: 'ONLINE' as MentorshipMode, location: '' };

export function CreateMentorshipOffer({ onNavigate, onNotify }: CreateMentorshipOfferProps) {
  const { token, user } = useAuth();
  const [skills, setSkills] = useState<Skill[]>([]);
  const [form, setForm] = useState(initialForm);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    if (token) profileApi.getSkills(token).then((items) => { if (active) setSkills(items); })
      .catch((loadError: unknown) => { if (active) setError(loadError instanceof Error ? loadError.message : 'Unable to load your profile skills.'); })
      .finally(() => { if (active) setLoading(false); });
    else setLoading(false);
    return () => { active = false; };
  }, [token]);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!token || user?.status !== 'ACTIVE') { setError('An active signed-in account is required to offer mentorship.'); return; }
    if (!form.skillIds.length) { setError('Choose at least one skill already listed on your profile.'); return; }
    if (form.preferredMode !== 'ONLINE' && !form.location.trim()) { setError('Add a location for in-person mentorship.'); return; }
    setSaving(true); setError('');
    try {
      const payload: OfferPayload = { ...form, category: form.category, experience: form.experience || undefined, location: form.location || undefined };
      const offer = await mentorshipService.createOffer(token, payload);
      onNotify('Your mentorship offer is live.');
      onNavigate(`/mentors/${offer.id}`);
    } catch (saveError) {
      setError(saveError instanceof Error ? saveError.message : 'Unable to publish your offer.');
    } finally { setSaving(false); }
  }

  if (loading) return <main className="mentorship-state mentorship-state--page" role="status"><span className="loading-spinner" />Loading your skills...</main>;
  return <main className="mentorship-page"><header className="discovery-nav"><div className="shell discovery-nav__inner"><a className="brand" href="/mentors" onClick={(event) => { event.preventDefault(); onNavigate('/mentors'); }}><span className="brand__mark"><span>G</span></span><span className="brand__name">GrowLink <em>AI</em></span></a><button className="button button--outline" type="button" onClick={() => onNavigate('/my-mentorship')}>My mentorship</button></div></header><section className="mentorship-form-page shell"><p className="eyebrow">Share what you know</p><h1>Offer mentorship</h1><p className="mentorship-form-page__intro">Create a clear, practical offer using skills already on your GrowLink profile.</p>{skills.length === 0 && <div className="mentorship-notice"><span>Add at least one skill to your profile before publishing an offer.</span><button className="button button--outline" type="button" onClick={() => onNavigate('/profile')}>Edit profile</button></div>}<form className="mentorship-create-form" onSubmit={submit}><label className="mentorship-field"><span>Offer title</span><input value={form.title} onChange={(event) => setForm({ ...form, title: event.target.value })} maxLength={120} required placeholder="e.g. Weekly guidance for early-career developers" /></label><label className="mentorship-field"><span>Description</span><textarea rows={5} value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} maxLength={3000} minLength={30} required placeholder="Describe who you can help and what sessions might cover." /></label><div className="mentorship-form-grid"><label className="mentorship-field"><span>Category</span><select value={form.category} onChange={(event) => setForm({ ...form, category: event.target.value })} required><option value="">Select a category</option>{mentorshipCategories.map((category) => <option key={category.value} value={category.value}>{category.label}</option>)}</select></label><label className="mentorship-field"><span>Mentorship mode</span><select value={form.preferredMode} onChange={(event) => setForm({ ...form, preferredMode: event.target.value as MentorshipMode })}><option value="ONLINE">Online</option><option value="IN_PERSON">In person</option><option value="BOTH">Online or in person</option></select></label></div><fieldset className="mentorship-skill-picker"><legend>Skills offered</legend>{skills.length ? <div>{skills.map((skill) => <label key={skill.id}><input type="checkbox" checked={form.skillIds.includes(skill.skillId)} onChange={(event) => setForm({ ...form, skillIds: event.target.checked ? [...form.skillIds, skill.skillId] : form.skillIds.filter((id) => id !== skill.skillId) })} /><span>{skill.name}</span><small>{skill.proficiencyLevel.toLowerCase()}</small></label>)}</div> : <p>No skills found on this profile.</p>}</fieldset><label className="mentorship-field"><span>Experience and background</span><textarea rows={3} value={form.experience} onChange={(event) => setForm({ ...form, experience: event.target.value })} maxLength={1500} placeholder="Relevant experience or context" /></label><div className="mentorship-form-grid"><label className="mentorship-field"><span>Availability</span><input value={form.availability} onChange={(event) => setForm({ ...form, availability: event.target.value })} maxLength={500} required placeholder="e.g. Two evenings per month" /></label><label className="mentorship-field"><span>Location {form.preferredMode === 'ONLINE' ? '(optional)' : ''}</span><input value={form.location} onChange={(event) => setForm({ ...form, location: event.target.value })} maxLength={120} required={form.preferredMode !== 'ONLINE'} placeholder="City or region" /></label></div>{error && <p className="mentorship-form-error" role="alert">{error}</p>}<div className="mentorship-create-form__actions"><button className="button button--primary" type="submit" disabled={saving || !skills.length}>{saving ? 'Publishing...' : 'Publish offer'}</button><button className="button button--ghost" type="button" onClick={() => onNavigate('/mentors')}>Cancel</button></div></form></section></main>;
}