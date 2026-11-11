import { Icon } from './Icon';

type HeroProps = {
  onNotify: (message: string) => void;
};

export function Hero({ onNotify }: HeroProps) {
  return (
    <section className="hero-section" id="home">
      <div className="shell hero-section__grid">
        <div className="hero-copy reveal-up">
          <div className="hero-badge"><span className="pulse-dot" /> AI-Powered Community Empowerment</div>
          <h1>Discover talent.<br /><span>Connect knowledge.</span><br />Grow together.</h1>
          <p className="hero-copy__description">Unlock the potential within your community. Discover talented people, share knowledge, find opportunities and build meaningful connections.</p>
          <div className="hero-copy__actions">
            <a className="button button--primary button--large" href="#talent">Explore Talent <Icon name="arrow" size={18} /></a>
            <button className="button button--outline button--large" type="button" onClick={() => onNotify('Community registration will open soon.')}>
              Join Our Community
            </button>
          </div>
          <div className="hero-trust"><span className="hero-trust__avatars"><i>AR</i><i>DM</i><i>PN</i><b>+</b></span><span>Connecting people. Empowering communities.<br />Creating opportunities.</span></div>
        </div>

        <div className="hero-visual reveal-up" aria-label="A visual network of people, skills and opportunities">
          <div className="hero-visual__halo hero-visual__halo--one" />
          <div className="hero-visual__halo hero-visual__halo--two" />
          <div className="hero-network">
            <div className="hero-network__line hero-network__line--one" />
            <div className="hero-network__line hero-network__line--two" />
            <div className="hero-network__line hero-network__line--three" />
            <div className="hero-network__node hero-network__node--center"><span>G</span><small>GrowLink</small></div>
            <div className="hero-network__node hero-network__node--top"><span>✦</span><small>Learn</small></div>
            <div className="hero-network__node hero-network__node--left"><span>↗</span><small>Skills</small></div>
            <div className="hero-network__node hero-network__node--right"><span>◉</span><small>People</small></div>
            <div className="hero-network__node hero-network__node--bottom"><span>◇</span><small>Opportunity</small></div>
          </div>
          <div className="floating-card floating-card--top"><span className="floating-card__icon floating-card__icon--green">✦</span><span><b>Knowledge shared</b><small>3,240 connections today</small></span></div>
          <div className="floating-card floating-card--bottom"><span className="floating-card__icon floating-card__icon--orange">↗</span><span><b>New opportunity</b><small>Community mentor needed</small></span><Icon name="arrow" size={15} /></div>
        </div>
      </div>
      <div className="hero-section__footnote"><span>Trusted by people building stronger communities</span><span className="footnote-line" /><span className="footnote-label">01 / 04</span></div>
    </section>
  );
}
