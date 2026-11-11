import { Icon } from './Icon';

export function Opportunities() {
  return (
    <section className="section opportunity-section" id="opportunities">
      <div className="shell opportunity-home-callout"><div><p className="eyebrow">Open a door</p><h2>Opportunities that move you forward</h2><p>Explore current jobs, scholarships, training, volunteering and events shared by the community.</p></div><a className="button button--primary button--large" href="/opportunities">Explore opportunities <Icon name="arrow" size={17} /></a></div>
    </section>
  );
}
