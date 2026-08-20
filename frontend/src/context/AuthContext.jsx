import { createContext, useContext, useState, useEffect, useCallback, useRef } from 'react';
import { authApi, userApi } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const stored = localStorage.getItem('user');
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  });
  const [loading, setLoading] = useState(true);
  const mountedRef = useRef(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      if (mountedRef.current) setLoading(false);
      return;
    }

    userApi.getMe()
      .then((res) => {
        if (!mountedRef.current) return;
        const userData = res.data;
        setUser(userData);
        localStorage.setItem('user', JSON.stringify(userData));
      })
      .catch(() => {
        if (!mountedRef.current) return;
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setUser(null);
      })
      .finally(() => {
        if (mountedRef.current) setLoading(false);
      });

    return () => { mountedRef.current = false; };
  }, []);

  useEffect(() => {
    const handleUnauthorized = () => {
      setUser(null);
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    };
    window.addEventListener('auth:unauthorized', handleUnauthorized);
    return () => window.removeEventListener('auth:unauthorized', handleUnauthorized);
  }, []);

  const login = useCallback(async (email, password) => {
    const res = await authApi.login(email, password);
    const { token } = res.data;
    localStorage.setItem('token', token);

    const meRes = await userApi.getMe();
    const userData = meRes.data;
    setUser(userData);
    localStorage.setItem('user', JSON.stringify(userData));
    return res.data;
  }, []);

  const register = useCallback(async (email, password, adminRequest = false) => {
    const res = await authApi.register(email, password, adminRequest);
    const { token } = res.data;
    localStorage.setItem('token', token);

    const meRes = await userApi.getMe();
    const userData = meRes.data;
    setUser(userData);
    localStorage.setItem('user', JSON.stringify(userData));
    return res.data;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  }, []);

  const refreshUser = useCallback(async () => {
    try {
      const res = await userApi.getMe();
      const userData = res.data;
      setUser(userData);
      localStorage.setItem('user', JSON.stringify(userData));
      return userData;
    } catch {
      return null;
    }
  }, []);

  const isAdmin = user?.role === 'ADMIN';

  const value = {
    user,
    loading,
    login,
    register,
    logout,
    refreshUser,
    isAuthenticated: !!user,
    isAdmin,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
