import React from 'react';
import { ClientFileItem } from '../../types';
import {
  FileText,
  Image,
  AlertCircle,
  Upload,
  Trash2,
  RotateCcw,
  Loader2,
  X,
} from 'lucide-react';
import { Button } from '../../components/Button';

interface FileResultListProps {
  files: ClientFileItem[];
  isUploading: boolean;
  onRemoveFile: (id: string) => void;
  onClearAll: () => void;
  onRetryAll: () => void;
  onUpload: () => void;
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function getFileExtension(filename: string): string {
  const parts = filename.split('.');
  if (parts.length > 1) {
    return parts.pop()?.toLowerCase() || '';
  }
  return '';
}

function renderFileIcon(filename: string) {
  const ext = getFileExtension(filename);

  if (ext === 'exe' || ext === 'bat' || ext === 'cmd' || ext === 'scr' || ext === 'cpl' || ext === 'com') {
    return (
      <div className="w-8 h-10 bg-[#F43F5E]/20 border border-[#F43F5E]/40 rounded flex items-center justify-center flex-shrink-0">
        <span className="text-[10px] font-bold text-[#FB7185] uppercase font-mono">{ext.slice(0, 3)}</span>
      </div>
    );
  }

  if (ext === 'sh' || ext === 'js' || ext === 'ts' || ext === 'py' || ext === 'json') {
    return (
      <div className="w-8 h-10 bg-[#252C3A] border border-[#2D333B] rounded flex items-center justify-center flex-shrink-0">
        <span className="text-[10px] font-mono text-[#E2E8F0] font-bold">&gt;_</span>
      </div>
    );
  }

  if (ext === 'jpg' || ext === 'jpeg' || ext === 'png' || ext === 'gif' || ext === 'webp' || ext === 'svg') {
    return (
      <div className="w-8 h-10 bg-[#10B981]/15 border border-[#10B981]/30 rounded flex items-center justify-center flex-shrink-0">
        <Image className="w-4 h-4 text-[#10B981]" />
      </div>
    );
  }

  if (ext === 'pdf') {
    return (
      <div className="w-8 h-10 bg-[#F43F5E]/15 border border-[#F43F5E]/30 rounded flex items-center justify-center flex-shrink-0">
        <span className="text-[10px] font-bold text-[#FB7185] uppercase font-mono">PDF</span>
      </div>
    );
  }

  return (
    <div className="w-8 h-10 bg-[#252C3A] border border-[#2D333B] rounded flex items-center justify-center flex-shrink-0">
      <FileText className="w-4 h-4 text-[#94A3B8]" />
    </div>
  );
}

export const FileResultList: React.FC<FileResultListProps> = ({
  files,
  isUploading,
  onRemoveFile,
  onClearAll,
  onRetryAll,
  onUpload,
}) => {
  if (files.length === 0) {
    return null;
  }

  const hasPendingFiles = files.some((f) => f.status === 'PENDING');

  return (
    <section
      id="file-result-section"
      className="w-full bg-[#151921] rounded-xl border border-[#2D333B] overflow-hidden flex flex-col shadow-lg shadow-black/20"
      aria-labelledby="file-result-header-title"
    >
      {/* Header Toolbar */}
      <div className="flex flex-wrap items-center justify-between px-6 py-4 border-b border-[#2D333B] bg-[#1B202A] gap-2">
        <div className="flex items-center gap-2">
          <h3
            id="file-result-header-title"
            className="text-base font-semibold text-[#E2E8F0]"
          >
            선택된 파일 ({files.length}/10)
          </h3>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <Button
            id="retry-all-files-btn"
            variant="outline"
            size="sm"
            onClick={onRetryAll}
            disabled={isUploading}
            leftIcon={<RotateCcw className="w-3.5 h-3.5" />}
          >
            다시 시도
          </Button>

          <Button
            id="clear-all-files-btn"
            variant="danger"
            size="sm"
            onClick={onClearAll}
            disabled={isUploading}
            leftIcon={<Trash2 className="w-3.5 h-3.5" />}
          >
            모두 삭제
          </Button>
        </div>
      </div>

      {/* File Items List */}
      <div
        id="file-items-container"
        className="flex flex-col divide-y divide-[#2D333B] bg-[#0A0C10]"
        role="list"
        aria-label="업로드 대기 및 결과 파일 목록"
      >
        {files.map((item) => {
          const rowId = `file-item-row-${item.id}`;
          const isRejected = item.status === 'REJECTED';
          const isProcessing = item.status === 'UPLOADING';
          const isPending = item.status === 'PENDING';

          return (
            <div
              key={item.id}
              id={rowId}
              role="listitem"
              className={`grid grid-cols-1 md:grid-cols-[auto_1fr_auto_auto_auto] gap-3 md:gap-4 items-center px-6 py-3.5 transition-colors ${
                isRejected
                  ? 'bg-[#F43F5E]/5 hover:bg-[#F43F5E]/10'
                  : 'hover:bg-[#1B202A]'
              }`}
            >
              {/* File Icon */}
              <div className="hidden sm:flex items-center">
                {renderFileIcon(item.name)}
              </div>

              {/* File Name & Mobile Size */}
              <div className="flex flex-col min-w-0 pr-2">
                <div className="flex items-center gap-2">
                  <div className="sm:hidden">{renderFileIcon(item.name)}</div>
                  <span
                    id={`${rowId}-name`}
                    className="text-sm text-[#E2E8F0] font-medium truncate"
                    title={item.name}
                  >
                    {item.name}
                  </span>
                </div>
                <span className="text-xs text-[#94A3B8] md:hidden font-mono mt-0.5">
                  {formatFileSize(item.size)}
                </span>
              </div>

              {/* File Size (Desktop) */}
              <div
                id={`${rowId}-size`}
                className="hidden md:block text-xs font-mono text-[#94A3B8] text-right min-w-[70px]"
              >
                {formatFileSize(item.size)}
              </div>

              {/* Status / Progress Indicator */}
              <div className="flex items-center gap-3 min-w-[140px]">
                {isProcessing && (
                  <div className="flex items-center gap-2 text-xs text-indigo-400 w-full">
                    <Loader2 className="w-4 h-4 animate-spin text-[#6366F1]" />
                    <div className="h-1.5 flex-1 bg-[#252C3A] rounded-full overflow-hidden">
                      <div className="h-full bg-[#6366F1] animate-pulse w-2/3 rounded-full" />
                    </div>
                    <span>검증 중...</span>
                  </div>
                )}

                {isRejected && (
                  <div
                    id={`${rowId}-rejected-badge`}
                    className="flex items-center gap-1 text-[#F43F5E] font-medium text-xs whitespace-nowrap"
                  >
                    <AlertCircle className="w-4 h-4 flex-shrink-0" />
                    <span>거부됨</span>
                  </div>
                )}

                {isPending && (
                  <div
                    id={`${rowId}-pending-badge`}
                    className="text-xs text-slate-400 font-mono"
                  >
                    대기 중
                  </div>
                )}
              </div>

              {/* Action Buttons: Rejection reason / Download / Remove */}
              <div className="flex items-center justify-between md:justify-end gap-2 min-w-[170px] max-w-sm">
                {isRejected && item.reason && (
                  <p
                    id={`${rowId}-reason`}
                    title={item.reason}
                    className="text-xs text-[#FFB4AB] text-left md:text-right truncate font-sans"
                  >
                    {item.reason}
                  </p>
                )}

                {!isUploading && (
                  <button
                    id={`${rowId}-remove-btn`}
                    type="button"
                    onClick={() => onRemoveFile(item.id)}
                    className="p-1.5 rounded text-slate-400 hover:text-white hover:bg-slate-700/40 transition-colors ml-1"
                    aria-label={`${item.name} 목록에서 제거`}
                    title="목록에서 제거"
                  >
                    <X className="w-4 h-4" />
                  </button>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* Footer Action */}
      <div className="p-4 flex items-center justify-between bg-[#1B202A] border-t border-[#2D333B]">
        <div className="text-xs text-[#94A3B8]">
          총 {files.length}개 파일 선택됨 (최대 10개)
        </div>

        <Button
          id="execute-upload-btn"
          type="button"
          variant="primary"
          size="md"
          isLoading={isUploading}
          disabled={!hasPendingFiles || isUploading}
          onClick={onUpload}
          leftIcon={<Upload className="w-4 h-4" />}
          className="shadow-md"
        >
          업로드
        </Button>
      </div>
    </section>
  );
};
