package com.assignment.fileuploadpolicy.domain.policy.dto;

import com.assignment.fileuploadpolicy.domain.policy.entity.ExtensionPolicy;
import java.util.List;

public record PolicyOverview(
        List<ExtensionPolicy> fixedPolicies,
        List<ExtensionPolicy> customPolicies,
        int customCount,
        int customMax
) {
}