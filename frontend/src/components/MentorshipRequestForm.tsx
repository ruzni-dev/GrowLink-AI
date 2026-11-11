import { useState, type FormEvent } from 'react';
import { useAuth } from '../context/useAuth';
import { mentorshipService, type MentorshipMode } from '../services/mentorshipService';

type MentorshipRequestFormProps = { offerId: number; offerMode: MentorshipMode; onNavigate: (path: string) => void; onSubmitted: () => void };

export function MentorshipRequestForm({ offerId, offerMode, onNavigate, onSubmitted }: MentorshipRequestFormProps) {
  const { token, user } = useAuth();
  const [message, setMessage] = useState('');
  const [goals, setGoals] = useState('');
  const [preferredMode, setPreferredMode] = useState<MentorshipMode>(offerMode === 'BOTH' ? 'ONLINE' : offerMode);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!token || !user) { onNavigate('/login'); return; }
    if (user.status !== 'ACTIVE') { setError('Your account must be active to request mentorship.'); return; }
    if (message.trim().length < 20 || goals.trim().length < 10) { setError('Add a message of at least 20 characters and learning goals of at least 10 characters.'); return; }
    setSubmitting(true); setError('');
    try {
      await mentorshipService.createRequest(token, { offerId, message: message.trim(), goals: goals.trim(), preferredMode });
      onSubmitted();
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Unable to send your request.');
    } finally { setSubmitting(false); }
  }

  return <form className="mentorship-request-form" onSubmit={submit}><p className="eyebrow">Start a conversation</p><h2>Request mentorship</h2><label className="mentorship-field"><span>Your message</span><textarea rows={4} maxLength={2000} value={message} onChange={(event) => setMessage(event.target.value)} placeholder="Introduce yourself and share why this mentor's experience interests you." required minLength={20} /></label><label className="mentorship-field"><span>What would you like to learn?</span><textarea rows={3} maxLength={2000} value={goals} onChange={(event) => setGoals(event.target.value)} placeholder="Describe your learning goals." required minLength={10} /></label><label className="mentorship-field"><span>Preferred mode</span><select value={preferredMode} onChange={(event) => setPreferredMode(event.target.value as MentorshipMode)}>{(offerMode === 'BOTH' ? ['ONLINE', 'IN_PERSON'] : [offerMode]).map((mode) => <option key={mode} value={mode}>{mode === 'IN_PERSON' ? 'In person' : 'Online'}</option>)}</select></label>{error && <p className="mentorship-form-error" role="alert">{error}</p>}<button className="button button--primary" type="submit" disabled={submitting}>{submitting ? 'Sending...' : 'Send mentorship request'}</button></form>;
}