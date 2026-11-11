import { useEffect, useState, type FormEvent } from 'react';
import { Icon } from '../components/Icon';
import { TALENT_CATEGORY_OPTIONS, talentService, type TalentCategory, type TalentPage, type TalentSearchParams } from '../services/talentService';

type TalentDiscoveryProps = { onNavigate: (path: string) => void };
type Filters = { name: string; skill: string; category: string; location: string; proficiencyLevel: string; minimumExperience: string; education: string };
const emptyFilters: Filters = { name: '', skill: '', category: '', location: '', proficiencyLevel: '', minimumExperience: '', education: '' };

export function TalentDiscovery({ onNavigate }: TalentDiscoveryProps) {
  const [filters, setFilters] = useState(emptyFilters);
  const [query, setQuery] = useState<TalentSearchParams>({ page: 0, size: 12, sortBy: 'name' });
  const [categories, setCategories] = useState<TalentCategory[]>(TALENT_CATEGORY_OPTIONS);
  const [results, setResults] = useState<TalentPage>();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');
    Promise.all([talentService.search(query), talentService.getCategories()])
      .then(([page, options]) => {
        if (!active) return;
        setResults(page);
        setCategories(options);
      })
      .catch((loadError: unknown) => {
        if (active) setError(loadError instanceof Error ? loadError.message : 'Unable to load talent right now.');
      })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [query]);

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setQuery({
      ...Object.fromEntries(Object.entries(filters).filter(([, value]) => value.trim() !== '')),
      minimumExperience: filters.minimumExperience ? Number(filters.minimumExperience) : undefined,
      page: 0,
      size: query.size,
      sortBy: query.sortBy,
    });
  }

  function clearFilters() {
    setFilters(emptyFilters);
    setQuery({ page: 0, size: query.size, sortBy: query.sortBy });
  }

  function updateSort(sortBy: string) {
    setQuery((current) => ({ ...current, sortBy, page: 0 }));
  }

  const page = results?.number ?? 0;
  const totalPages = results?.totalPages ?? 0;

  return <main className="talent-discovery">
    <header className="discovery-nav"><div className="shell discovery-nav__inner"><a className="brand" href="/" onClick={(event) => { event.preventDefault(); onNavigate('/'); }}><span className="brand__mark"><span>G</span></span><span className="brand__name">GrowLink <em>AI</em></span></a><nav aria-label="Discovery navigation"><a href="/dashboard" onClick={(event) => { event.preventDefault(); onNavigate('/dashboard'); }}>Dashboard</a><a href="/talents" aria-current="page">Discover people</a></nav></div></header>
    <section className="discovery-intro"><div className="shell"><p className="eyebrow">A people-first directory</p><h1>Find the person<br /><span>who moves you forward.</span></h1><p>Search public profiles by skills, experience, learning and location.</p></div></section>
    <section className="shell discovery-workspace" aria-label="Talent search">
      <form className="discovery-filters" onSubmit={submit}>
        <div className="discovery-filters__heading"><div><span className="discovery-index">01</span><div><h2>Refine your search</h2><p>Only profiles shared publicly are shown.</p></div></div><button className="discovery-clear" type="button" onClick={clearFilters}>Clear filters</button></div>
        <label className="discovery-field discovery-field--wide"><span>Name</span><input value={filters.name} onChange={(event) => setFilters({ ...filters, name: event.target.value })} placeholder="Search people" maxLength={120} /></label>
        <div className="discovery-filters__grid">
          <label className="discovery-field"><span>Skill</span><input value={filters.skill} onChange={(event) => setFilters({ ...filters, skill: event.target.value })} placeholder="e.g. Java" maxLength={120} /></label>
          <label className="discovery-field"><span>Category</span><select value={filters.category} onChange={(event) => setFilters({ ...filters, category: event.target.value })}><option value="">All categories</option>{categories.map((category) => <option key={category.value} value={category.value}>{category.label}</option>)}</select></label>
          <label className="discovery-field"><span>Location</span><input value={filters.location} onChange={(event) => setFilters({ ...filters, location: event.target.value })} placeholder="City or region" maxLength={120} /></label>
          <label className="discovery-field"><span>Proficiency</span><select value={filters.proficiencyLevel} onChange={(event) => setFilters({ ...filters, proficiencyLevel: event.target.value })}><option value="">Any level</option><option value="BEGINNER">Beginner</option><option value="INTERMEDIATE">Intermediate</option><option value="ADVANCED">Advanced</option><option value="EXPERT">Expert</option></select></label>
          <label className="discovery-field"><span>Minimum experience</span><input type="number" min="0" max="80" value={filters.minimumExperience} onChange={(event) => setFilters({ ...filters, minimumExperience: event.target.value })} placeholder="Years" /></label>
          <label className="discovery-field"><span>Education</span><input value={filters.education} onChange={(event) => setFilters({ ...filters, education: event.target.value })} placeholder="Qualification or field" maxLength={160} /></label>
        </div>
        <button className="button button--primary discovery-submit" type="submit"><Icon name="search" size={17} /> Search people</button>
      </form>

      <section className="discovery-results" aria-live="polite">
        <div className="discovery-results__bar"><div><p className="eyebrow">The network</p><h2>{loading ? 'Finding people' : `${results?.totalElements ?? 0} public ${results?.totalElements === 1 ? 'profile' : 'profiles'}`}</h2></div><label className="discovery-sort"><span>Sort by</span><select value={query.sortBy} onChange={(event) => updateSort(event.target.value)}><option value="name">Name</option><option value="location">Location</option><option value="recent">Recently joined</option></select></label></div>
        {loading && <div className="discovery-state" role="status"><span className="loading-spinner" />Loading public profiles...</div>}
        {!loading && error && <div className="discovery-state discovery-state--error" role="alert"><h3>We couldn’t load the directory</h3><p>{error}</p><button className="button button--outline" type="button" onClick={() => setQuery({ ...query })}>Try again</button></div>}
        {!loading && !error && results?.content.length === 0 && <div className="discovery-state"><span className="discovery-state__mark">?</span><h3>No matching profiles yet</h3><p>Try a broader skill, category or location.</p><button className="button button--outline" type="button" onClick={clearFilters}>Clear filters</button></div>}
        {!loading && !error && Boolean(results?.content.length) && <div className="discovery-grid">{results?.content.map((talent) => <article className="discovery-card" key={talent.id}><div className="discovery-card__top"><div className="discovery-avatar">{talent.profileImage ? <img src={talent.profileImage} alt="" referrerPolicy="no-referrer" /> : <span>{talent.fullName.split(/\s+/).slice(0, 2).map((part) => part[0]).join('').toUpperCase()}</span>}</div><div className="discovery-card__identity"><h3>{talent.fullName}</h3><p>{[talent.location, talent.country].filter(Boolean).join(', ') || 'Location not listed'}</p></div><span className="discovery-card__spark" aria-hidden="true">✦</span></div><p className="discovery-card__bio">{talent.bio || 'This member has not added an introduction yet.'}</p><div className="discovery-card__skills">{talent.skills.slice(0, 4).map((skill) => <span key={skill.name}>{skill.name}<small>{skill.proficiencyLevel.toLowerCase()}</small></span>)}{talent.skills.length > 4 && <span>+{talent.skills.length - 4} more</span>}</div><button className="button button--outline discovery-card__action" type="button" onClick={() => onNavigate(`/talents/${talent.id}`)}>View profile <Icon name="arrow" size={15} /></button></article>)}</div>}
        {!loading && !error && totalPages > 1 && <nav className="discovery-pagination" aria-label="Talent results pages"><button className="button button--outline" type="button" disabled={page === 0} onClick={() => setQuery((current) => ({ ...current, page: page - 1 }))}>Previous</button><span>Page {page + 1} of {totalPages}</span><button className="button button--outline" type="button" disabled={Boolean(results?.last)} onClick={() => setQuery((current) => ({ ...current, page: page + 1 }))}>Next</button></nav>}
      </section>
    </section>
  </main>;
}