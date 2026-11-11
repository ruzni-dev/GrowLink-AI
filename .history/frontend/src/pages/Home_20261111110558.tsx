import { categories, talents } from '../data/mockData';
import { CallToAction } from '../components/CallToAction';
import { CategorySection } from '../components/CategorySection';
import { FeaturedTalents } from '../components/FeaturedTalents';
import { Footer } from '../components/Footer';
import { Hero } from '../components/Hero';
import { HowItWorks } from '../components/HowItWorks';
import { ImpactSection } from '../components/ImpactSection';
import { Navbar } from '../components/Navbar';
import { Opportunities } from '../components/Opportunities';
import { TalentSearch } from '../components/TalentSearch';

type HomeProps = { onNotify: (message: string) => void };

export function Home({ onNotify }: HomeProps) {
  return (
    <>
      <Navbar onNotify={onNotify} />
      <main>
        <Hero onNotify={onNotify} />
        <TalentSearch talents={talents} />
        <CategorySection onNotify={onNotify} />
        <FeaturedTalents talents={talents} onNotify={onNotify} />
        <HowItWorks />
        <Opportunities onNotify={onNotify} />
        <ImpactSection />
        <CallToAction onNotify={onNotify} />
      </main>
      <Footer onNotify={onNotify} />
      <span className="sr-only">{categories.length} community categories available</span>
    </>
  );
}
