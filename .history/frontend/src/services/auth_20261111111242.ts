export type UserRole = 'USER' | 'MENTOR' | 'ORGANIZATION' | 'ADMIN';
export type UserStatus = 'ACTIVE' | 'PENDING_APPROVAL' | 'SUSPENDED';

export type AuthUser = {
  id: number;
  fullName: string;
  email: string;
  phone?: string;
  location: string;
  role: UserRole;
  requestedRole?: UserRole;
  status: UserStatus;
  createdAt: string;
};

type AuthResponse = {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: AuthUser;
};

type RegisterPayload = {
  fullName: string;
  email: string;
  password: string;
  phone?: string;
  location: string;
  role: UserRole;
};

type ApiError = {
  message?: string;
  fieldErrors?: Record<string, string>;
};

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`/api${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
  });

  const body = await response.json().catch(() => null);
  if (!response.ok) {
    const error = body as ApiError | null;
    const validationMessage = error?.fieldErrors ? Object.values(error.fieldErrors)[0] : undefined;
    throw new Error(validationMessage || error?.message || 'Something went wrong. Please try again.');
  }
  return body as T;
}

export function register(payload: RegisterPayload) {
  return request<AuthUser>('/auth/register', { method: 'POST', body: JSON.stringify(payload) });
}

export function login(email: string, password: string) {
  return request<AuthResponse>('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) });
}

export function getCurrentUser(token: string) {
  return request<AuthUser>('/auth/me', { headers: { Authorization: `Bearer ${token}` } });
}

export function logout(token: string) {
  return request<{ message: string }>('/auth/logout', { method: 'POST', headers: { Authorization: `Bearer ${token}` } });
}
