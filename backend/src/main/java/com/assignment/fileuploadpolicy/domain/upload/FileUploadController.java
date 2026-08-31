package com.assignment.fileuploadpolicy.domain.upload;

import com.assignment.fileuploadpolicy.domain.upload.dto.UploadHistoryResponse;
import com.assignment.fileuploadpolicy.domain.upload.dto.UploadResponse;
import com.assignment.fileuploadpolicy.global.auth.ActorContext;
import com.assignment.fileuploadpolicy.global.auth.ActorContextResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.net.URLEncoder;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "File Upload", description = "실제 파일 업로드 및 정책 강제 적용 API")
@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;
    private final ActorContextResolver actorContextResolver;

    @Operation(summary = "파일 업로드")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse upload(@RequestParam("files") List<MultipartFile> files, HttpSession session) {
        ActorContext actor = actorContextResolver.resolve(session);
        return UploadResponse.from(fileUploadService.upload(files, actor));
    }

    @Operation(summary = "내 업로드 이력 조회", description = "로그인한 사용자 본인의 업로드 이력만 조회한다")
    @GetMapping("/history")
    public UploadHistoryResponse getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session) {
        ActorContext actor = actorContextResolver.resolve(session);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return UploadHistoryResponse.from(fileUploadService.getHistory(actor, pageable));
    }

    @Operation(summary = "업로드했던 파일 다운로드", description = "본인이 업로드한 SUCCESS 상태 파일만 다운로드 가능")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id, HttpSession session) {
        ActorContext actor = actorContextResolver.resolve(session);
        FileDownload download = fileUploadService.loadForDownload(id, actor);

        String safeFilename = sanitizeForHeader(download.originalFilename());
        String encodedFilename = URLEncoder.encode(safeFilename, StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                // 저장된 MIME을 신뢰하지 않고 강제로 octet-stream 처리 -> 브라우저가
                // 렌더링하지 않고 무조건 다운로드만 하도록 강제 (1-8 SVG XSS 대응과 동일 원칙)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + safeFilename + "\"; filename*=UTF-8''" + encodedFilename)
                .body(download.resource());
    }

    /**
     * Content-Disposition 헤더 인젝션 방지. 파일명에 개행/제어문자가 섞여 있으면
     * 응답 헤더가 조작될 수 있어(HTTP Response Splitting), 헤더에 넣기 직전 제거한다.
     * (CONSIDERATIONS.md 1-8 "로그 인젝션 방지"와 동일한 원칙을 HTTP 헤더에 적용)
     */
    private String sanitizeForHeader(String filename) {
        return filename.replaceAll("[\\r\\n\"]", "");
    }
}