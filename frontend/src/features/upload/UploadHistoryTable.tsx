import React, { useState } from 'react';
import { UploadHistoryItemResponse } from '../../types';
import { ChevronDown, Loader2, Download, Check, AlertCircle, History, Lock } from 'lucide-react';
import { Button } from '../../components/Button';

interface UploadHistoryTableProps {
  items: UploadHistoryItemResponse[];
  isAuthenticated: boolean;
  totalCount: number;
  hasMore: boolean;
  isLoading: boolean;
  isLoadingMore: boolean;
  downloadingIds: Record<number, boolean>;
  onLoadMore: () => void;
  onDownload: (item: UploadHistoryItemResponse) => void;
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
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

export const UploadHistoryTable: React.FC<UploadHistoryTableProps> = ({
  items,
  isAuthenticated,
  totalCount,
  hasMore,
  isLoading,
  isLoadingMore,
  downloadingIds,
  onLoadMore,
  onDownload,
}) => {
  const [expandedIds, setExpandedIds] = useState<Set<number>>(new Set());

  const toggleExpanded = (id: number) => {
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

  if (!isAuthenticated) {
    return (
      <section
        id="upload-history-section"
        className="bg-[#151921] border border-[#2D333B] rounded-xl p-6 flex flex-col items-center gap-2 shadow-sm text-center"
        aria-labelledby="upload-history-title"
      >
        <h3 id="upload-history-title" className="text-base font-semibold text-[#E2E8F0] tracking-tight">
          내 업로드 이력
        </h3>
        <div className="flex flex-col items-center gap-2 py-6 text-[#94A3B8]">
          <Lock className="w-5 h-5 text-slate-500" />
          <span className="text-xs">로그인 후 이용 가능한 기능입니다.</span>
        </div>
      </section>
    );
  }

  if (isLoading) {
    return null;
  }

  return (
    <section
      id="upload-history-section"
      className="bg-[#151921] border border-[#2D333B] rounded-xl p-6 flex flex-col gap-4 shadow-sm"
      aria-labelledby="upload-history-title"
    >
      <h3
        id="upload-history-title"
        className="text-base font-semibold text-[#E2E8F0] tracking-tight flex items-center gap-2"
      >
        <span>내 업로드 이력</span>
        <span className="text-xs font-normal text-[#94A3B8]">
          (전체 {totalCount}건 중 {items.length}건 표시)
        </span>
      </h3>

      <div
        id="upload-history-table-wrapper"
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
                업로드 시각
              </th>
              <th className="px-4 py-3 text-xs font-semibold uppercase tracking-wider text-[#94A3B8]" />
            </tr>
          </thead>
          <tbody className="divide-y divide-[#2D333B]">
            {items.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-4 py-8 text-center text-xs text-[#94A3B8]/60">
                  <div className="flex flex-col items-center gap-2">
                    <History className="w-5 h-5 text-slate-500" />
                    <span>업로드 이력이 없습니다.</span>
                  </div>
                </td>
              </tr>
            ) : (
              items.map((item) => {
                const isSuccess = item.status === 'SUCCESS';
                const isDownloading = !!downloadingIds[item.id];
                const isExpanded = expandedIds.has(item.id);
                return (
                  <React.Fragment key={item.id}>
                    <tr
                      className={`transition-colors ${
                        isSuccess ? 'hover:bg-[#1B202A]' : 'hover:bg-[#1B202A] cursor-pointer'
                      }`}
                      onClick={isSuccess ? undefined : () => toggleExpanded(item.id)}
                    >
                      <td className="px-4 py-3 text-[#E2E8F0] font-medium max-w-[220px] truncate" title={item.originalFilename}>
                        {item.originalFilename}
                      </td>
                      <td className="px-4 py-3 font-mono text-[#94A3B8] whitespace-nowrap">
                        {formatFileSize(item.sizeBytes)}
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
                        {formatTimestamp(item.createdAt)}
                      </td>
                      <td className="px-4 py-3 text-right whitespace-nowrap">
                        {item.downloadable && (
                          <button
                            type="button"
                            onClick={(e) => {
                              e.stopPropagation();
                              onDownload(item);
                            }}
                            disabled={isDownloading}
                            className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded bg-[#10B981]/15 hover:bg-[#10B981]/25 text-[#10B981] border border-[#10B981]/30 text-xs font-medium transition-colors disabled:opacity-60 disabled:cursor-wait"
                            aria-label={`${item.originalFilename} 다운로드`}
                          >
                            {isDownloading ? (
                              <Loader2 className="w-3.5 h-3.5 animate-spin" />
                            ) : (
                              <Download className="w-3.5 h-3.5" />
                            )}
                            <span>다운로드</span>
                          </button>
                        )}
                      </td>
                    </tr>
                    {isExpanded && !isSuccess && (
                      <tr className="bg-[#0A0C10]">
                        <td colSpan={5} className="px-4 py-3 text-xs text-[#FFB4AB] border-t border-[#2D333B]/60">
                          거부 사유: {item.rejectReason || '알 수 없는 사유'}
                        </td>
                      </tr>
                    )}
                  </React.Fragment>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {hasMore && (
        <div className="flex justify-end pt-1">
          <Button
            id="upload-history-load-more-btn"
            variant="outline"
            size="sm"
            onClick={onLoadMore}
            disabled={isLoadingMore}
            rightIcon={
              isLoadingMore ? (
                <Loader2 className="w-3.5 h-3.5 animate-spin" />
              ) : (
                <ChevronDown className="w-3.5 h-3.5" />
              )
            }
          >
            더보기
          </Button>
        </div>
      )}
    </section>
  );
};
