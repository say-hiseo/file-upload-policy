import React, { createContext, useContext, useState, useCallback, useId } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { CheckCircle2, AlertCircle, AlertTriangle, Info, X } from 'lucide-react';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface ToastMessage {
  id: string;
  type: ToastType;
  title?: string;
  message: string;
  duration?: number;
}

interface ToastContextValue {
  showToast: (type: ToastType, message: string, title?: string, duration?: number) => void;
  showSuccess: (message: string, title?: string) => void;
  showError: (message: string, title?: string) => void;
  showWarning: (message: string, title?: string) => void;
  showInfo: (message: string, title?: string) => void;
  removeToast: (id: string) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);

export function useToast() {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
}

export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [toasts, setToasts] = useState<ToastMessage[]>([]);

  const removeToast = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const showToast = useCallback(
    (type: ToastType, message: string, title?: string, duration = 4000) => {
      const id = `${Date.now()}-${Math.random().toString(36).substring(2, 9)}`;
      const newToast: ToastMessage = { id, type, message, title, duration };
      
      setToasts((prev) => [...prev, newToast]);

      if (duration > 0) {
        setTimeout(() => {
          removeToast(id);
        }, duration);
      }
    },
    [removeToast]
  );

  const showSuccess = useCallback((message: string, title?: string) => {
    showToast('success', message, title);
  }, [showToast]);

  const showError = useCallback((message: string, title?: string) => {
    showToast('error', message, title, 5000);
  }, [showToast]);

  const showWarning = useCallback((message: string, title?: string) => {
    showToast('warning', message, title);
  }, [showToast]);

  const showInfo = useCallback((message: string, title?: string) => {
    showToast('info', message, title);
  }, [showToast]);

  return (
    <ToastContext.Provider
      value={{ showToast, showSuccess, showError, showWarning, showInfo, removeToast }}
    >
      {children}
      {/* Toast Render Area */}
      <aside
        id="toast-container"
        aria-live="polite"
        aria-label="알림 메시지 목록"
        className="fixed bottom-6 right-6 z-50 flex flex-col gap-2 max-w-md w-full pointer-events-none px-4 sm:px-0"
      >
        <AnimatePresence>
          {toasts.map((toast) => (
            <motion.div
              key={toast.id}
              id={`toast-${toast.id}`}
              initial={{ opacity: 0, y: 15, scale: 0.95 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: -10, scale: 0.95 }}
              transition={{ duration: 0.2 }}
              className={`pointer-events-auto flex items-start gap-3 p-4 rounded-xl border shadow-xl backdrop-blur-md ${
                toast.type === 'success'
                  ? 'bg-[#151921]/95 border-[#10B981]/40 text-[#E2E8F0]'
                  : toast.type === 'error'
                  ? 'bg-[#151921]/95 border-[#F43F5E]/40 text-[#E2E8F0]'
                  : toast.type === 'warning'
                  ? 'bg-[#151921]/95 border-[#F59E0B]/40 text-[#E2E8F0]'
                  : 'bg-[#151921]/95 border-[#6366F1]/40 text-[#E2E8F0]'
              }`}
            >
              <div className="flex-shrink-0 mt-0.5">
                {toast.type === 'success' && (
                  <CheckCircle2 className="w-5 h-5 text-[#10B981]" aria-hidden="true" />
                )}
                {toast.type === 'error' && (
                  <AlertCircle className="w-5 h-5 text-[#F43F5E]" aria-hidden="true" />
                )}
                {toast.type === 'warning' && (
                  <AlertTriangle className="w-5 h-5 text-[#F59E0B]" aria-hidden="true" />
                )}
                {toast.type === 'info' && (
                  <Info className="w-5 h-5 text-[#6366F1]" aria-hidden="true" />
                )}
              </div>

              <div className="flex-1 text-sm">
                {toast.title && (
                  <h4 className="font-semibold text-white mb-0.5">{toast.title}</h4>
                )}
                <p className="text-sm text-slate-200 leading-relaxed break-words">{toast.message}</p>
              </div>

              <button
                id={`toast-close-${toast.id}`}
                onClick={() => removeToast(toast.id)}
                className="flex-shrink-0 p-1 rounded hover:bg-slate-700/50 text-slate-400 hover:text-white transition-colors"
                aria-label="알림 닫기"
              >
                <X className="w-4 h-4" />
              </button>
            </motion.div>
          ))}
        </AnimatePresence>
      </aside>
    </ToastContext.Provider>
  );
};
