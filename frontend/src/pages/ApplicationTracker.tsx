import { useCallback, useEffect, useState } from 'react';
import { OpportunityApplicationCard } from '../components/OpportunityApplicationCard';
import { useAuth } from '../context/useAuth';
import { opportunityService, type OpportunityApplication, type OpportunityApplicationStatus, type Paged } from '../services/opportunityService';
import './opportunity.css';

type ApplicationTrackerProps = { onNavigate: (path: string) => void; onNotify: (message: string) => void };

export function ApplicationTracker({ onNavigate, onNotify }: ApplicationTrackerProps) {
  const { token } = useAuth();
  const [page, setPage] = useState(0);
  const [results, setResults] = useState<Paged<OpportunityApplication>>();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    if (!token) return;
    setLoading(true); setError('');
    try { setResults(await opportunityService.applications(token, page, 12)); }
    catch (loadError) { setError(loadError instanceof Error ? loadError.message : 'Unable to load application records.'); }
    finally { setLoading(false); }
  }, [page, token]);
  useEffect(() => { void load(); }, [load]);

  async function changeStatus(id: number, status: OpportunityApplicationStatus) {
    if (!token) return;
    try { await opportunityService.changeApplicationStatus(token, id, status); onNotify('Application status updated.'); await load(); }
    catch (statusError) { onNotify(statusError instanceof Error ? statusError.message : 'Unable to update application status.'); }
  }

  async function saveNotes(id: number, notes: string) {
    if (!token) return;
    try { await opportunityService.updateApplicationNotes(token, id, notes); onNotify('Private notes saved.'); await load(); }
    catch (notesError) { onNotify(notesError instanceof Error ? notesError.message : 'Unable to save notes.'); }
  }

  return <main className="opportunity-page"><header className="discovery-nav"><div className="shell discovery-nav__inner"><a className="brand" href="/opportunities" onClick={(event) => { event.preventDefault(); onNavigate('/opportunities'); }}><span className="brand__mark"><span>G</span></span><span className="brand__name">GrowLink <em>AI</em></span></a><button className="button button--outline" type="button" onClick={() => onNavigate('/saved-opportunities')}>Saved</button></div></header><div className="shell my-opportunities"><p className="eyebrow">Personal progress</p><h1>Application tracker</h1><p className="opportunity-tracker-intro">Update your progress manually. Applications are submitted on the organization’s external website.</p>{loading && <div className="opportunity-state" role="status"><span className="loading-spinner" />Loading your tracker...</div>}{!loading && error && <div className="opportunity-state opportunity-state--error" role="alert"><p>{error}</p><button className="button button--outline" type="button" onClick={() => void load()}>Try again</button></div>}{!loading && !error && !results?.content.length && <div className="opportunity-state"><h2>Your tracker is empty</h2><p>Open an opportunity and add it to your tracker after deciding to pursue it.</p><button className="button button--primary" type="button" onClick={() => onNavigate('/opportunities')}>Explore opportunities</button></div>}{!loading && !error && Boolean(results?.content.length) && <div className="opportunity-application-list">{results?.content.map((application) => <OpportunityApplicationCard key={application.id} application={application} onStatusChange={changeStatus} onNotesSave={saveNotes} />)}</div>}{!loading && results && results.totalPages > 1 && <nav className="opportunity-pagination"><button className="button button--outline" type="button" disabled={results.first} onClick={() => setPage((current) => current - 1)}>Previous</button><span>Page {results.number + 1} of {results.totalPages}</span><button className="button button--outline" type="button" disabled={results.last} onClick={() => setPage((current) => current + 1)}>Next</button></nav>}</div></main>;
}