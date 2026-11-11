import type { Talent } from '../data/mockData';
import { Icon } from './Icon';
import { SectionHeading } from './SectionHeading';

type FeaturedTalentsProps = {
  talents: Talent[];
  onNotify: (message: string) => void;
};

export function FeaturedTalents({ talents, onNotify }: FeaturedTalentsProps) {
  return (
    <section className="section section--cream">
      <div className="shell">
        <div className="section-heading-row"><SectionHeading eyebrow="Community spotlight" title="Meet the talents in your community" description="A glimpse at the people sharing what they know. Demonstration profiles, not verified platform users." /><a className="text-link" href="#talent">Explore all talent <Icon name="arrow" size={16} /></a></div>
        <div className="talent-grid">
          {talents.map((talent) => <article className="talent-card" key={talent.name}><div className={`talent-card__avatar talent-card__avatar--${talent.tone}`}>{talent.initials}</div><div className="talent-card__content"><div className="talent-card__title"><div><h3>{talent.name}</h3><p>{talent.role}</p></div><button type="button" aria-label={`Save ${talent.name}`} onClick={() => onNotify('Saved for your next visit.')}>♡</button></div><div className="talent-card__skills">{talent.skills.map((skill) => <span key={skill}>{skill}</span>)}</div><p className="talent-card__location"><Icon name="map" size={14} /> {talent.location}</p><p className="talent-card__description">{talent.description}</p><button className="button button--outline button--full" type="button" onClick={() => onNotify('Profile previews will be available soon.')}>View Profile <Icon name="arrow" size={16} /></button></div></article>)}
        </div>
      </div>
    </section>
  );
}
