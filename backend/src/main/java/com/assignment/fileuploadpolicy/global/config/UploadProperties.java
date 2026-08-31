package com.assignment.fileuploadpolicy.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.upload")
public record UploadProperties(
        long maxFileSizeBytes,
        int maxFileCountPerRequest,
        String storagePath
) {
}