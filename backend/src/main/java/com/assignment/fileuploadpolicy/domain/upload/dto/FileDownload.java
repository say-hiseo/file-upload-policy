package com.assignment.fileuploadpolicy.domain.upload.dto;

import org.springframework.core.io.Resource;

public record FileDownload(Resource resource, String originalFilename) {
}