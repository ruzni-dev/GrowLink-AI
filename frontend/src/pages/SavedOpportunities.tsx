import { useCallback, useEffect, useState } from 'react';
import { OpportunityCard } from '../components/OpportunityCard';
import { useAuth } from '../context/useAuth';
import { opportunityService, type Paged, type SavedOpportunity } from '../services/opportunityService';
import './opportunity.css';

type SavedOpportunitiesProps = { onNavigate: (path: string) => void; onNotify: (message: string) => void };

export function SavedOpportunities({ onNavigate, onNotify }: SavedOpportunitiesProps) {
  const { token } = useAuth();
  const [page, setPage] = useState(0);
  const [results, setResults] = useState<Paged<SavedOpportunity>>();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    if (!token) return;
    setLoading(true); setError('');
    try { setResults(await opportunityService.saved(token, page, 12)); }
    catch (loadError) { setError(loadError instanceof Error ? loadError.message : 'Unable to load saved opportunities.'); }
    finally { setLoading(false); }
  }, [page, token]);
  useEffect(() => { void load(); }, [load]);

  async function remove(id: number) {
    if (!token) return;
    try { await opportunityService.unsave(token, id); onNotify('Removed from saved opportunities.'); await load(); }
    catch (removeError) { onNotify(removeError instanceof Error ? removeError.message : 'Unable to remove the bookmark.'); }
  }

  return <main className="opportunity-page"><header className="discovery-nav"><div className="shell discovery-nav__inner"><a className="brand" href="/opportunities" onClick={(event) => { event.preventDefault(); onNavigate('/opportunities'); }}><span className="brand__mark"><span>G</span></span><span className="brand__name">GrowLink <em>AI</em></span></a><button className="button button--outline" type="button" onClick={() => onNavigate('/application-tracker')}>Application tracker</button></div></header><div className="shell my-opportunities"><p className="eyebrow">Your shortlist</p><h1>Saved opportunities</h1>{loading && <div className="opportunity-state" role="status"><span className="loading-spinner" />Loading saved opportunities...</div>}{!loading && error && <div className="opportunity-state opportunity-state--error" role="alert"><p>{error}</p><button className="button button--outline" type="button" onClick={() => void load()}>Try again</button></div>}{!loading && !error && !results?.content.length && <div className="opportunity-state"><h2>No saved opportunities yet</h2><p>Bookmark listings you want to revisit.</p><button className="button button--primary" type="button" onClick={() => onNavigate('/opportunities')}>Explore opportunities</button></div>}{!loading && !error && Boolean(results?.content.length) && <div className="opportunity-record-grid">{results?.content.map((saved) => <OpportunityCard key={saved.id} opportunity={saved.opportunity} saved onSave={remove} onView={(id) => onNavigate(`/opportunities/${id}`)} />)}</div>}{!loading && results && results.totalPages > 1 && <nav className="opportunity-pagination"><button className="button button--outline" type="button" disabled={results.first} onClick={() => setPage((current) => current - 1)}>Previous</button><span>Page {results.number + 1} of {results.totalPages}</span><button className="button button--outline" type="button" disabled={results.last} onClick={() => setPage((current) => current + 1)}>Next</button></nav>}</div></main>;
}