import React, { useState } from 'react';
import { UploadResultItem } from '../../types';
import { Check, AlertCircle, ChevronDown, Trash2 } from 'lucide-react';
import { Button } from '../../components/Button';

interface UploadResultListProps {
  results: UploadResultItem[];
  onClear: () => void;
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function formatTimestamp(iso: string): string {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  const pad = (n: number) => n.toString().padStart(2, '0');
  return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}

// The direct response to "what just happened to the file I uploaded" — kept in
// local state so it's visible to guests too, without a login-gated history call.
export const UploadResultList: React.FC<UploadResultListProps> = ({ results, onClear }) => {
  const [expandedIds, setExpandedIds] = useState<Set<string>>(new Set());

  if (results.length === 0) {
    return null;
  }

  const toggleExpanded = (id: string) => {
    setExpandedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  };

  const successCount = results.filter((r) => r.status === 'SUCCESS').length;
  const rejectedCount = results.length - successCount;

  return (
    <section
      id="upload-result-section"
      className="bg-[#151921] border border-[#2D333B] rounded-xl p-6 flex flex-col gap-4 shadow-sm"
      aria-labelledby="upload-result-title"
    >
      <div className="flex items-center justify-between gap-2">
        <h3
          id="upload-result-title"
          className="text-base font-semibold text-[#E2E8F0] tracking-tight flex items-center gap-2"
        >
          <span>업로드 결과</span>
          <span className="text-xs font-normal text-[#94A3B8]">
            (성공 {successCount}건 · 거부 {rejectedCount}건)
          </span>
        </h3>
        <Button
          id="clear-upload-results-btn"
          variant="outline"
          size="sm"
          onClick={onClear}
          leftIcon={<Trash2 className="w-3.5 h-3.5" />}
        >
          지우기
        </Button>
      </div>

      <div
        id="upload-result-table-wrapper"
        className="overflow-x-auto border border-[#2D333B] rounded-lg bg-[#0A0C10]"
      >
        <table className="w-full text-left border-collapse text-xs sm:text-sm">
          <thead className="bg-[#1B202A] border-b border-[#2D333B]">
            <tr>
              <th className="px-4 py-3 text-xs font-semibold uppercase tracking-wider text-[#94A3B8]">
                파일명
              </th>
              <th className="px-4 py-3 text-xs font-semibold uppercase tracking-wider text-[#94A3B8]">
                크기
              </th>
              <th className="px-4 py-3 text-xs font-semibold uppercase tracking-wider text-[#94A3B8]">
                상태
              </th>
              <th className="px-4 py-3 text-xs font-semibold uppercase tracking-wider text-[#94A3B8]">
                시각
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[#2D333B]">
            {results.map((item) => {
              const isSuccess = item.status === 'SUCCESS';
              const isExpanded = expandedIds.has(item.id);
              return (
                <React.Fragment key={item.id}>
                  <tr
                    className={`transition-colors ${
                      isSuccess ? 'hover:bg-[#1B202A]' : 'hover:bg-[#1B202A] cursor-pointer'
                    }`}
                    onClick={isSuccess ? undefined : () => toggleExpanded(item.id)}
                  >
                    <td
                      className="px-4 py-3 text-[#E2E8F0] font-medium max-w-[220px] truncate"
                      title={item.filename}
                    >
                      {item.filename}
                    </td>
                    <td className="px-4 py-3 font-mono text-[#94A3B8] whitespace-nowrap">
                      {formatFileSize(item.size)}
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap">
                      {isSuccess ? (
                        <span className="font-semibold text-[#10B981] flex items-center gap-1">
                          <Check className="w-3.5 h-3.5" />
                          성공
                        </span>
                      ) : (
                        <span className="font-semibold text-[#F43F5E] flex items-center gap-1">
                          <AlertCircle className="w-3.5 h-3.5" />
                          거부됨
                          <ChevronDown
                            className={`w-3 h-3 transition-transform ${isExpanded ? 'rotate-180' : ''}`}
                          />
                        </span>
                      )}
                    </td>
                    <td className="px-4 py-3 font-mono text-[#94A3B8] whitespace-nowrap">
                      {formatTimestamp(item.uploadedAt)}
                    </td>
                  </tr>
                  {isExpanded && !isSuccess && (
                    <tr className="bg-[#0A0C10]">
                      <td colSpan={4} className="px-4 py-3 text-xs text-[#FFB4AB] border-t border-[#2D333B]/60">
                        거부 사유: {item.reason || '알 수 없는 사유'}
                      </td>
                    </tr>
                  )}
                </React.Fragment>
              );
            })}
          </tbody>
        </table>
      </div>
    </section>
  );
};
