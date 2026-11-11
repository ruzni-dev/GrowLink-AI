import type { ReactNode } from 'react';
import { useAuth } from '../context/AuthContext';

type ProtectedRouteProps = { children: ReactNode; onNavigate: (path: string) => void };

export function ProtectedRoute({ children, onNavigate }: ProtectedRouteProps) {
  const { user, isLoading } = useAuth();
  if (isLoading) return <main className="auth-loading"><span className="loading-spinner" />Loading your community space...</main>;
  if (!user) {
    onNavigate('/login');
    return <main className="auth-loading"><span className="loading-spinner" />Redirecting to sign in...</main>;
  }
  return <>{children}</>;
}
