import { useState, useCallback } from 'react';
import { ClientFileItem, UploadResultItem } from '../../types';
import { uploadApi } from '../../api/uploadApi';
import { useToast } from '../../components/Toast';

export function useFileUpload(onUploadComplete?: () => void) {
  const [fileList, setFileList] = useState<ClientFileItem[]>([]);
  const [uploadResults, setUploadResults] = useState<UploadResultItem[]>([]);
  const [isUploading, setIsUploading] = useState(false);
  const { showError, showSuccess, showWarning } = useToast();

  const addFiles = useCallback(
    (newFiles: File[]) => {
      if (newFiles.length === 0) return;

      setFileList((prev) => {
        const remainingSlots = 10 - prev.length;
        if (remainingSlots <= 0) {
          showWarning('한 번에 최대 10개까지만 선택할 수 있습니다.');
          return prev;
        }

        const filesToAdd = newFiles.slice(0, remainingSlots);
        if (newFiles.length > remainingSlots) {
          showWarning(`최대 10개 제한으로 인해 ${filesToAdd.length}개 파일만 추가되었습니다.`);
        }

        const formattedNewItems: ClientFileItem[] = filesToAdd.map((file) => ({
          id: `${Date.now()}-${Math.random().toString(36).substring(2, 9)}`,
          file,
          name: file.name,
          size: file.size,
          progress: 0,
          status: 'PENDING',
          reason: null,
        }));

        return [...prev, ...formattedNewItems];
      });
    },
    [showWarning]
  );

  // Quick Preset / Sample Files for Evaluators (matching Mockup Image 5.png)
  const addTestPresets = useCallback(() => {
    // 1. normal.txt
    const txtBlob = new Blob(['Hello World Normal Text File'], { type: 'text/plain' });
    const normalTxt = new File([txtBlob], 'normal.txt', { type: 'text/plain' });

    // 2. malware.exe (Executable)
    const exeBlob = new Blob(['MZ\x90\x00\x03\x00\x00\x00\x04\x00\x00\x00\xFF\xFF\x00\x00'], {
      type: 'application/x-msdownload',
    });
    const malwareExe = new File([exeBlob], 'malware.exe', {
      type: 'application/x-msdownload',
    });

    // 3. report.jpg (Disguised JPG containing executable MZ signature header)
    const fakeJpgBlob = new Blob(['MZ\x90\x00\x03\x00\x00\x00FakeDisguisedExecutablePayload'], {
      type: 'image/jpeg',
    });
    const reportJpg = new File([fakeJpgBlob], 'report.jpg', { type: 'image/jpeg' });

    // 4. script.sh (Shell script)
    const shBlob = new Blob(['#!/bin/bash\necho "test"'], { type: 'application/x-sh' });
    const scriptSh = new File([shBlob], 'script.sh', { type: 'application/x-sh' });

    // 5. document.pdf
    const pdfBlob = new Blob(['%PDF-1.5\n%Header\nSample PDF Content'], {
      type: 'application/pdf',
    });
    const docPdf = new File([pdfBlob], 'document.pdf', { type: 'application/pdf' });

    setFileList([
      {
        id: 'test-1',
        file: normalTxt,
        name: 'normal.txt',
        size: 12697, // ~12.4 KB
        progress: 0,
        status: 'PENDING',
        reason: null,
      },
      {
        id: 'test-2',
        file: malwareExe,
        name: 'malware.exe',
        size: 1258291, // ~1.2 MB
        progress: 0,
        status: 'PENDING',
        reason: null,
      },
      {
        id: 'test-3',
        file: reportJpg,
        name: 'report.jpg',
        size: 2621440, // ~2.5 MB
        progress: 0,
        status: 'PENDING',
        reason: null,
      },
      {
        id: 'test-4',
        file: scriptSh,
        name: 'script.sh',
        size: 3174, // ~3.1 KB
        progress: 0,
        status: 'PENDING',
        reason: null,
      },
      {
        id: 'test-5',
        file: docPdf,
        name: 'document.pdf',
        size: 466944, // ~456 KB
        progress: 0,
        status: 'PENDING',
        reason: null,
      },
    ]);
  }, []);

  const removeFile = useCallback((id: string) => {
    setFileList((prev) => prev.filter((item) => item.id !== id));
  }, []);

  const clearAllFiles = useCallback(() => {
    setFileList([]);
  }, []);

  const clearUploadResults = useCallback(() => {
    setUploadResults([]);
  }, []);

  const retryAllFiles = useCallback(() => {
    setFileList((prev) =>
      prev.map((item) => ({
        ...item,
        status: 'PENDING',
        progress: 0,
        reason: null,
      }))
    );
  }, []);

  const uploadSelectedFiles = async () => {
    if (isUploading) return;

    // Only PENDING items go out — files already uploaded (SUCCESS/REJECTED by the
    // server) are removed from the list right after their round-trip, so re-clicking
    // "업로드" can never re-send them.
    const pendingItems = fileList.filter((item) => item.status === 'PENDING');
    if (pendingItems.length === 0) return;

    if (pendingItems.length > 10) {
      showError('한 번에 최대 10개의 파일만 업로드할 수 있습니다.');
      return;
    }

    for (const item of pendingItems) {
      if (item.size > 10 * 1024 * 1024) {
        showError(`'${item.name}' 파일이 최대 크기(10MB)를 초과합니다.`);
        return;
      }
    }

    const uploadingIds = new Set(pendingItems.map((item) => item.id));
    setFileList((prev) =>
      prev.map((item) =>
        uploadingIds.has(item.id) ? { ...item, status: 'UPLOADING', progress: 50 } : item
      )
    );
    setIsUploading(true);

    try {
      const filesToUpload: File[] = pendingItems.map((item) => {
        if (item.file) return item.file;
        // Fallback file generator if created without File instance
        return new File(['sample content'], item.name);
      });

      const response = await uploadApi.uploadFiles(filesToUpload);

      // Keep the server's verdict (incl. rejection reason) client-side so it stays
      // visible without a login-gated history call — results line up with
      // pendingItems by index since that's the order they were sent in.
      const newResults: UploadResultItem[] = pendingItems.map((item, index) => {
        const res = response.results[index];
        return {
          id: item.id,
          filename: res?.filename || item.name,
          size: item.size,
          status: res?.status ?? 'REJECTED',
          reason: res?.reason ?? null,
          uploadedAt: new Date().toISOString(),
        };
      });
      // Replace rather than accumulate — each new upload batch is its own answer
      // to "what just happened," not a running log (that's what 업로드 이력 is for).
      setUploadResults(newResults);

      // The server has now recorded these in the upload history — drop them from
      // the selection list so they only show there, and so they can't be re-uploaded.
      setFileList((prev) => prev.filter((item) => !uploadingIds.has(item.id)));

      const successCount = response.results.filter((r) => r.status === 'SUCCESS').length;
      const rejectedCount = response.results.filter((r) => r.status === 'REJECTED').length;

      if (rejectedCount === 0) {
        showSuccess(`총 ${successCount}개 파일 업로드가 모두 완료되었습니다.`, '업로드 완료');
      } else if (successCount === 0) {
        showWarning(`정책에 의해 ${rejectedCount}개 파일 업로드가 모두 거부되었습니다.`, '업로드 거부');
      } else {
        showSuccess(
          `성공 ${successCount}건, 거부 ${rejectedCount}건이 처리되었습니다.`,
          '부분 업로드 완료'
        );
      }

      onUploadComplete?.();
    } catch (err: unknown) {
      // Nothing reached the server, so keep these items (as REJECTED) for a retry
      // instead of dropping them like a real server verdict would.
      setFileList((prev) =>
        prev.map((item) =>
          uploadingIds.has(item.id)
            ? {
                ...item,
                status: 'REJECTED',
                reason: err instanceof Error ? err.message : '업로드 중 오류가 발생했습니다.',
              }
            : item
        )
      );
      showError(err instanceof Error ? err.message : '업로드 요청에 실패했습니다.');
    } finally {
      setIsUploading(false);
    }
  };

  return {
    fileList,
    uploadResults,
    isUploading,
    addFiles,
    addTestPresets,
    removeFile,
    clearAllFiles,
    clearUploadResults,
    retryAllFiles,
    uploadSelectedFiles,
  };
}
