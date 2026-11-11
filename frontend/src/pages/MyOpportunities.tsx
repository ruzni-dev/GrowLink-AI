import { useCallback, useEffect, useState } from 'react';
import { OpportunityCard } from '../components/OpportunityCard';
import { useAuth } from '../context/useAuth';
import { opportunityService, type Opportunity, type OpportunityStatus, type Paged } from '../services/opportunityService';
import './opportunity.css';

type MyOpportunitiesProps = { onNavigate: (path: string) => void; onNotify: (message: string) => void };
const statuses: { value: OpportunityStatus | ''; label: string }[] = [{ value: '', label: 'All' }, { value: 'DRAFT', label: 'Drafts' }, { value: 'PUBLISHED', label: 'Published' }, { value: 'CLOSED', label: 'Closed' }, { value: 'ARCHIVED', label: 'Archived' }];

export function MyOpportunities({ onNavigate, onNotify }: MyOpportunitiesProps) {
  const { token } = useAuth();
  const [status, setStatus] = useState<OpportunityStatus | ''>('');
  const [page, setPage] = useState(0);
  const [results, setResults] = useState<Paged<Opportunity>>();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    if (!token) return;
    setLoading(true); setError('');
    try { setResults(await opportunityService.myOpportunities(token, page, 12, status || undefined)); }
    catch (loadError) { setError(loadError instanceof Error ? loadError.message : 'Unable to load your listings.'); }
    finally { setLoading(false); }
  }, [page, status, token]);
  useEffect(() => { void load(); }, [load]);

  async function changeStatus(id: number, next: OpportunityStatus) {
    if (!token) return;
    try { await opportunityService.changeStatus(token, id, next); onNotify(`Opportunity ${next.toLowerCase()}.`); await load(); }
    catch (statusError) { onNotify(statusError instanceof Error ? statusError.message : 'Unable to change opportunity status.'); }
  }

  return <main className="opportunity-page"><header className="discovery-nav"><div className="shell discovery-nav__inner"><a className="brand" href="/opportunities" onClick={(event) => { event.preventDefault(); onNavigate('/opportunities'); }}><span className="brand__mark"><span>G</span></span><span className="brand__name">GrowLink <em>AI</em></span></a><button className="button button--primary" type="button" onClick={() => onNavigate('/opportunities/create')}>Create listing</button></div></header><div className="shell my-opportunities"><div className="opportunity-page-heading"><div><p className="eyebrow">Your listings</p><h1>My opportunities</h1><p>Manage drafts, published listings and closed opportunities.</p></div></div><div className="opportunity-tabs" role="tablist" aria-label="Opportunity status filters">{statuses.map((item) => <button key={item.label} className={status === item.value ? 'opportunity-tab opportunity-tab--active' : 'opportunity-tab'} type="button" role="tab" aria-selected={status === item.value} onClick={() => { setStatus(item.value); setPage(0); }}>{item.label}</button>)}</div>{loading && <div className="opportunity-state" role="status"><span className="loading-spinner" />Loading your listings...</div>}{!loading && error && <div className="opportunity-state opportunity-state--error" role="alert"><h2>Listings unavailable</h2><p>{error}</p><button className="button button--outline" type="button" onClick={() => void load()}>Try again</button></div>}{!loading && !error && !results?.content.length && <div className="opportunity-state"><h2>No opportunities in this view</h2><p>Create a listing or select a different status.</p><button className="button button--primary" type="button" onClick={() => onNavigate('/opportunities/create')}>Create opportunity</button></div>}{!loading && !error && Boolean(results?.content.length) && <div className="opportunity-record-grid">{results?.content.map((opportunity) => <OpportunityCard key={opportunity.id} opportunity={opportunity} owned onView={(id) => onNavigate(`/opportunities/${id}`)} onEdit={(id) => onNavigate(`/opportunities/edit/${id}`)} onStatusChange={changeStatus} />)}</div>}{!loading && results && results.totalPages > 1 && <nav className="opportunity-pagination"><button className="button button--outline" type="button" disabled={results.first} onClick={() => setPage((current) => current - 1)}>Previous</button><span>Page {results.number + 1} of {results.totalPages}</span><button className="button button--outline" type="button" disabled={results.last} onClick={() => setPage((current) => current + 1)}>Next</button></nav>}</div></main>;
}