import { useMemo, useState, type ReactNode } from 'react';
import * as authApi from '../services/auth';
import { AuthContext, type AuthContextValue } from './authContext';

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
      if (token) await authApi.logout(token).catch(() => undefined);
      setToken(null);
      setUser(null);
    },
  }), [token, user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
