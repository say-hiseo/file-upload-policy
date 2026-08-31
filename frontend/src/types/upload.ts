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
  createdAt: string; // ISO datetime
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

// A POST /api/uploads response, kept client-side so "what did I just upload"
// (including the rejection reason) is visible without a login-gated history call.
export interface UploadResultItem {
  id: string;
  filename: string;
  size: number;
  status: "SUCCESS" | "REJECTED";
  reason: string | null;
  uploadedAt: string; // client-side ISO timestamp
}
