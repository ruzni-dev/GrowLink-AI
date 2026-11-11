import { useEffect, useState, type FormEvent } from 'react';
import { useAuth } from '../context/useAuth';
import { profileApi, type Skill } from '../services/profile';
import { mentorshipCategories, mentorshipService, type MentorshipMode, type MentorshipOffer, type OfferPayload } from '../services/mentorshipService';
import './mentorship.css';

type EditMentorshipOfferProps = { offerId: number; onNavigate: (path: string) => void; onNotify: (message: string) => void };
type EditForm = { title: string; description: string; category: string; skillIds: number[]; experience: string; availability: string; preferredMode: MentorshipMode; location: string };

export function EditMentorshipOffer({ offerId, onNavigate, onNotify }: EditMentorshipOfferProps) {
  const { token } = useAuth();
  const [offer, setOffer] = useState<MentorshipOffer>();
  const [skills, setSkills] = useState<Skill[]>([]);
  const [form, setForm] = useState<EditForm>({ title: '', description: '', category: '', skillIds: [], experience: '', availability: '', preferredMode: 'ONLINE', location: '' });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    if (!token) { setError('Sign in to edit an offer.'); setLoading(false); return; }
    Promise.all([mentorshipService.myOffers(token, 0, 50), profileApi.getSkills(token)])
      .then(([offers, profileSkills]) => {
        if (!active) return;
        const found = offers.content.find((item) => item.id === offerId);
        if (!found) { setError('This offer is not available to edit.'); return; }
        if (found.status === 'CLOSED') { setError('Closed offers cannot be edited.'); return; }
        setOffer(found);
        setSkills(profileSkills);
        setForm({ title: found.title, description: found.description, category: found.category, skillIds: found.skills.map((skill) => skill.id), experience: found.experience || '', availability: found.availability, preferredMode: found.preferredMode, location: found.location || '' });
      })
      .catch((loadError: unknown) => { if (active) setError(loadError instanceof Error ? loadError.message : 'Unable to load this offer.'); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [offerId, token]);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!token || !offer) return;
    if (!form.skillIds.length) { setError('Choose at least one skill already listed on your profile.'); return; }
    if (form.preferredMode !== 'ONLINE' && !form.location.trim()) { setError('Add a location for in-person mentorship.'); return; }
    setSaving(true); setError('');
    try {
      const payload: OfferPayload = { ...form, experience: form.experience || undefined, location: form.location || undefined };
      await mentorshipService.updateOffer(token, offer.id, payload);
      onNotify('Your mentorship offer was updated.');
      onNavigate('/my-mentorship');
    } catch (saveError) { setError(saveError instanceof Error ? saveError.message : 'Unable to update your offer.'); }
    finally { setSaving(false); }
  }

  if (loading) return <main className="mentorship-state mentorship-state--page" role="status"><span className="loading-spinner" />Loading your offer...</main>;
  if (!offer) return <main className="mentorship-state mentorship-state--page" role="alert"><h1>Offer unavailable</h1><p>{error}</p><button className="button button--outline" type="button" onClick={() => onNavigate('/my-mentorship')}>Back to my mentorship</button></main>;

  return <main className="mentorship-page"><header className="discovery-nav"><div className="shell discovery-nav__inner"><a className="brand" href="/my-mentorship" onClick={(event) => { event.preventDefault(); onNavigate('/my-mentorship'); }}><span className="brand__mark"><span>G</span></span><span className="brand__name">GrowLink <em>AI</em></span></a><button className="button button--outline" type="button" onClick={() => onNavigate('/my-mentorship')}>My mentorship</button></div></header><section className="mentorship-form-page shell"><p className="eyebrow">Share what you know</p><h1>Edit mentorship offer</h1><p className="mentorship-form-page__intro">Keep the offer details and your availability up to date.</p><form className="mentorship-create-form" onSubmit={submit}><label className="mentorship-field"><span>Offer title</span><input value={form.title} onChange={(event) => setForm({ ...form, title: event.target.value })} maxLength={120} required /></label><label className="mentorship-field"><span>Description</span><textarea rows={5} value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} maxLength={3000} minLength={30} required /></label><div className="mentorship-form-grid"><label className="mentorship-field"><span>Category</span><select value={form.category} onChange={(event) => setForm({ ...form, category: event.target.value })} required>{mentorshipCategories.map((category) => <option key={category.value} value={category.value}>{category.label}</option>)}</select></label><label className="mentorship-field"><span>Mentorship mode</span><select value={form.preferredMode} onChange={(event) => setForm({ ...form, preferredMode: event.target.value as MentorshipMode })}><option value="ONLINE">Online</option><option value="IN_PERSON">In person</option><option value="BOTH">Online or in person</option></select></label></div><fieldset className="mentorship-skill-picker"><legend>Skills offered</legend><div>{skills.map((skill) => <label key={skill.id}><input type="checkbox" checked={form.skillIds.includes(skill.skillId)} onChange={(event) => setForm({ ...form, skillIds: event.target.checked ? [...form.skillIds, skill.skillId] : form.skillIds.filter((id) => id !== skill.skillId) })} /><span>{skill.name}</span><small>{skill.proficiencyLevel.toLowerCase()}</small></label>)}</div></fieldset><label className="mentorship-field"><span>Experience and background</span><textarea rows={3} value={form.experience} onChange={(event) => setForm({ ...form, experience: event.target.value })} maxLength={1500} /></label><div className="mentorship-form-grid"><label className="mentorship-field"><span>Availability</span><input value={form.availability} onChange={(event) => setForm({ ...form, availability: event.target.value })} maxLength={500} required /></label><label className="mentorship-field"><span>Location {form.preferredMode === 'ONLINE' ? '(optional)' : ''}</span><input value={form.location} onChange={(event) => setForm({ ...form, location: event.target.value })} maxLength={120} required={form.preferredMode !== 'ONLINE'} /></label></div>{error && <p className="mentorship-form-error" role="alert">{error}</p>}<div className="mentorship-create-form__actions"><button className="button button--primary" type="submit" disabled={saving}>{saving ? 'Saving...' : 'Save changes'}</button><button className="button button--ghost" type="button" onClick={() => onNavigate('/my-mentorship')}>Cancel</button></div></form></section></main>;
}