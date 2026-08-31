import { useState, useEffect, useCallback } from 'react';
import {
  PolicyOverviewResponse,
  ExtensionPolicyResponse,
  AuditLogItem,
} from '../../types';
import { policyApi } from '../../api/policyApi';
import { useToast } from '../../components/Toast';

const DEFAULT_FIXED_EXTENSIONS = ['bat', 'cmd', 'com', 'cpl', 'exe', 'scr', 'js'];
const AUDIT_LOG_PAGE_SIZE = 5;

export function usePolicyOverview(authKey?: unknown) {
  const [policies, setPolicies] = useState<PolicyOverviewResponse>({
    fixed: DEFAULT_FIXED_EXTENSIONS.map((ext) => ({ extension: ext, blocked: false })),
    custom: [],
    customCount: 0,
    customMax: 200,
  });

  const [auditLogs, setAuditLogs] = useState<AuditLogItem[]>([]);
  const [auditLogsPage, setAuditLogsPage] = useState<number>(0);
  const [auditLogsHasMore, setAuditLogsHasMore] = useState<boolean>(false);
  const [auditLogsTotalCount, setAuditLogsTotalCount] = useState<number>(0);
  const [isLoadingMoreAuditLogs, setIsLoadingMoreAuditLogs] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [savingFixedExts, setSavingFixedExts] = useState<Record<string, boolean>>({});
  const [isAddingCustom, setIsAddingCustom] = useState<boolean>(false);
  const [deletingCustomExts, setDeletingCustomExts] = useState<Record<string, boolean>>({});
  const { showError, showWarning } = useToast();

  const fetchPolicies = useCallback(async () => {
    try {
      const data = await policyApi.getPolicies();

      // Ensure all 7 fixed extensions exist
      const fixedMap = new Map<string, boolean>();
      data.fixed?.forEach((f) => fixedMap.set(f.extension.toLowerCase(), f.blocked));
      const fullFixed: ExtensionPolicyResponse[] = DEFAULT_FIXED_EXTENSIONS.map((ext) => ({
        extension: ext,
        blocked: fixedMap.get(ext) ?? false,
      }));

      setPolicies({
        fixed: fullFixed,
        custom: data.custom || [],
        customCount: data.customCount ?? (data.custom?.length || 0),
        customMax: data.customMax || 200,
      });
    } catch {} finally {
      setIsLoading(false);
    }
  }, []);

  const fetchAuditLogs = useCallback(async () => {
    try {
      const data = await policyApi.getAuditLogs(0, AUDIT_LOG_PAGE_SIZE);
      setAuditLogs(data.items || []);
      setAuditLogsPage(0);
      setAuditLogsHasMore(data.hasMore);
      setAuditLogsTotalCount(data.totalCount);
    } catch {}
  }, []);

  const loadMoreAuditLogs = useCallback(async () => {
    if (isLoadingMoreAuditLogs || !auditLogsHasMore) return;
    setIsLoadingMoreAuditLogs(true);
    try {
      const nextPage = auditLogsPage + 1;
      const data = await policyApi.getAuditLogs(nextPage, AUDIT_LOG_PAGE_SIZE);
      setAuditLogs((prev) => [...prev, ...(data.items || [])]);
      setAuditLogsPage(nextPage);
      setAuditLogsHasMore(data.hasMore);
    } catch {} finally {
      setIsLoadingMoreAuditLogs(false);
    }
  }, [auditLogsPage, auditLogsHasMore, isLoadingMoreAuditLogs]);

  useEffect(() => {
    fetchPolicies();
    fetchAuditLogs();
  }, [fetchPolicies, fetchAuditLogs, authKey]);

  // Optimistic Toggle for Fixed Extension
  const toggleFixedExtension = async (extName: string) => {
    const normalized = extName.toLowerCase();
    const currentItem = policies.fixed.find((f) => f.extension.toLowerCase() === normalized);
    if (!currentItem || savingFixedExts[normalized]) return;

    const previousBlocked = currentItem.blocked;
    const nextBlocked = !previousBlocked;

    // 1. Optimistic update
    setPolicies((prev) => ({
      ...prev,
      fixed: prev.fixed.map((f) =>
        f.extension.toLowerCase() === normalized ? { ...f, blocked: nextBlocked } : f
      ),
    }));
    setSavingFixedExts((prev) => ({ ...prev, [normalized]: true }));

    // 2. Network API call
    try {
      await policyApi.toggleFixed(normalized, { blocked: nextBlocked });
      // Refresh audit logs
      fetchAuditLogs();
    } catch (err: unknown) {
      // 3. Rollback on failure
      setPolicies((prev) => ({
        ...prev,
        fixed: prev.fixed.map((f) =>
          f.extension.toLowerCase() === normalized ? { ...f, blocked: previousBlocked } : f
        ),
      }));
      const errorMsg =
        err instanceof Error ? err.message : `'${normalized}' 고정 확장자 변경에 실패했습니다.`;
      showError(errorMsg, '정책 변경 실패');
    } finally {
      setSavingFixedExts((prev) => {
        const next = { ...prev };
        delete next[normalized];
        return next;
      });
    }
  };

  // Optimistic Add for Custom Extension
  const addCustomExtension = async (extName: string): Promise<boolean> => {
    const normalized = extName.trim().toLowerCase().replace(/^\./, '');
    
    // Client Pre-validations
    if (!normalized) {
      throw new Error('확장자를 입력해주세요.');
    }
    if (normalized.length > 20) {
      throw new Error('확장자는 최대 20자까지 입력 가능합니다.');
    }
    if (!/^[a-zA-Z0-9_-]+$/.test(normalized)) {
      throw new Error('확장자에는 영문, 숫자, 하이픈(-), 밑줄(_)만 사용 가능합니다.');
    }
    if (DEFAULT_FIXED_EXTENSIONS.includes(normalized)) {
      throw new Error(`'${normalized}'은(는) 이미 고정 확장자 목록에 등록되어 있어 추가할 수 없습니다.`);
    }
    if (policies.custom.some((c) => c.extension.toLowerCase() === normalized)) {
      throw new Error(`'${normalized}'은(는) 이미 등록된 확장자입니다.`);
    }
    if (policies.customCount >= policies.customMax) {
      throw new Error(`커스텀 확장자는 최대 ${policies.customMax}개까지 등록 가능합니다.`);
    }

    const previousCustom = [...policies.custom];
    const previousCount = policies.customCount;

    // 1. Optimistic Add
    const optimisticItem: ExtensionPolicyResponse = {
      extension: normalized,
      blocked: true,
    };
    setPolicies((prev) => ({
      ...prev,
      custom: [...prev.custom, optimisticItem],
      customCount: prev.customCount + 1,
    }));
    setIsAddingCustom(true);

    // 2. Network Call
    try {
      await policyApi.addCustom({ extension: normalized });
      fetchAuditLogs();
      return true;
    } catch (err: unknown) {
      // 3. Rollback
      setPolicies((prev) => ({
        ...prev,
        custom: previousCustom,
        customCount: previousCount,
      }));
      throw err;
    } finally {
      setIsAddingCustom(false);
    }
  };

  // Optimistic Delete for Custom Extension
  const deleteCustomExtension = async (extName: string) => {
    const normalized = extName.trim().toLowerCase();
    if (deletingCustomExts[normalized]) return;

    const previousCustom = [...policies.custom];
    const previousCount = policies.customCount;
    const targetItem = policies.custom.find((c) => c.extension.toLowerCase() === normalized);

    if (!targetItem) return;

    // 1. Optimistic Delete
    setPolicies((prev) => ({
      ...prev,
      custom: prev.custom.filter((c) => c.extension.toLowerCase() !== normalized),
      customCount: Math.max(0, prev.customCount - 1),
    }));
    setDeletingCustomExts((prev) => ({ ...prev, [normalized]: true }));

    // 2. Network Call
    try {
      await policyApi.deleteCustom(normalized);
      fetchAuditLogs();
    } catch (err: unknown) {
      // 3. Rollback
      setPolicies((prev) => ({
        ...prev,
        custom: previousCustom,
        customCount: previousCount,
      }));
      const errorMsg =
        err instanceof Error ? err.message : `'${normalized}' 확장자 삭제에 실패했습니다.`;
      showError(errorMsg, '삭제 실패');
    } finally {
      setDeletingCustomExts((prev) => {
        const next = { ...prev };
        delete next[normalized];
        return next;
      });
    }
  };

  return {
    policies,
    auditLogs,
    auditLogsHasMore,
    auditLogsTotalCount,
    isLoadingMoreAuditLogs,
    isLoading,
    savingFixedExts,
    isAddingCustom,
    deletingCustomExts,
    toggleFixedExtension,
    addCustomExtension,
    deleteCustomExtension,
    refreshPolicies: fetchPolicies,
    refreshAuditLogs: fetchAuditLogs,
    loadMoreAuditLogs,
  };
}
