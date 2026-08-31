import { useState, useEffect, useCallback } from 'react';
import { UploadHistoryItemResponse } from '../../types';
import { uploadApi } from '../../api/uploadApi';
import { useToast } from '../../components/Toast';

const HISTORY_PAGE_SIZE = 10;

export function useUploadHistory(isAuthenticated: boolean) {
  const [items, setItems] = useState<UploadHistoryItemResponse[]>([]);
  const [page, setPage] = useState<number>(0);
  const [hasMore, setHasMore] = useState<boolean>(false);
  const [totalCount, setTotalCount] = useState<number>(0);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [isLoadingMore, setIsLoadingMore] = useState<boolean>(false);
  const [downloadingIds, setDownloadingIds] = useState<Record<number, boolean>>({});
  const { showError } = useToast();

  const fetchHistory = useCallback(async () => {
    if (!isAuthenticated) return;
    setIsLoading(true);
    try {
      const data = await uploadApi.getHistory(0, HISTORY_PAGE_SIZE);
      setItems(data.items || []);
      setPage(0);
      setHasMore(data.hasMore);
      setTotalCount(data.totalCount);
    } catch {} finally {
      setIsLoading(false);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    if (isAuthenticated) {
      fetchHistory();
    } else {
      setItems([]);
      setPage(0);
      setHasMore(false);
      setTotalCount(0);
    }
  }, [isAuthenticated, fetchHistory]);

  const loadMore = useCallback(async () => {
    if (isLoadingMore || !hasMore) return;
    setIsLoadingMore(true);
    try {
      const nextPage = page + 1;
      const data = await uploadApi.getHistory(nextPage, HISTORY_PAGE_SIZE);
      setItems((prev) => [...prev, ...(data.items || [])]);
      setPage(nextPage);
      setHasMore(data.hasMore);
    } catch {} finally {
      setIsLoadingMore(false);
    }
  }, [page, hasMore, isLoadingMore]);

  const downloadItem = useCallback(
    async (item: UploadHistoryItemResponse) => {
      if (downloadingIds[item.id]) return;
      setDownloadingIds((prev) => ({ ...prev, [item.id]: true }));
      try {
        await uploadApi.downloadFile(item.id, item.originalFilename);
      } catch (err: unknown) {
        showError(err instanceof Error ? err.message : '파일 다운로드에 실패했습니다.');
      } finally {
        setDownloadingIds((prev) => {
          const next = { ...prev };
          delete next[item.id];
          return next;
        });
      }
    },
    [downloadingIds, showError]
  );

  return {
    items,
    isAuthenticated,
    isLoading,
    isLoadingMore,
    hasMore,
    totalCount,
    downloadingIds,
    loadMore,
    downloadItem,
    refresh: fetchHistory,
  };
}
