import { useMemo, useState } from 'react';
import type { Talent } from '../data/mockData';
import { Icon } from './Icon';
import { SectionHeading } from './SectionHeading';

type TalentSearchProps = {
  talents: Talent[];
};

export function TalentSearch({ talents }: TalentSearchProps) {
  const [query, setQuery] = useState('');
  const [category, setCategory] = useState('All categories');
  const [location, setLocation] = useState('Anywhere');
  const [hasSearched, setHasSearched] = useState(false);

  const results = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();
    if (!normalizedQuery && category === 'All categories' && location === 'Anywhere') return talents;
    return talents.filter((talent) => {
      const matchesQuery = !normalizedQuery || [talent.name, talent.role, talent.location, ...talent.skills].join(' ').toLowerCase().includes(normalizedQuery);
      const matchesCategory = category === 'All categories' || talent.role.toLowerCase().includes(category.toLowerCase());
      const matchesLocation = location === 'Anywhere' || talent.location.includes(location);
      return matchesQuery && matchesCategory && matchesLocation;
    });
  }, [category, location, query, talents]);

  return (
    <section className="search-section" id="talent">
      <div className="shell">
        <SectionHeading eyebrow="Find your people" title="Find the talent your community needs" description="Search a growing network of people ready to share their skills, perspective and time." />
        <div className="talent-search__panel">
          <label className="search-field search-field--wide">
            <span className="search-field__icon"><Icon name="search" size={19} /></span>
            <span className="search-field__content"><small>What are you looking for?</small><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search skills, talents or expertise" /></span>
          </label>
          <label className="search-field">
            <span className="search-field__icon">◈</span>
            <span className="search-field__content"><small>Category</small><select value={category} onChange={(event) => setCategory(event.target.value)}><option>All categories</option><option>Learning</option><option>Technology</option><option>Career</option><option>Mentorship</option></select></span>
            <Icon name="chevron" size={16} />
          </label>
          <label className="search-field">
            <span className="search-field__icon"><Icon name="map" size={19} /></span>
            <span className="search-field__content"><small>Location</small><select value={location} onChange={(event) => setLocation(event.target.value)}><option>Anywhere</option><option>Colombo</option><option>Accra</option><option>Bengaluru</option></select></span>
            <Icon name="chevron" size={16} />
          </label>
          <button className="button button--primary search-submit" type="button" onClick={() => setHasSearched(true)}>Find Talent <Icon name="arrow" size={17} /></button>
        </div>
        {hasSearched && <div className="search-results"><span>{results.length} demonstration {results.length === 1 ? 'profile' : 'profiles'} found</span>{results.length > 0 && <div className="search-results__chips">{results.map((result) => <span key={result.name}>{result.name}</span>)}</div>}</div>}
      </div>
    </section>
  );
}
