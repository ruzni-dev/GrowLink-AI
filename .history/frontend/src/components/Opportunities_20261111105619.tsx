import { opportunities } from '../data/mockData';
import { Icon } from './Icon';
import { SectionHeading } from './SectionHeading';

type OpportunitiesProps = { onNotify: (message: string) => void };

export function Opportunities({ onNotify }: OpportunitiesProps) {
  return (
    <section className="section opportunity-section" id="opportunities">
      <div className="shell">
        <div className="section-heading-row"><SectionHeading eyebrow="Open a door" title="Opportunities that move you forward" description="Small steps become big shifts when the right opportunity meets the right person." /><a className="text-link" href="#community">View all opportunities <Icon name="arrow" size={16} /></a></div>
        <div className="opportunity-grid">
          {opportunities.map((opportunity) => <button className="opportunity-card" type="button" key={opportunity.name} onClick={() => onNotify(`${opportunity.name} listings will be available soon.`)}><span className="opportunity-card__icon">{opportunity.icon}</span><span className="opportunity-card__body"><b>{opportunity.name}</b><small>{opportunity.description}</small></span><span className="opportunity-card__link">Explore <Icon name="arrow" size={15} /></span></button>)}
        </div>
      </div>
    </section>
  );
}
