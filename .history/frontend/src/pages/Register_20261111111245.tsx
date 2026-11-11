import { useState, type FormEvent } from 'react';
import type { UserRole } from '../services/auth';
import { useAuth } from '../context/AuthContext';
import { Icon } from '../components/Icon';
import { AuthLayout } from './Login';

type RegisterProps = { onNavigate: (path: string) => void; onNotify: (message: string) => void };

export function Register({ onNavigate, onNotify }: RegisterProps) {
  const { signUp } = useAuth();
  const [form, setForm] = useState({ fullName: '', email: '', password: '', confirmPassword: '', phone: '', location: '', role: 'USER' as UserRole });
  const [showPassword, setShowPassword] = useState(false);
  const [terms, setTerms] = useState(false);
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const update = (key: keyof typeof form, value: string) => setForm((current) => ({ ...current, [key]: value }));

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError('');
    if (form.password !== form.confirmPassword) return setError('Passwords do not match.');
    if (form.password.length < 8 || !/[A-Z]/.test(form.password) || !/[a-z]/.test(form.password) || !/\d/.test(form.password)) return setError('Use at least 8 characters with uppercase, lowercase and a number.');
    if (!terms) return setError('Accept the community terms to create an account.');
    setIsSubmitting(true);
    try {
      const created = await signUp({ fullName: form.fullName, email: form.email, password: form.password, phone: form.phone || undefined, location: form.location, role: form.role });
      onNotify(created.status === 'PENDING_APPROVAL' ? 'Your account request was received for review.' : 'Account created. Sign in to continue.');
      onNavigate('/login');
    } catch (submissionError) {
      setError(submissionError instanceof Error ? submissionError.message : 'Unable to create your account.');
    } finally {
      setIsSubmitting(false);
    }
  }

  return <AuthLayout eyebrow="Join the network" title="Bring your strengths to the table." description="Create a profile and find the people, knowledge and opportunities that help communities move forward." alternate="Already have an account?" action="Sign in" onAction={() => onNavigate('/login')} onNotify={onNotify}>
    <form className="auth-form auth-form--register" onSubmit={handleSubmit} noValidate>
      <div className="auth-form__row"><label className="auth-field"><span>Full name</span><input value={form.fullName} onChange={(event) => update('fullName', event.target.value)} placeholder="Your full name" autoComplete="name" required /></label><label className="auth-field"><span>Location</span><input value={form.location} onChange={(event) => update('location', event.target.value)} placeholder="City or region" autoComplete="address-level2" required /></label></div>
      <label className="auth-field"><span>Email address</span><input type="email" value={form.email} onChange={(event) => update('email', event.target.value)} placeholder="you@example.com" autoComplete="email" required /></label>
      <div className="auth-form__row"><label className="auth-field"><span>Password</span><div className="auth-password"><input type={showPassword ? 'text' : 'password'} value={form.password} onChange={(event) => update('password', event.target.value)} placeholder="8+ characters" autoComplete="new-password" required /><button type="button" onClick={() => setShowPassword(!showPassword)} aria-label={showPassword ? 'Hide passwords' : 'Show passwords'}>{showPassword ? 'Hide' : 'Show'}</button></div></label><label className="auth-field"><span>Confirm password</span><input type={showPassword ? 'text' : 'password'} value={form.confirmPassword} onChange={(event) => update('confirmPassword', event.target.value)} placeholder="Repeat password" autoComplete="new-password" required /></label></div>
      <div className="auth-form__row"><label className="auth-field"><span>Phone <small>Optional</small></span><input type="tel" value={form.phone} onChange={(event) => update('phone', event.target.value)} placeholder="+94 77 000 0000" autoComplete="tel" /></label><label className="auth-field"><span>How will you contribute?</span><select value={form.role} onChange={(event) => update('role', event.target.value as UserRole)}><option value="USER">Community member</option><option value="MENTOR">Mentor (review required)</option><option value="ORGANIZATION">Organization (review required)</option></select></label></div>
      <label className="auth-checkbox"><input type="checkbox" checked={terms} onChange={(event) => setTerms(event.target.checked)} /><span>I agree to the GrowLink AI community terms and understand this is a demonstration account flow.</span></label>
      {error && <p className="auth-error" role="alert">{error}</p>}
      <button className="button button--primary button--large auth-submit" type="submit" disabled={isSubmitting}>{isSubmitting ? 'Creating account...' : 'Create account'} {!isSubmitting && <Icon name="arrow" size={17} />}</button>
    </form>
  </AuthLayout>;
}
