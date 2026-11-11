import { categories } from '../data/mockData';
import { CallToAction } from '../components/CallToAction';
import { CategorySection } from '../components/CategorySection';
import { Footer } from '../components/Footer';
import { Hero } from '../components/Hero';
import { HowItWorks } from '../components/HowItWorks';
import { ImpactSection } from '../components/ImpactSection';
import { Navbar } from '../components/Navbar';
import { Opportunities } from '../components/Opportunities';

type HomeProps = { onNotify: (message: string) => void };

export function Home({ onNotify }: HomeProps) {
  return (
    <>
      <Navbar />
      <main>
        <Hero />
        <section className="search-section" id="talent"><div className="shell discovery-home-link"><div><p className="eyebrow">Find your people</p><h2>Find the talent your community needs</h2><p>Explore real public profiles shared by the GrowLink community.</p></div><a className="button button--primary button--large" href="/talents">Browse talent <span aria-hidden="true">→</span></a></div></section>
        <CategorySection onNotify={onNotify} />
        <HowItWorks />
        <Opportunities onNotify={onNotify} />
        <ImpactSection />
        <CallToAction />
      </main>
      <Footer onNotify={onNotify} />
      <span className="sr-only">{categories.length} community categories available</span>
    </>
  );
}
