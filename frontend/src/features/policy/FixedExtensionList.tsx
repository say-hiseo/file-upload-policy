import React from 'react';
import { ExtensionPolicyResponse } from '../../types';
import { HelpCircle, Check, Loader2 } from 'lucide-react';

interface FixedExtensionListProps {
  fixedPolicies: ExtensionPolicyResponse[];
  savingMap: Record<string, boolean>;
  onToggle: (extension: string) => void;
}

export const FixedExtensionList: React.FC<FixedExtensionListProps> = ({
  fixedPolicies,
  savingMap,
  onToggle,
}) => {
  return (
    <section
      id="fixed-extension-section"
      className="bg-[#151921] border border-[#2D333B] rounded-xl p-6 flex flex-col gap-4 shadow-sm"
      aria-labelledby="fixed-extension-title"
    >
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <h2
            id="fixed-extension-title"
            className="text-base font-semibold text-[#E2E8F0] tracking-tight"
          >
            ① 고정 확장자 ({fixedPolicies.length}개)
          </h2>
          <div className="relative group cursor-help">
            <HelpCircle
              className="w-4 h-4 text-[#94A3B8] hover:text-slate-200 transition-colors"
              aria-label="고정 확장자 도움말"
            />
            <div className="absolute left-0 bottom-full mb-2 hidden group-hover:block w-64 p-2.5 bg-[#0A0C10] border border-[#2D333B] rounded-lg shadow-xl text-xs text-[#94A3B8] z-20 pointer-events-none">
              시스템에 사전 정의된 주요 실행 및 위험 확장자 목록입니다. 체크 시 업로드가 즉시 차단됩니다.
            </div>
          </div>
        </div>
      </div>

      <p className="text-xs sm:text-sm text-[#94A3B8]">
        체크한 확장자는 업로드가 차단됩니다. (체크 해제 시 허용)
      </p>

      {/* Checkboxes List */}
      <div
        id="fixed-extension-checkbox-group"
        className="flex flex-wrap gap-2.5 pt-1"
        role="group"
        aria-label="고정 확장자 차단 선택"
      >
        {fixedPolicies.map((item) => {
          const isSaving = !!savingMap[item.extension.toLowerCase()];
          const inputId = `fixed-ext-checkbox-${item.extension}`;

          return (
            <label
              key={item.extension}
              htmlFor={inputId}
              id={`fixed-ext-label-${item.extension}`}
              className={`flex items-center gap-2 px-3.5 py-2 rounded-lg border transition-all duration-150 cursor-pointer select-none font-mono text-sm ${
                item.blocked
                  ? 'bg-[#6366F1]/10 border-[#6366F1] text-indigo-300 font-semibold shadow-[0_0_14px_rgba(99,102,241,0.18)]'
                  : 'bg-[#0A0C10] border-[#2D333B] text-[#E2E8F0] hover:border-slate-500 hover:bg-[#1B202A]'
              } ${isSaving ? 'opacity-60 cursor-wait' : ''}`}
            >
              <div className="relative flex items-center justify-center">
                <input
                  id={inputId}
                  type="checkbox"
                  checked={item.blocked}
                  disabled={isSaving}
                  onChange={() => onToggle(item.extension)}
                  className="sr-only"
                  aria-label={`${item.extension} 확장자 차단 여부`}
                />
                <div
                  className={`w-4 h-4 rounded flex items-center justify-center border transition-colors ${
                    item.blocked
                      ? 'bg-[#6366F1] border-[#6366F1] text-white'
                      : 'bg-[#151921] border-[#2D333B]'
                  }`}
                >
                  {isSaving ? (
                    <Loader2 className="w-3 h-3 animate-spin text-current" />
                  ) : item.blocked ? (
                    <Check className="w-3 h-3 stroke-[3]" />
                  ) : null}
                </div>
              </div>
              <span>{item.extension}</span>
            </label>
          );
        })}
      </div>
    </section>
  );
};
