import { apiRequest, CustomApiError } from './client';
import { UploadResponse, UploadHistoryResponse } from '../types';

export const uploadApi = {
  uploadFiles: (files: File[]): Promise<UploadResponse> => {
    const formData = new FormData();
    files.forEach((file) => {
      formData.append('files', file);
    });

    return apiRequest<UploadResponse>('/api/uploads', {
      method: 'POST',
      body: formData,
    });
  },

  getHistory: (page = 0, size = 10): Promise<UploadHistoryResponse> => {
    return apiRequest<UploadHistoryResponse>(
      `/api/uploads/history?page=${page}&size=${size}`,
      { method: 'GET' }
    );
  },

  downloadFile: async (id: number, filename: string): Promise<void> => {
    const res = await fetch(`/api/uploads/${id}/download`, {
      credentials: 'include',
    });

    if (!res.ok) {
      throw new CustomApiError('파일 다운로드에 실패했습니다.');
    }

    const blob = await res.blob();
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  },
};
