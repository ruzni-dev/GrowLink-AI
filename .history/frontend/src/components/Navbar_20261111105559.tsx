import { useState } from 'react';
import { Icon } from './Icon';

type NavbarProps = {
  onNotify: (message: string) => void;
};

const links = [
  { label: 'Home', href: '#home' },
  { label: 'Explore Talent', href: '#talent' },
  { label: 'Categories', href: '#categories' },
  { label: 'Opportunities', href: '#opportunities' },
  { label: 'About Us', href: '#about' },
];

export function Navbar({ onNotify }: NavbarProps) {
  const [isOpen, setIsOpen] = useState(false);

  const closeMenu = () => setIsOpen(false);

  return (
    <header className="site-nav">
      <div className="shell site-nav__inner">
        <a className="brand" href="#home" onClick={closeMenu} aria-label="GrowLink AI home">
          <span className="brand__mark"><span>G</span></span>
          <span className="brand__name">GrowLink <em>AI</em></span>
        </a>

        <nav className={`site-nav__links ${isOpen ? 'site-nav__links--open' : ''}`} aria-label="Primary navigation">
          {links.map((link) => (
            <a key={link.href} href={link.href} onClick={closeMenu}>
              {link.label}
            </a>
          ))}
          <div className="site-nav__mobile-actions">
            <button type="button" className="button button--ghost" onClick={() => { onNotify('Sign in will be available when accounts launch.'); closeMenu(); }}>Log In</button>
            <a className="button button--dark" href="#community" onClick={closeMenu}>Get Started <Icon name="arrow" size={16} /></a>
          </div>
        </nav>

        <div className="site-nav__actions">
          <button type="button" className="button button--ghost" onClick={() => onNotify('Sign in will be available when accounts launch.')}>Log In</button>
          <a className="button button--dark" href="#community">Get Started <Icon name="arrow" size={16} /></a>
        </div>

        <button className="site-nav__menu" type="button" aria-label={isOpen ? 'Close menu' : 'Open menu'} aria-expanded={isOpen} onClick={() => setIsOpen(!isOpen)}>
          <Icon name={isOpen ? 'close' : 'menu'} size={22} />
        </button>
      </div>
    </header>
  );
}
