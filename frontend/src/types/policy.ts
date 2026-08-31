export interface ExtensionPolicyResponse {
  extension: string; // normalized lowercase e.g., "exe", "sh"
  blocked: boolean;
}

export interface PolicyOverviewResponse {
  fixed: ExtensionPolicyResponse[];
  custom: ExtensionPolicyResponse[];
  customCount: number;
  customMax: number; // 200
}

export interface ToggleFixedExtensionRequest {
  blocked: boolean;
}

export interface AddCustomExtensionRequest {
  extension: string; // max 20 chars
}

export interface AuditLogItem {
  changedAt: string; // ISO datetime
  changedByUsername: string;
  action: "BLOCK" | "UNBLOCK" | "ADD" | "REMOVE";
  extension: string;
  type: "FIXED" | "CUSTOM";
}

export interface AuditLogListResponse {
  items: AuditLogItem[];
  totalCount: number;
  hasMore: boolean;
}
