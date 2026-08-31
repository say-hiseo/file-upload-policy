import React from 'react';
import { AuditLogItem } from '../../types';
import { ChevronDown, History, Loader2 } from 'lucide-react';
import { Button } from '../../components/Button';

interface AuditLogTableProps {
  logs: AuditLogItem[];
  totalCount: number;
  hasMore: boolean;
  isLoadingMore: boolean;
  onLoadMore: () => void;
}

function getActionLabel(type: AuditLogItem['type'], action: AuditLogItem['action']): string {
  if (type === 'FIXED') {
    return action === 'BLOCK' ? '고정 확장자 차단' : '고정 확장자 차단 해제';
  }
  return action === 'ADD' ? '커스텀 확장자 추가' : '커스텀 확장자 삭제';
}

function formatTimestamp(iso: string): string {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  const pad = (n: number) => n.toString().padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}

export const AuditLogTable: React.FC<AuditLogTableProps> = ({
  logs,
  totalCount,
  hasMore,
  isLoadingMore,
  onLoadMore,
}) => {
  return (
    <section
      id="audit-log-section"
      className="bg-[#151921] border border-[#2D333B] rounded-xl p-6 flex flex-col gap-4 shadow-sm"
      aria-labelledby="audit-log-title"
    >
      <div className="flex items-center justify-between">
        <h2
          id="audit-log-title"
          className="text-base font-semibold text-[#E2E8F0] tracking-tight flex items-center gap-2"
        >
          <span>③ 변경 이력</span>
          <span className="text-xs font-normal text-[#94A3B8]">
            (전체 {totalCount}건 중 {logs.length}건 표시)
          </span>
        </h2>
      </div>

      <div
        id="audit-log-table-wrapper"
        className="overflow-x-auto border border-[#2D333B] rounded-lg bg-[#0A0C10]"
      >
        <table
          id="audit-log-table"
          className="w-full text-left border-collapse text-xs sm:text-sm"
        >
          <thead className="bg-[#1B202A] border-b border-[#2D333B]">
            <tr>
              <th className="px-4 py-3 text-xs font-semibold uppercase tracking-wider text-[#94A3B8]">
                시간
              </th>
              <th className="px-4 py-3 text-xs font-semibold uppercase tracking-wider text-[#94A3B8]">
                사용자
              </th>
              <th className="px-4 py-3 text-xs font-semibold uppercase tracking-wider text-[#94A3B8]">
                작업
              </th>
              <th className="px-4 py-3 text-xs font-semibold uppercase tracking-wider text-[#94A3B8]">
                대상
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[#2D333B]">
            {logs.length === 0 ? (
              <tr>
                <td
                  colSpan={4}
                  className="px-4 py-8 text-center text-xs text-[#94A3B8]/60"
                >
                  <div className="flex flex-col items-center gap-2">
                    <History className="w-5 h-5 text-slate-500" />
                    <span>아직 기록된 정책 변경 이력이 없습니다.</span>
                  </div>
                </td>
              </tr>
            ) : (
              logs.map((log, index) => {
                const rowId = `audit-log-row-${log.changedAt}-${log.extension}-${index}`;
                return (
                  <tr
                    key={rowId}
                    id={rowId}
                    className="hover:bg-[#1B202A] transition-colors"
                  >
                    <td className="px-4 py-3 font-mono text-[#E2E8F0] whitespace-nowrap">
                      {formatTimestamp(log.changedAt)}
                    </td>
                    <td className="px-4 py-3 font-medium whitespace-nowrap">
                      <span
                        className={`px-2 py-0.5 rounded text-xs font-mono ${
                          log.changedByUsername === 'SYSTEM'
                            ? 'bg-slate-800 text-slate-400 border border-slate-700'
                            : 'bg-indigo-950/60 text-indigo-300 border border-indigo-800/50'
                        }`}
                      >
                        {log.changedByUsername}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-[#E2E8F0] whitespace-nowrap">
                      {getActionLabel(log.type, log.action)}
                    </td>
                    <td className="px-4 py-3 font-mono text-[#E2E8F0] whitespace-nowrap font-semibold">
                      {log.extension}
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {hasMore && (
        <div className="flex justify-end pt-1">
          <Button
            id="audit-log-load-more-btn"
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
