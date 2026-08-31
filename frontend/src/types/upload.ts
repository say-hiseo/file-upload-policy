export interface UploadResultResponse {
  filename: string;
  status: "SUCCESS" | "REJECTED";
  reason: string | null;
}

export interface UploadResponse {
  results: UploadResultResponse[];
}

export interface UploadHistoryItemResponse {
  id: number;
  originalFilename: string;
  status: "SUCCESS" | "REJECTED";
  rejectReason: string | null;
  sizeBytes: number;
  createdAt: string;
  downloadable: boolean;
}

export interface UploadHistoryResponse {
  items: UploadHistoryItemResponse[];
  totalCount: number;
  hasMore: boolean;
}

export interface ClientFileItem {
  id: string;
  file?: File;
  name: string;
  size: number;
  progress: number;
  status: "PENDING" | "UPLOADING" | "SUCCESS" | "REJECTED";
  reason: string | null;
  isMockFile?: boolean;
}

export interface UploadResultItem {
  id: string;
  filename: string;
  size: number;
  status: "SUCCESS" | "REJECTED";
  reason: string | null;
  uploadedAt: string;
}
