import React from 'react';
import { usePolicyOverview } from './usePolicyOverview';
import { FixedExtensionList } from './FixedExtensionList';
import { CustomExtensionList } from './CustomExtensionList';
import { AuditLogTable } from './AuditLogTable';
import { Loader2 } from 'lucide-react';

interface PolicyPageProps {
  policyState: ReturnType<typeof usePolicyOverview>;
}

export const PolicyPage: React.FC<PolicyPageProps> = ({ policyState }) => {
  const {
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
    loadMoreAuditLogs,
  } = policyState;

  if (isLoading) {
    return (
      <div
        id="policy-page-loading"
        className="flex flex-col items-center justify-center py-24 gap-3 text-slate-400"
      >
        <Loader2 className="w-8 h-8 animate-spin text-[#6366F1]" />
        <p className="text-sm font-medium">정책 정보를 불러오는 중입니다...</p>
      </div>
    );
  }

  return (
    <div id="policy-page" className="flex flex-col gap-6 w-full max-w-5xl mx-auto">
      {/* 1. 고정 확장자 */}
      <FixedExtensionList
        fixedPolicies={policies.fixed}
        savingMap={savingFixedExts}
        onToggle={toggleFixedExtension}
      />

      {/* 2. 커스텀 확장자 */}
      <CustomExtensionList
        customPolicies={policies.custom}
        customCount={policies.customCount}
        customMax={policies.customMax}
        isAdding={isAddingCustom}
        deletingMap={deletingCustomExts}
        onAdd={addCustomExtension}
        onDelete={deleteCustomExtension}
      />

      {/* 3. 변경 이력 */}
      <AuditLogTable
        logs={auditLogs}
        totalCount={auditLogsTotalCount}
        hasMore={auditLogsHasMore}
        isLoadingMore={isLoadingMoreAuditLogs}
        onLoadMore={loadMoreAuditLogs}
      />
    </div>
  );
};
