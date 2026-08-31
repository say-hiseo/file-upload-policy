import React, { useRef, useState } from 'react';
import { UploadCloud, Sparkles, FolderPlus } from 'lucide-react';
import { Button } from '../../components/Button';

interface FileDropzoneProps {
  onFilesSelected: (files: File[]) => void;
  onAddTestPresets: () => void;
  disabled?: boolean;
  fileCount: number;
}

export const FileDropzone: React.FC<FileDropzoneProps> = ({
  onFilesSelected,
  onAddTestPresets,
  disabled = false,
  fileCount,
}) => {
  const [isDragOver, setIsDragOver] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleDragEnter = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (!disabled) setIsDragOver(true);
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (!disabled) setIsDragOver(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);
    if (disabled) return;

    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      const filesArray = Array.from(e.dataTransfer.files);
      onFilesSelected(filesArray);
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      const filesArray = Array.from(e.target.files);
      onFilesSelected(filesArray);
      // Reset input value so same files can be re-selected if removed
      e.target.value = '';
    }
  };

  const triggerFileInput = () => {
    if (!disabled && fileInputRef.current) {
      fileInputRef.current.click();
    }
  };

  return (
    <div className="flex flex-col gap-3 w-full">
      {/* Drag & Drop Zone */}
      <div
        id="file-dropzone-container"
        onDragEnter={handleDragEnter}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        onClick={triggerFileInput}
        role="button"
        tabIndex={0}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            triggerFileInput();
          }
        }}
        aria-label="파일 업로드 선택 영역: 클릭하거나 파일을 끌어다 놓으세요"
        className={`w-full bg-[#151921] border-2 border-dashed rounded-2xl p-8 flex flex-col items-center justify-center min-h-[200px] transition-all cursor-pointer select-none group ${
          isDragOver
            ? 'border-[#6366F1] bg-[#6366F1]/10 scale-[1.005]'
            : 'border-[#2D333B] hover:border-[#6366F1]/60 hover:bg-[#1B202A]'
        } ${disabled ? 'opacity-50 cursor-not-allowed' : ''}`}
      >
        <input
          ref={fileInputRef}
          id="hidden-file-input"
          type="file"
          multiple
          disabled={disabled}
          onChange={handleFileChange}
          className="sr-only"
        />

        <div
          id="dropzone-icon-badge"
          className="w-14 h-14 bg-[#1B202A] group-hover:bg-[#6366F1]/20 border border-[#2D333B] rounded-full flex items-center justify-center mb-3 text-[#6366F1] group-hover:scale-110 transition-all duration-200 shadow-inner"
        >
          <UploadCloud className="w-7 h-7" aria-hidden="true" />
        </div>

        <h2
          id="dropzone-title"
          className="text-base font-semibold text-[#E2E8F0] mb-1 text-center group-hover:text-indigo-200 transition-colors"
        >
          파일을 드래그하거나 클릭해서 선택하세요
        </h2>

        <p id="dropzone-subtitle" className="text-xs text-[#94A3B8] text-center">
          여러 파일을 선택할 수 있습니다 (최대 10개, 개당 10MB)
        </p>

        {fileCount > 0 && (
          <div className="mt-3 text-xs text-indigo-400 font-mono bg-indigo-950/40 border border-indigo-800/40 px-3 py-1 rounded-full">
            현재 {fileCount}개 파일 대기 중
          </div>
        )}
      </div>

      {/* Quick Test Helper Toolbar */}
      <div className="flex flex-wrap items-center justify-between gap-2 px-1">
        <span className="text-xs text-[#94A3B8]">
          💡 테스트를 위한 모의 파일 프리셋을 원클릭으로 로드할 수 있습니다:
        </span>
        <Button
          id="load-test-presets-btn"
          type="button"
          variant="outline"
          size="sm"
          onClick={onAddTestPresets}
          leftIcon={<Sparkles className="w-3.5 h-3.5 text-indigo-400" />}
          className="text-xs text-indigo-300 hover:text-white border-indigo-800/60 bg-indigo-950/20 hover:bg-indigo-900/30"
        >
          검증용 테스트 파일 5종 불러오기
        </Button>
      </div>
    </div>
  );
};
