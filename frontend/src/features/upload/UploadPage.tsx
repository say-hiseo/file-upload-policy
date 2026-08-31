import React from 'react';
import { useFileUpload } from './useFileUpload';
import { useUploadHistory } from './useUploadHistory';
import { FileDropzone } from './FileDropzone';
import { FileResultList } from './FileResultList';
import { UploadResultList } from './UploadResultList';
import { UploadHistoryTable } from './UploadHistoryTable';

interface UploadPageProps {
  uploadState: ReturnType<typeof useFileUpload>;
  uploadHistoryState: ReturnType<typeof useUploadHistory>;
}

export const UploadPage: React.FC<UploadPageProps> = ({ uploadState, uploadHistoryState }) => {
  const {
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
  } = uploadState;

  const {
    items: historyItems,
    isAuthenticated: isHistoryAuthenticated,
    totalCount: historyTotalCount,
    hasMore: historyHasMore,
    isLoading: isHistoryLoading,
    isLoadingMore: isHistoryLoadingMore,
    downloadingIds: historyDownloadingIds,
    loadMore: loadMoreHistory,
    downloadItem: downloadHistoryItem,
  } = uploadHistoryState;

  return (
    <div id="upload-page" className="flex flex-col gap-6 w-full max-w-5xl mx-auto">
      {/* Drag & Drop File Selector */}
      <FileDropzone
        onFilesSelected={addFiles}
        onAddTestPresets={addTestPresets}
        disabled={isUploading}
        fileCount={fileList.length}
      />

      {/* Selected Files & Result List */}
      <FileResultList
        files={fileList}
        isUploading={isUploading}
        onRemoveFile={removeFile}
        onClearAll={clearAllFiles}
        onRetryAll={retryAllFiles}
        onUpload={uploadSelectedFiles}
      />

      {/* Local, login-free result of the upload(s) just made this session */}
      <UploadResultList results={uploadResults} onClear={clearUploadResults} />

      {/* Server-side Upload History */}
      <UploadHistoryTable
        items={historyItems}
        isAuthenticated={isHistoryAuthenticated}
        totalCount={historyTotalCount}
        hasMore={historyHasMore}
        isLoading={isHistoryLoading}
        isLoadingMore={isHistoryLoadingMore}
        downloadingIds={historyDownloadingIds}
        onLoadMore={loadMoreHistory}
        onDownload={downloadHistoryItem}
      />
    </div>
  );
};
