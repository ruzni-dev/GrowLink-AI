import { createContext, useContext, useMemo, useState, type ReactNode } from 'react';
import * as authApi from '../services/auth';

type AuthContextValue = {
  user: authApi.AuthUser | null;
  token: string | null;
  isLoading: boolean;
  signIn: (email: string, password: string) => Promise<void>;
  signUp: (payload: Parameters<typeof authApi.register>[0]) => Promise<authApi.AuthUser>;
  signOut: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<authApi.AuthUser | null>(null);
  const [token, setToken] = useState<string | null>(null);

  const value = useMemo<AuthContextValue>(() => ({
    user,
    token,
    isLoading: false,
    async signIn(email, password) {
      const response = await authApi.login(email, password);
      setToken(response.accessToken);
      setUser(response.user);
    },
    signUp: authApi.register,
    async signOut() {
      if (token) {
        await authApi.logout(token).catch(() => undefined);
      }
      setToken(null);
      setUser(null);
    },
  }), [token, user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}
