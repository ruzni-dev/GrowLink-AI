import { useEffect, useState } from 'react';
import { useAuth } from '../context/useAuth';
import { opportunityService, type Opportunity, type OpportunityApplicationStatus } from '../services/opportunityService';
import './opportunity.css';

type OpportunityDetailsProps = { id: number; onNavigate: (path: string) => void; onNotify: (message: string) => void };

export function OpportunityDetails({ id, onNavigate, onNotify }: OpportunityDetailsProps) {
  const { token, user } = useAuth();
  const [opportunity, setOpportunity] = useState<Opportunity>();
  const [saved, setSaved] = useState(false);
  const [status, setStatus] = useState<OpportunityApplicationStatus>('INTERESTED');
  const [notes, setNotes] = useState('');
  const [tracking, setTracking] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    const savedPromise = token ? opportunityService.saved(token, 0, 50).catch(() => undefined) : Promise.resolve(undefined);
    Promise.all([opportunityService.get(id), savedPromise]).then(([result, savedPage]) => {
      if (!active) return;
      setOpportunity(result);
      setSaved(Boolean(savedPage?.content.some((item) => item.opportunity.id === id)));
    }).catch((loadError: unknown) => { if (active) setError(loadError instanceof Error ? loadError.message : 'Opportunity unavailable.'); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [id, token]);

  async function toggleSaved() {
    if (!token || !user) { onNavigate('/login'); return; }
    if (user.status !== 'ACTIVE') { onNotify('An active account is required to save opportunities.'); return; }
    try {
      if (saved) await opportunityService.unsave(token, id);
      else await opportunityService.save(token, id);
      setSaved(!saved);
    } catch (saveError) { onNotify(saveError instanceof Error ? saveError.message : 'Unable to update saved opportunities.'); }
  }

  async function track() {
    if (!token || !user) { onNavigate('/login'); return; }
    if (user.status !== 'ACTIVE') { setError('An active account is required to track applications.'); return; }
    setTracking(true); setError('');
    try {
      await opportunityService.track(token, id, status, notes);
      onNotify('Opportunity added to your application tracker.');
      onNavigate('/application-tracker');
    } catch (trackError) { setError(trackError instanceof Error ? trackError.message : 'Unable to track this opportunity.'); }
    finally { setTracking(false); }
  }

  async function share() {
    const url = window.location.href;
    try {
      if (navigator.share) await navigator.share({ title: opportunity?.title, url });
      else { await navigator.clipboard.writeText(url); onNotify('Opportunity link copied.'); }
    } catch { onNotify('Unable to share this opportunity from this browser.'); }
  }

  if (loading) return <main className="opportunity-state opportunity-state--page" role="status"><span className="loading-spinner" />Loading opportunity...</main>;
  if (error && !opportunity) return <main className="opportunity-state opportunity-state--page" role="alert"><h1>Opportunity unavailable</h1><p>{error}</p><button className="button button--outline" type="button" onClick={() => onNavigate('/opportunities')}>Back to opportunities</button></main>;
  if (!opportunity) return null;

  return <main className="opportunity-page"><header className="discovery-nav"><div className="shell discovery-nav__inner"><a className="brand" href="/opportunities" onClick={(event) => { event.preventDefault(); onNavigate('/opportunities'); }}><span className="brand__mark"><span>G</span></span><span className="brand__name">GrowLink <em>AI</em></span></a><button className="button button--outline" type="button" onClick={() => onNavigate('/opportunities')}>Back to search</button></div></header><div className="shell opportunity-detail-layout"><article className="opportunity-detail"><div className="opportunity-detail__top"><span className={`opportunity-category opportunity-category--${opportunity.category.toLowerCase()}`}>{opportunity.category}</span><button className="button button--ghost" type="button" onClick={() => void share()} aria-label="Share opportunity">Share</button></div><h1>{opportunity.title}</h1><p className="opportunity-detail__organization">{opportunity.organizationName}{opportunity.opportunityType ? ` · ${opportunity.opportunityType}` : ''}</p><div className="opportunity-detail__facts"><span>{opportunity.remote ? 'Remote' : opportunity.location || 'On-site'}</span><span>{opportunity.deadline ? `Deadline ${new Date(`${opportunity.deadline}T00:00:00`).toLocaleDateString()}` : 'No deadline listed'}</span></div><section><h2>About this opportunity</h2><p>{opportunity.description}</p></section>{opportunity.eligibility && <section><h2>Eligibility</h2><p>{opportunity.eligibility}</p></section>}<section><h2>Required skills</h2>{opportunity.requiredSkills.length ? <div className="opportunity-record__skills">{opportunity.requiredSkills.map((skill) => <span key={skill}>{skill}</span>)}</div> : <p>No specific skills listed.</p>}</section>{opportunity.qualification && <section><h2>Qualifications</h2><p>{opportunity.qualification}</p></section>}{opportunity.contactInformation && <section><h2>Contact information</h2><p>{opportunity.contactInformation}</p></section>}<div className="opportunity-detail__actions"><button className={saved ? 'button button--saved' : 'button button--outline'} type="button" onClick={() => void toggleSaved()}>{saved ? 'Saved' : 'Save opportunity'}</button>{opportunity.applicationUrl && <a className="button button--primary" href={opportunity.applicationUrl} target="_blank" rel="noopener noreferrer nofollow">Open application site <span aria-hidden="true">↗</span></a>}</div></article><aside className="opportunity-track-panel"><p className="eyebrow">Your next step</p><h2>Track your progress</h2><p>GrowLink keeps a personal reminder only. Submit the application directly through the organization’s website.</p><label className="opportunity-field"><span>Current status</span><select value={status} onChange={(event) => setStatus(event.target.value as OpportunityApplicationStatus)}><option value="INTERESTED">Interested</option><option value="APPLIED">Applied</option></select></label><label className="opportunity-field"><span>Private notes</span><textarea rows={4} maxLength={4000} value={notes} onChange={(event) => setNotes(event.target.value)} placeholder="Optional reminders for yourself" /></label>{error && <p className="opportunity-form__error" role="alert">{error}</p>}<button className="button button--primary" type="button" onClick={() => void track()} disabled={tracking}>{tracking ? 'Saving...' : 'Add to application tracker'}</button></aside></div></main>;
}