import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { LoginRequest, LoginResponse } from '../../types';
import { authApi } from '../../api/authApi';
import { useToast } from '../../components/Toast';

interface AuthContextValue {
  user: LoginResponse | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  isLoginModalOpen: boolean;
  openLoginModal: () => void;
  closeLoginModal: () => void;
  login: (credentials: LoginRequest) => Promise<boolean>;
  logout: () => Promise<void>;
  checkAuth: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<LoginResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isLoginModalOpen, setIsLoginModalOpen] = useState<boolean>(false);
  const { showSuccess, showError, showInfo } = useToast();

  const checkAuth = useCallback(async () => {
    try {
      const data = await authApi.getMe();
      if (data && data.memberId !== null) {
        setUser(data);
      } else {
        setUser(null);
      }
    } catch {
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  const login = async (credentials: LoginRequest): Promise<boolean> => {
    try {
      const res = await authApi.login(credentials);
      setUser(res);
      setIsLoginModalOpen(false);
      showSuccess(`${res.displayName || res.username}님으로 로그인되었습니다.`, '로그인 성공');
      return true;
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : '로그인에 실패했습니다.';
      showError(errorMsg, '로그인 실패');
      throw err;
    }
  };

  const logout = async () => {
    try {
      await authApi.logout();
      setUser(null);
      showInfo('로그아웃되었습니다. 이후 작업은 게스트(SYSTEM)로 기록됩니다.', '로그아웃');
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : '로그아웃 중 오류가 발생했습니다.';
      showError(errorMsg);
      setUser(null);
    }
  };

  const openLoginModal = () => setIsLoginModalOpen(true);
  const closeLoginModal = () => setIsLoginModalOpen(false);

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user?.memberId,
        isLoading,
        isLoginModalOpen,
        openLoginModal,
        closeLoginModal,
        login,
        logout,
        checkAuth,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
