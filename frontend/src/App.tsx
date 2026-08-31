import React from 'react';
import {
  Shield,
  User,
  LogIn,
  LogOut,
  Info,
} from 'lucide-react';
import { ToastProvider } from './components/Toast';
import { AuthProvider, useAuth } from './features/auth/useAuth';
import { LoginModal } from './features/auth/LoginModal';
import { PolicyPage } from './features/policy/PolicyPage';
import { UploadPage } from './features/upload/UploadPage';
import { usePolicyOverview } from './features/policy/usePolicyOverview';
import { useFileUpload } from './features/upload/useFileUpload';
import { useUploadHistory } from './features/upload/useUploadHistory';
import { Button } from './components/Button';

const MainLayout: React.FC = () => {
  const { user, isAuthenticated, logout, openLoginModal } = useAuth();

  // Persisted state across views
  const policyState = usePolicyOverview(isAuthenticated);
  const uploadHistoryState = useUploadHistory(isAuthenticated);
  const uploadState = useFileUpload(uploadHistoryState.refresh);

  return (
    <div className="min-h-screen flex flex-col bg-[#0A0C10] text-[#E2E8F0]">
      {/* Top App Header */}
      <header
        id="top-app-header"
        className="w-full bg-[#0A0C10] border-b border-[#2D333B] sticky top-0 z-40 px-4 sm:px-8 py-3.5 flex items-center justify-between shadow-sm"
      >
        <div className="flex items-center gap-3">
          <div
            id="app-logo-badge"
            className="w-9 h-9 rounded-lg bg-[#6366F1]/15 border border-[#6366F1]/30 flex items-center justify-center text-[#6366F1] shadow-[0_0_14px_rgba(99,102,241,0.25)]"
          >
            <Shield className="w-5 h-5 fill-current" />
          </div>
          <div>
            <h1
              id="app-main-title"
              className="text-base sm:text-lg font-bold text-[#E2E8F0] tracking-tight leading-none"
            >
              파일 업로드 정책 관리
            </h1>
            <span className="text-[11px] text-[#94A3B8] hidden sm:inline-block">
              보안 정책 및 실시간 업로드 검증 콘솔
            </span>
          </div>
        </div>

        {/* User / Auth Status Bar */}
        <div className="flex items-center gap-2 sm:gap-3">
          {isAuthenticated && user ? (
            <>
              <div
                id="authenticated-user-badge"
                className="flex items-center gap-1.5 px-3 py-1.5 bg-[#1B202A] border border-[#2D333B] rounded-lg text-xs text-[#E2E8F0]"
              >
                <User className="w-3.5 h-3.5 text-[#6366F1]" />
                <span className="font-medium">
                  {user.displayName || user.username}님으로 로그인됨
                </span>
              </div>
              <Button
                id="logout-btn"
                variant="outline"
                size="sm"
                onClick={logout}
                leftIcon={<LogOut className="w-3.5 h-3.5" />}
              >
                로그아웃
              </Button>
            </>
          ) : (
            <>
              <div
                id="guest-status-badge"
                className="relative group flex items-center gap-1.5 px-2.5 sm:px-3 py-1.5 bg-[#151921] border border-[#2D333B] rounded-lg text-xs text-[#94A3B8] cursor-help"
              >
                <Info className="w-3.5 h-3.5 text-slate-400" />
                <span className="hidden sm:inline">게스트로 이용 중</span>
                <span className="sm:hidden font-mono">게스트</span>
                {/* Tooltip */}
                <div className="absolute right-0 top-full mt-2 hidden group-hover:block w-64 p-2.5 bg-[#0A0C10] border border-[#2D333B] rounded-lg shadow-xl text-[11px] text-[#94A3B8] z-50 pointer-events-none">
                  로그인 없이도 정책 조회와 테스트가 가능하며, 변경 이력에는 <strong className="text-slate-200">SYSTEM</strong>으로 기록됩니다.
                </div>
              </div>
              <Button
                id="login-modal-open-btn"
                variant="primary"
                size="sm"
                onClick={openLoginModal}
                leftIcon={<LogIn className="w-3.5 h-3.5" />}
              >
                로그인
              </Button>
            </>
          )}
        </div>
      </header>

      {/* Main 2-Split Content Area */}
      <main
        id="main-content-canvas"
        className="flex-1 p-4 sm:p-6 lg:p-8 w-full max-w-[1600px] mx-auto"
      >
        <div
          id="split-view-container"
          className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start"
        >
          {/* Left Panel: Policy Management (6 cols on desktop) */}
          <div
            id="split-left-panel"
            className="lg:col-span-6 xl:col-span-6 flex flex-col gap-6"
          >
            <div className="flex items-center justify-between border-b border-[#2D333B] pb-2.5">
              <div className="flex items-center gap-2">
                <div className="w-2 h-2 rounded-full bg-[#6366F1]" />
                <h2 className="text-sm font-bold uppercase tracking-wider text-[#E2E8F0]">
                  보안 정책 설정 영역
                </h2>
              </div>
              <span className="text-xs text-[#94A3B8]">
                차단 확장자 설정 및 이력 관리
              </span>
            </div>
            <PolicyPage policyState={policyState} />
          </div>

          {/* Right Panel: File Upload & Verification (6 cols on desktop) */}
          <div
            id="split-right-panel"
            className="lg:col-span-6 xl:col-span-6 flex flex-col gap-6"
          >
            <div className="flex items-center justify-between border-b border-[#2D333B] pb-2.5">
              <div className="flex items-center gap-2">
                <div className="w-2 h-2 rounded-full bg-[#10B981]" />
                <h2 className="text-sm font-bold uppercase tracking-wider text-[#E2E8F0]">
                  파일 업로드 및 실시간 정책 검증
                </h2>
              </div>
              <span className="text-xs text-[#94A3B8]">
                드래그 & 드롭 및 파일별 차단 여부 판별
              </span>
            </div>
            <UploadPage uploadState={uploadState} uploadHistoryState={uploadHistoryState} />
          </div>
        </div>
      </main>

      {/* Login Modal */}
      <LoginModal />
    </div>
  );
};

export default function App() {
  return (
    <ToastProvider>
      <AuthProvider>
        <MainLayout />
      </AuthProvider>
    </ToastProvider>
  );
}
