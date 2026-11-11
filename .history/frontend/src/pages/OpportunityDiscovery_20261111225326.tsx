import { useEffect, useState, type FormEvent } from 'react';
import { OpportunityCard } from '../components/OpportunityCard';
import { OpportunityFilters } from '../components/OpportunityFilters';
import { useAuth } from '../context/useAuth';
import { opportunityService, type Opportunity, type OpportunityCategoryOption, type OpportunityFilters as Filters, type Paged } from '../services/opportunityService';
import './opportunity.css';

type OpportunityDiscoveryProps = { onNavigate: (path: string) => void; onNotify: (message: string) => void };
const empty: Filters = { page: 0, size: 9, sortBy: 'recent' };

function initialFilters(): Filters {
  const search = new URLSearchParams(window.location.search);
  return { ...empty, q: search.get('q') || '', category: search.get('category') || '' };
}

export function OpportunityDiscovery({ onNavigate, onNotify }: OpportunityDiscoveryProps) {
  const { token, user } = useAuth();
  const [form, setForm] = useState<Filters>(initialFilters);
  const [query, setQuery] = useState<Filters>(initialFilters);
  const [categories, setCategories] = useState<OpportunityCategoryOption[]>([]);
  const [results, setResults] = useState<Paged<Opportunity>>();
  const [savedIds, setSavedIds] = useState<Set<number>>(new Set());
  const [savingId, setSavingId] = useState<number>();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    setLoading(true); setError('');
    Promise.all([
      opportunityService.search(query),
      opportunityService.categories(),
      token ? opportunityService.saved(token, 0, 50).catch(() => undefined) : Promise.resolve(undefined),
    ]).then(([page, categoryOptions, savedPage]) => {
      if (!active) return;
      setResults(page); setCategories(categoryOptions);
      if (savedPage) setSavedIds(new Set(savedPage.content.map((item) => item.opportunity.id)));
    }).catch((loadError: unknown) => {
      if (active) setError(loadError instanceof Error ? loadError.message : 'Unable to load opportunities.');
    }).finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [query, token]);

  function search(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setQuery({ ...form, page: 0, size: 9 });
  }

  function clear() {
    setForm(empty); setQuery(empty);
  }

  async function toggleSave(id: number) {
    if (savingId === id) return;
    if (!token || !user) { onNavigate('/login'); return; }
    if (user.status !== 'ACTIVE') { onNotify('An active account is required to save opportunities.'); return; }
    setSavingId(id);
    try {
      if (savedIds.has(id)) { await opportunityService.unsave(token, id); setSavedIds((current) => { const next = new Set(current); next.delete(id); return next; }); }
      else { await opportunityService.save(token, id); setSavedIds((current) => new Set(current).add(id)); }
    } catch (saveError) { onNotify(saveError instanceof Error ? saveError.message : 'Unable to update saved opportunities.'); }
    finally { setSavingId(undefined); }
  }

  return <main className="opportunity-page"><header className="discovery-nav"><div className="shell discovery-nav__inner"><a className="brand" href="/" onClick={(event) => { event.preventDefault(); onNavigate('/'); }}><span className="brand__mark"><span>G</span></span><span className="brand__name">GrowLink <em>AI</em></span></a><nav aria-label="Opportunity navigation"><a href="/mentors" onClick={(event) => { event.preventDefault(); onNavigate('/mentors'); }}>Mentorship</a><a href="/opportunities" aria-current="page">Opportunities</a><a href="/my-opportunities" onClick={(event) => { event.preventDefault(); onNavigate('/my-opportunities'); }}>My listings</a></nav></div></header><section className="opportunity-hero"><div className="shell"><p className="eyebrow">Learning, work and community</p><h1>Find your next<br /><span>meaningful opportunity.</span></h1><p>Explore current opportunities shared by the GrowLink community.</p></div></section><div className="shell opportunity-layout"><OpportunityFilters value={form} categories={categories} onChange={setForm} onSearch={search} onClear={clear} /><section className="opportunity-results" aria-live="polite"><div className="opportunity-results__heading"><div><p className="eyebrow">Open opportunities</p><h2>{loading ? 'Searching' : `${results?.totalElements ?? 0} opportunities`}</h2></div><div className="opportunity-results__links"><button className="button button--outline" type="button" onClick={() => onNavigate('/saved-opportunities')}>Saved</button><button className="button button--primary" type="button" onClick={() => onNavigate('/opportunities/create')}>Post opportunity</button></div></div>{loading && <div className="opportunity-state" role="status"><span className="loading-spinner" />Loading opportunities...</div>}{!loading && error && <div className="opportunity-state opportunity-state--error" role="alert"><h3>Opportunities could not be loaded</h3><p>{error}</p><button className="button button--outline" type="button" onClick={() => setQuery({ ...query })}>Try again</button></div>}{!loading && !error && !results?.content.length && <div className="opportunity-state"><span className="opportunity-state__symbol">↗</span><h3>No matching opportunities</h3><p>Try broadening your search or clearing the filters.</p><button className="button button--outline" type="button" onClick={clear}>Clear filters</button></div>}{!loading && !error && Boolean(results?.content.length) && <div className="opportunity-record-grid">{results?.content.map((opportunity) => <OpportunityCard key={opportunity.id} opportunity={opportunity} saved={savedIds.has(opportunity.id)} onSave={(id) => void toggleSave(id)} onView={(id) => onNavigate(`/opportunities/${id}`)} />)}</div>}{!loading && !error && results && results.totalPages > 1 && <nav className="opportunity-pagination" aria-label="Opportunity pages"><button className="button button--outline" type="button" disabled={results.first} onClick={() => setQuery((current) => ({ ...current, page: (current.page || 0) - 1 }))}>Previous</button><span>Page {results.number + 1} of {results.totalPages}</span><button className="button button--outline" type="button" disabled={results.last} onClick={() => setQuery((current) => ({ ...current, page: (current.page || 0) + 1 }))}>Next</button></nav>}</section></div></main>;
}