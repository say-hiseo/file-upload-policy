import React, { useState } from 'react';
import { Lock, Eye, EyeOff, AlertCircle } from 'lucide-react';
import { Modal } from '../../components/Modal';
import { Button } from '../../components/Button';
import { useAuth } from './useAuth';

export const LoginModal: React.FC = () => {
  const { isLoginModalOpen, closeLoginModal, login } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username.trim() || !password) {
      setErrorMessage('아이디와 비밀번호를 모두 입력해주세요.');
      return;
    }

    setErrorMessage(null);
    setIsSubmitting(true);

    try {
      await login({ username: username.trim(), password });
      setUsername('');
      setPassword('');
    } catch (err: unknown) {
      if (err instanceof Error) {
        setErrorMessage(err.message);
      } else {
        setErrorMessage('아이디 또는 비밀번호가 올바르지 않습니다');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const fillDummy = (u: string, p: string) => {
    setUsername(u);
    setPassword(p);
    setErrorMessage(null);
  };

  const handleClose = () => {
    setErrorMessage(null);
    closeLoginModal();
  };

  return (
    <Modal
      id="login-modal"
      isOpen={isLoginModalOpen}
      onClose={handleClose}
      maxWidth="sm"
    >
      <div className="flex flex-col items-center mb-6">
        <div
          id="login-icon-container"
          className="w-14 h-14 rounded-2xl bg-[#6366F1]/15 border border-[#6366F1]/30 flex items-center justify-center mb-3 shadow-[0_0_20px_rgba(99,102,241,0.2)]"
        >
          <Lock className="w-7 h-7 text-[#6366F1]" aria-hidden="true" />
        </div>
        <h2 id="login-heading" className="text-xl font-bold text-[#E2E8F0]">
          로그인
        </h2>
        <p className="text-xs text-[#94A3B8] mt-1">
          정책 변경 이력을 특정 관리자 계정으로 기록합니다
        </p>
      </div>

      <form id="login-form" onSubmit={handleSubmit} className="space-y-4">
        {errorMessage && (
          <div
            id="login-error-message"
            role="alert"
            className="flex items-center gap-2 p-3 bg-[#F43F5E]/15 border border-[#F43F5E]/40 rounded-lg text-[#FFB4AB] text-xs"
          >
            <AlertCircle className="w-4 h-4 flex-shrink-0 text-[#F43F5E]" />
            <span>{errorMessage}</span>
          </div>
        )}

        <div>
          <label
            htmlFor="login-username-input"
            className="block text-xs font-semibold uppercase tracking-wider text-[#94A3B8] mb-1.5"
          >
            아이디
          </label>
          <input
            id="login-username-input"
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="아이디를 입력하세요"
            autoComplete="username"
            disabled={isSubmitting}
            className="w-full bg-[#0A0C10] border border-[#2D333B] rounded-lg px-3.5 py-2.5 text-sm text-[#E2E8F0] placeholder-[#94A3B8]/40 focus:outline-none focus:border-[#6366F1] focus:ring-1 focus:ring-[#6366F1] transition-colors disabled:opacity-50"
          />
        </div>

        <div>
          <label
            htmlFor="login-password-input"
            className="block text-xs font-semibold uppercase tracking-wider text-[#94A3B8] mb-1.5"
          >
            비밀번호
          </label>
          <div className="relative">
            <input
              id="login-password-input"
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="비밀번호를 입력하세요"
              autoComplete="current-password"
              disabled={isSubmitting}
              className="w-full bg-[#0A0C10] border border-[#2D333B] rounded-lg px-3.5 py-2.5 pr-10 text-sm text-[#E2E8F0] placeholder-[#94A3B8]/40 focus:outline-none focus:border-[#6366F1] focus:ring-1 focus:ring-[#6366F1] transition-colors disabled:opacity-50"
            />
            <button
              id="toggle-password-visibility-btn"
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute inset-y-0 right-0 pr-3 flex items-center text-[#94A3B8] hover:text-[#E2E8F0] transition-colors"
              aria-label={showPassword ? '비밀번호 숨기기' : '비밀번호 보기'}
            >
              {showPassword ? (
                <EyeOff className="w-4 h-4" />
              ) : (
                <Eye className="w-4 h-4" />
              )}
            </button>
          </div>
        </div>

        <Button
          id="login-submit-btn"
          type="submit"
          variant="primary"
          size="md"
          isLoading={isSubmitting}
          className="w-full mt-2"
        >
          로그인
        </Button>
      </form>

      <div className="mt-6 pt-5 border-t border-[#2D333B]">
        <p className="text-xs font-semibold text-[#94A3B8] uppercase tracking-wider mb-2">
          더미 계정 안내 (클릭 시 자동 입력)
        </p>
        <div className="space-y-1.5">
          <button
            type="button"
            id="dummy-account-test1-btn"
            onClick={() => fillDummy('test1', '1234')}
            className="w-full flex items-center justify-between px-3 py-1.5 rounded-md bg-[#1B202A] hover:bg-[#252C3A] border border-[#2D333B] text-xs text-left text-slate-300 hover:text-white transition-colors group"
          >
            <span className="font-mono text-indigo-400 group-hover:text-indigo-300">
              test1 / 1234
            </span>
            <span className="text-[11px] text-slate-400">관리자 1</span>
          </button>
          <button
            type="button"
            id="dummy-account-test2-btn"
            onClick={() => fillDummy('test2', '5678')}
            className="w-full flex items-center justify-between px-3 py-1.5 rounded-md bg-[#1B202A] hover:bg-[#252C3A] border border-[#2D333B] text-xs text-left text-slate-300 hover:text-white transition-colors group"
          >
            <span className="font-mono text-indigo-400 group-hover:text-indigo-300">
              test2 / 5678
            </span>
            <span className="text-[11px] text-slate-400">관리자 2</span>
          </button>
        </div>
      </div>
    </Modal>
  );
};
