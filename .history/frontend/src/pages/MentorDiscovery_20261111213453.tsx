import { useEffect, useState, type FormEvent } from 'react';
import { MentorshipOfferCard } from '../components/MentorshipOfferCard';
import { Icon } from '../components/Icon';
import { mentorshipCategories, mentorshipService, type MentorshipCategory, type MentorshipOffer, type PagedResponse } from '../services/mentorshipService';
import './mentorship.css';

type MentorDiscoveryProps = { onNavigate: (path: string) => void };

export function MentorDiscovery({ onNavigate }: MentorDiscoveryProps) {
  const [offers, setOffers] = useState<PagedResponse<MentorshipOffer>>();
  const [categories] = useState<MentorshipCategory[]>(mentorshipCategories);
  const [query, setQuery] = useState({ q: '', category: '', location: '', page: 0 });
  const [form, setForm] = useState(query);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    setLoading(true); setError('');
    mentorshipService.findOffers({ ...query, size: 9 })
      .then((result) => { if (active) setOffers(result); })
      .catch((loadError: unknown) => { if (active) setError(loadError instanceof Error ? loadError.message : 'Mentorship offers are unavailable.'); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [query]);

  function search(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setQuery({ ...form, page: 0 });
  }

  function clear() {
    const blank = { q: '', category: '', location: '', page: 0 };
    setForm(blank); setQuery(blank);
  }

  return <main className="mentorship-page"><header className="discovery-nav"><div className="shell discovery-nav__inner"><a className="brand" href="/" onClick={(event) => { event.preventDefault(); onNavigate('/'); }}><span className="brand__mark"><span>G</span></span><span className="brand__name">GrowLink <em>AI</em></span></a><nav aria-label="Mentorship navigation"><a href="/talents" onClick={(event) => { event.preventDefault(); onNavigate('/talents'); }}>Talent</a><a href="/mentors" aria-current="page">Mentors</a><a href="/my-mentorship" onClick={(event) => { event.preventDefault(); onNavigate('/my-mentorship'); }}>My mentorship</a></nav></div></header><section className="mentorship-hero"><div className="shell"><p className="eyebrow">Mentorship & community</p><h1>Grow with someone<br /><span>who has been there.</span></h1><p>Learn from people in your community who are ready to share their experience.</p></div></section><div className="shell mentorship-layout"><form className="mentorship-filters" onSubmit={search}><div className="mentorship-section-label"><span>01</span><div><h2>Find a mentor</h2><p>Explore active offers from public profiles.</p></div></div><label className="mentorship-field"><span>Search</span><input value={form.q} onChange={(event) => setForm({ ...form, q: event.target.value })} placeholder="Name, skill or topic" maxLength={160} /></label><label className="mentorship-field"><span>Category</span><select value={form.category} onChange={(event) => setForm({ ...form, category: event.target.value })}><option value="">All categories</option>{categories.map((category) => <option key={category.value} value={category.value}>{category.label}</option>)}</select></label><label className="mentorship-field"><span>Location</span><input value={form.location} onChange={(event) => setForm({ ...form, location: event.target.value })} placeholder="City or region" maxLength={120} /></label><div className="mentorship-filter-actions"><button className="button button--primary" type="submit"><Icon name="search" size={16} /> Search</button><button className="button button--ghost" type="button" onClick={clear}>Clear</button></div><button className="button button--outline mentorship-create-link" type="button" onClick={() => onNavigate('/mentorship/create')}>Offer mentorship <Icon name="arrow" size={15} /></button></form><section className="mentorship-results" aria-live="polite"><div className="mentorship-results__heading"><div><p className="eyebrow">Community mentors</p><h2>{loading ? 'Finding mentors' : `${offers?.totalElements ?? 0} available offers`}</h2></div></div>{loading && <div className="mentorship-state" role="status"><span className="loading-spinner" />Loading mentorship offers...</div>}{!loading && error && <div className="mentorship-state mentorship-state--error" role="alert"><h3>Offers could not be loaded</h3><p>{error}</p><button className="button button--outline" type="button" onClick={() => setQuery({ ...query })}>Try again</button></div>}{!loading && !error && !offers?.content.length && <div className="mentorship-state"><span className="mentorship-state__symbol">↗</span><h3>No matching offers yet</h3><p>Try a different search, category or location.</p><button className="button button--outline" type="button" onClick={clear}>Clear filters</button></div>}{!loading && !error && Boolean(offers?.content.length) && <div className="mentorship-offer-grid">{offers?.content.map((offer) => <MentorshipOfferCard key={offer.id} offer={offer} onView={(id) => onNavigate(`/mentors/${id}`)} onRequest={(id) => onNavigate(`/mentors/${id}`)} />)}</div>}{!loading && !error && offers && offers.totalPages > 1 && <nav className="mentorship-pagination" aria-label="Mentorship offer pages"><button className="button button--outline" type="button" disabled={query.page === 0} onClick={() => setQuery((current) => ({ ...current, page: current.page - 1 }))}>Previous</button><span>Page {query.page + 1} of {offers.totalPages}</span><button className="button button--outline" type="button" disabled={offers.last} onClick={() => setQuery((current) => ({ ...current, page: current.page + 1 }))}>Next</button></nav>}</section></div></main>;
}