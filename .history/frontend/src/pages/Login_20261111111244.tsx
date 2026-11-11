import { useState, type FormEvent } from 'react';
import { useAuth } from '../context/AuthContext';
import { Icon } from '../components/Icon';

type LoginProps = { onNavigate: (path: string) => void; onNotify: (message: string) => void };

export function Login({ onNavigate, onNotify }: LoginProps) {
  const { signIn } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError('');
    if (!email || !password) {
      setError('Enter your email and password to continue.');
      return;
    }
    setIsSubmitting(true);
    try {
      await signIn(email, password);
      onNavigate('/dashboard');
    } catch (submissionError) {
      setError(submissionError instanceof Error ? submissionError.message : 'Unable to sign in.');
    } finally {
      setIsSubmitting(false);
    }
  }

  return <AuthLayout eyebrow="Welcome back" title="Your next connection starts here." description="Sign in to pick up where you left off and keep growing with your community." alternate="New to GrowLink AI?" action="Create an account" onAction={() => onNavigate('/register')} onNotify={onNotify}>
    <form className="auth-form" onSubmit={handleSubmit} noValidate>
      <label className="auth-field"><span>Email address</span><input type="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="you@example.com" autoComplete="email" required /></label>
      <label className="auth-field"><span>Password</span><div className="auth-password"><input type={showPassword ? 'text' : 'password'} value={password} onChange={(event) => setPassword(event.target.value)} placeholder="Enter your password" autoComplete="current-password" required /><button type="button" onClick={() => setShowPassword(!showPassword)} aria-label={showPassword ? 'Hide password' : 'Show password'}>{showPassword ? 'Hide' : 'Show'}</button></div></label>
      {error && <p className="auth-error" role="alert">{error}</p>}
      <button className="button button--primary button--large auth-submit" type="submit" disabled={isSubmitting}>{isSubmitting ? 'Signing in...' : 'Sign in'} {!isSubmitting && <Icon name="arrow" size={17} />}</button>
      <p className="auth-security">Access tokens stay in memory on this device and are never saved to browser storage.</p>
    </form>
  </AuthLayout>;
}

type AuthLayoutProps = { eyebrow: string; title: string; description: string; alternate: string; action: string; onAction: () => void; onNotify: (message: string) => void; children: React.ReactNode };

export function AuthLayout({ eyebrow, title, description, alternate, action, onAction, onNotify, children }: AuthLayoutProps) {
  return <main className="auth-page"><div className="auth-page__visual"><a className="brand brand--auth" href="/" onClick={() => onNotify('Returning to the GrowLink AI home page.') }><span className="brand__mark"><span>G</span></span><span className="brand__name">GrowLink <em>AI</em></span></a><div className="auth-page__quote"><span className="auth-quote-mark">“</span><p>Every community has untapped potential. We make the right connection easier to find.</p><span className="auth-page__quote-label">GrowLink AI / Community network</span></div><div className="auth-page__orbit"><span>Learn</span><span>Connect</span><span>Grow</span><b>G</b></div></div><div className="auth-page__form"><div className="auth-page__form-inner"><p className="eyebrow">{eyebrow}</p><h1>{title}</h1><p className="auth-page__description">{description}</p>{children}<p className="auth-page__alternate">{alternate} <button type="button" onClick={onAction}>{action}</button></p><button className="auth-back" type="button" onClick={() => onNotify('Landing page navigation is available from the home link.')}>← Back to home</button></div></div></main>;
}
