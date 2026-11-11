import { Icon } from './Icon';

type CallToActionProps = { onNotify: (message: string) => void };

export function CallToAction({ onNotify }: CallToActionProps) {
  return (
    <section className="cta-section" id="community">
      <div className="shell cta-section__inner"><div><p className="eyebrow">Your next connection starts here</p><h2>Your talent can make<br /><span>a difference.</span></h2><p>Join a growing network where knowledge is shared, opportunities are discovered and communities grow stronger.</p></div><div className="cta-section__actions"><a className="button button--primary button--large" href="/register">Join GrowLink AI <Icon name="arrow" size={18} /></a><a className="button button--outline button--large" href="#opportunities">Explore Opportunities</a></div></div>
    </section>
  );
}
