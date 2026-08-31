import { apiRequest } from './client';
import {
  PolicyOverviewResponse,
  ExtensionPolicyResponse,
  ToggleFixedExtensionRequest,
  AddCustomExtensionRequest,
  AuditLogListResponse,
} from '../types';

export const policyApi = {
  getPolicies: (): Promise<PolicyOverviewResponse> => {
    return apiRequest<PolicyOverviewResponse>('/api/policies', {
      method: 'GET',
    });
  },

  toggleFixed: (extension: string, data: ToggleFixedExtensionRequest): Promise<void> => {
    return apiRequest<void>(`/api/policies/fixed/${encodeURIComponent(extension)}`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    });
  },

  addCustom: (data: AddCustomExtensionRequest): Promise<ExtensionPolicyResponse> => {
    return apiRequest<ExtensionPolicyResponse>('/api/policies/custom', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    });
  },

  deleteCustom: (extension: string): Promise<void> => {
    return apiRequest<void>(`/api/policies/custom/${encodeURIComponent(extension)}`, {
      method: 'DELETE',
    });
  },

  getAuditLogs: (page = 0, size = 5): Promise<AuditLogListResponse> => {
    return apiRequest<AuditLogListResponse>(
      `/api/policies/audit-logs?page=${page}&size=${size}`,
      { method: 'GET' }
    );
  },
};
