import { Icon } from './Icon';

const impactStats = [
  { value: '12.8k', label: 'People connected', accent: 'teal' },
  { value: '48k+', label: 'Skills shared', accent: 'yellow' },
  { value: '3.2k', label: 'Learning opportunities', accent: 'coral' },
  { value: '184', label: 'Community initiatives', accent: 'blue' },
];

export function ImpactSection() {
  return (
    <section className="impact-section">
      <div className="shell impact-section__inner">
        <div className="impact-copy"><p className="eyebrow eyebrow--light">A better kind of network</p><h2>Growing together,<br /><span>creating impact.</span></h2><p>When knowledge moves freely, communities become more resilient, more connected and more ready for what comes next.</p><a className="button button--light" href="#about">Our story <Icon name="arrow" size={16} /></a></div>
        <div className="impact-stats">{impactStats.map((stat) => <div className={`impact-stat impact-stat--${stat.accent}`} key={stat.label}><strong>{stat.value}</strong><span>{stat.label}</span><i>Illustrative</i></div>)}</div>
      </div>
    </section>
  );
}
