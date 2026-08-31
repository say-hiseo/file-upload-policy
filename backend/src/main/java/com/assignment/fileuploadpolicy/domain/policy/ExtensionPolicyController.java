package com.assignment.fileuploadpolicy.domain.policy;

import com.assignment.fileuploadpolicy.domain.policy.dto.*;
import com.assignment.fileuploadpolicy.global.auth.ActorContext;
import com.assignment.fileuploadpolicy.global.auth.ActorContextResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Extension Policy", description = "확장자 차단 정책 관리 API")
@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class ExtensionPolicyController {

    private final ExtensionPolicyService extensionPolicyService;
    private final ActorContextResolver actorContextResolver;

    @Operation(summary = "정책 전체 조회")
    @GetMapping
    public PolicyOverviewResponse getPolicies() {
        return PolicyOverviewResponse.from(extensionPolicyService.getPolicyOverview());
    }

    @Operation(summary = "고정 확장자 체크/해제")
    @PatchMapping("/fixed/{extension}")
    public void toggleFixed(@PathVariable String extension,
                            @Valid @RequestBody ToggleFixedExtensionRequest request,
                            HttpSession session) {
        ActorContext actor = actorContextResolver.resolve(session);
        extensionPolicyService.toggleFixed(extension, request.blocked(), actor);
    }

    @Operation(summary = "커스텀 확장자 추가")
    @PostMapping("/custom")
    @ResponseStatus(HttpStatus.CREATED)
    public ExtensionPolicyResponse addCustom(@Valid @RequestBody AddCustomExtensionRequest request,
                                             HttpSession session) {
        ActorContext actor = actorContextResolver.resolve(session);
        ExtensionPolicy saved = extensionPolicyService.addCustom(request.extension(), actor);
        return ExtensionPolicyResponse.from(saved);
    }

    @Operation(summary = "커스텀 확장자 삭제")
    @DeleteMapping("/custom/{extension}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCustom(@PathVariable String extension, HttpSession session) {
        ActorContext actor = actorContextResolver.resolve(session);
        extensionPolicyService.removeCustom(extension, actor);
    }

    @Operation(summary = "정책 변경 이력 조회", description = "최근 변경 이력을 최신순으로 조회한다 (기본 5건)")
    @GetMapping("/audit-logs")
    public AuditLogListResponse getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return AuditLogListResponse.from(extensionPolicyService.getAuditLogs(pageable));
    }
}