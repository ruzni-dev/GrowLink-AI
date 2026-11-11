import { createContext } from 'react';
import type { AuthUser } from '../services/auth';

export type AuthContextValue = {
  user: AuthUser | null;
  token: string | null;
  isLoading: boolean;
  signIn: (email: string, password: string) => Promise<void>;
  signUp: (payload: {
    fullName: string;
    email: string;
    password: string;
    phone?: string;
    location: string;
    role: 'USER' | 'MENTOR' | 'ORGANIZATION' | 'ADMIN';
  }) => Promise<AuthUser>;
  signOut: () => Promise<void>;
};

export const AuthContext = createContext<AuthContextValue | null>(null);
