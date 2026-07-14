'use client';

import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { apiFetch } from '@/lib/api-client';

interface User {
  id: number;
  employeeCode: string;
  name: string;
  email: string;
  role: 'EMPLOYEE' | 'ADMIN';
}

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (employeeCode: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    apiFetch<User>('/api/auth/me', { skipAuth: true })
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  const login = useCallback(async (employeeCode: string, password: string) => {
    await apiFetch('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ employeeCode, password }),
      skipAuth: true,
    });
    const me = await apiFetch<User>('/api/auth/me', { skipAuth: true });
    setUser(me);
  }, []);

  const logout = useCallback(async () => {
    await apiFetch('/api/auth/logout', { method: 'POST' });
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
