import { categories } from '../data/mockData';
import { Icon } from './Icon';
import { SectionHeading } from './SectionHeading';

type CategorySectionProps = {
  onNotify: (message: string) => void;
};

export function CategorySection({ onNotify }: CategorySectionProps) {
  return (
    <section className="section section--soft" id="categories">
      <div className="shell">
        <SectionHeading eyebrow="Explore by focus" title="Explore opportunities across every field" description="From a first lesson to a fresh venture, find the people and pathways that move you forward." />
        <div className="category-grid">
          {categories.map((category) => <button className="category-card" key={category.name} type="button" onClick={() => onNotify(`${category.name} exploration will be available soon.`)}><span className={`category-card__icon category-card__icon--${category.tone}`}>{category.icon}</span><span className="category-card__body"><b>{category.name}</b><small>{category.description}</small></span><span className="category-card__arrow"><Icon name="arrow" size={17} /></span></button>)}
        </div>
      </div>
    </section>
  );
}
