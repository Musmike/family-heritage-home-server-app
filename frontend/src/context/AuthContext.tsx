import { createContext, useState, type ReactNode, useContext, useEffect } from 'react';
import api from '../api/axiosInstance';

interface AuthUser {
  username: string;
  roles: string[];
}

interface AuthContextType {
  user: AuthUser | null;
  isLoading: boolean;
  login: () => Promise<void>;
  logout: () => void;
}
export const AuthContext = createContext<AuthContextType | null>(null);
export const useAuth = () => useContext(AuthContext)!;

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const fetchUser = async () => {
    try {
      const response = await api.get<AuthUser>('/users/me');
      setUser(response.data);
    } catch (error) {
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchUser();

    const handleAuthError = () => {
      setUser(null);
    };
    window.addEventListener('auth-error', handleAuthError);
    return () => {
      window.removeEventListener('auth-error', handleAuthError);
    };
  }, []);


  const login = async () => {
    setIsLoading(true);
    await fetchUser();
  };

  const logout = async () => {
    try {
      await api.post('/auth/logout');
    } catch (error) {
        console.error("Logout failed", error);
    } finally {
      setUser(null);
    }
  };

  const value = { user, isLoading, login, logout };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}