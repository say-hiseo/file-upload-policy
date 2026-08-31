import React, { useState } from 'react';
import { ExtensionPolicyResponse } from '../../types';
import { HelpCircle, Plus, AlertCircle, X, Loader2 } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { Button } from '../../components/Button';

interface CustomExtensionListProps {
  customPolicies: ExtensionPolicyResponse[];
  customCount: number;
  customMax: number;
  isAdding: boolean;
  deletingMap: Record<string, boolean>;
  onAdd: (extension: string) => Promise<boolean>;
  onDelete: (extension: string) => void;
}

export const CustomExtensionList: React.FC<CustomExtensionListProps> = ({
  customPolicies,
  customCount,
  customMax,
  isAdding,
  deletingMap,
  onAdd,
  onDelete,
}) => {
  const [inputValue, setInputValue] = useState('');
  const [inlineError, setInlineError] = useState<string | null>(null);

  const isFull = customCount >= customMax;

  const handleAdd = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (isFull || isAdding) return;

    const trimmed = inputValue.trim();
    if (!trimmed) {
      setInlineError('확장자를 입력해주세요.');
      return;
    }

    try {
      setInlineError(null);
      await onAdd(trimmed);
      setInputValue(''); // Clear on success
    } catch (err: unknown) {
      if (err instanceof Error) {
        setInlineError(err.message);
      } else {
        setInlineError('커스텀 확장자 추가에 실패했습니다.');
      }
      // Note: do not clear inputValue on error so user can fix it
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setInputValue(e.target.value);
    if (inlineError) {
      setInlineError(null);
    }
  };

  return (
    <section
      id="custom-extension-section"
      className="bg-[#151921] border border-[#2D333B] rounded-xl p-6 flex flex-col gap-4 shadow-sm"
      aria-labelledby="custom-extension-title"
    >
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <h2
            id="custom-extension-title"
            className="text-base font-semibold text-[#E2E8F0] tracking-tight"
          >
            ② 커스텀 확장자
          </h2>
          <div className="relative group cursor-help">
            <HelpCircle
              className="w-4 h-4 text-[#94A3B8] hover:text-slate-200 transition-colors"
              aria-label="커스텀 확장자 도움말"
            />
            <div className="absolute left-0 bottom-full mb-2 hidden group-hover:block w-72 p-2.5 bg-[#0A0C10] border border-[#2D333B] rounded-lg shadow-xl text-xs text-[#94A3B8] z-20 pointer-events-none">
              관리자가 개별적으로 차단할 확장자를 직접 추가합니다. 최대 {customMax}개까지 등록 가능하며, 등록된 확장자는 업로드가 거부됩니다.
            </div>
          </div>
        </div>

        <div
          id="custom-extension-count-badge"
          className={`text-xs font-mono font-semibold px-2.5 py-1 rounded-md border ${
            isFull
              ? 'bg-[#F43F5E]/20 border-[#F43F5E]/40 text-[#FB7185]'
              : 'bg-[#0A0C10] border-[#2D333B] text-[#94A3B8]'
          }`}
        >
          <span>{customCount}</span> / <span>{customMax}</span>
        </div>
      </div>

      <p className="text-xs sm:text-sm text-[#94A3B8]">
        직접 추가한 확장자는 업로드가 차단됩니다.
      </p>

      {/* Input and Add Form */}
      <form onSubmit={handleAdd} className="flex flex-col gap-2" id="custom-ext-form">
        <div className="flex gap-2.5">
          <div className="relative flex-1">
            <input
              id="custom-extension-input"
              type="text"
              value={inputValue}
              onChange={handleInputChange}
              placeholder={
                isFull
                  ? `최대 개수(${customMax}개)에 도달하여 추가할 수 없습니다`
                  : '확장자를 입력하세요 (최대 20자)'
              }
              maxLength={20}
              disabled={isFull || isAdding}
              className={`w-full bg-[#0A0C10] border rounded-lg px-3.5 py-2 text-sm text-[#E2E8F0] font-mono placeholder-[#94A3B8]/40 focus:outline-none transition-colors ${
                inlineError
                  ? 'border-[#F43F5E] focus:ring-1 focus:ring-[#F43F5E]'
                  : 'border-[#2D333B] focus:border-[#6366F1] focus:ring-1 focus:ring-[#6366F1]'
              } disabled:opacity-50 disabled:cursor-not-allowed`}
              aria-describedby={inlineError ? 'custom-ext-error' : undefined}
            />
            {inputValue && (
              <span className="absolute right-3 top-1/2 -translate-y-1/2 text-[11px] font-mono text-slate-400 pointer-events-none">
                {inputValue.length}/20
              </span>
            )}
          </div>

          <Button
            id="custom-extension-add-btn"
            type="submit"
            variant="primary"
            size="md"
            isLoading={isAdding}
            disabled={isFull || !inputValue.trim()}
            leftIcon={<Plus className="w-4 h-4" />}
          >
            추가
          </Button>
        </div>

        {/* Inline Error Message */}
        {inlineError && (
          <div
            id="custom-ext-error"
            role="alert"
            className="flex items-center gap-1.5 text-xs text-[#FFB4AB] font-medium mt-0.5"
          >
            <AlertCircle className="w-3.5 h-3.5 flex-shrink-0 text-[#F43F5E]" />
            <span>{inlineError}</span>
          </div>
        )}
      </form>

      {/* Custom Extensions Tags List */}
      <div
        id="custom-extension-tags-container"
        className="flex flex-wrap gap-2 pt-2 min-h-[44px]"
        aria-label="등록된 커스텀 확장자 목록"
      >
        {customPolicies.length === 0 ? (
          <div
            id="custom-extension-empty-state"
            className="w-full py-4 text-center text-xs text-[#94A3B8]/60 border border-dashed border-[#2D333B] rounded-lg"
          >
            등록된 커스텀 확장자가 없습니다. 차단할 확장자를 입력 후 추가해주세요.
          </div>
        ) : (
          <AnimatePresence mode="popLayout">
            {customPolicies.map((item) => {
              const isDeleting = !!deletingMap[item.extension.toLowerCase()];
              const tagId = `custom-ext-tag-${item.extension}`;

              return (
                <motion.div
                  key={item.extension}
                  id={tagId}
                  initial={{ opacity: 0, scale: 0.8 }}
                  animate={{ opacity: 1, scale: 1 }}
                  exit={{ opacity: 0, scale: 0.8 }}
                  transition={{ duration: 0.15 }}
                  className={`inline-flex items-center gap-1.5 px-3 py-1 bg-[#F43F5E]/10 border border-[#F43F5E]/40 text-[#FB7185] rounded-md text-sm font-mono transition-opacity ${
                    isDeleting ? 'opacity-40' : 'hover:border-[#F43F5E]/70'
                  }`}
                >
                  <span className="font-semibold">{item.extension}</span>
                  <button
                    id={`delete-custom-ext-${item.extension}-btn`}
                    type="button"
                    onClick={() => onDelete(item.extension)}
                    disabled={isDeleting}
                    aria-label={`${item.extension} 커스텀 확장자 삭제`}
                    className="p-0.5 rounded hover:bg-[#F43F5E]/20 text-[#FB7185] hover:text-red-300 transition-colors focus:outline-none"
                  >
                    {isDeleting ? (
                      <Loader2 className="w-3 h-3 animate-spin" />
                    ) : (
                      <X className="w-3.5 h-3.5 stroke-[2.5]" />
                    )}
                  </button>
                </motion.div>
              );
            })}
          </AnimatePresence>
        )}
      </div>
    </section>
  );
};
