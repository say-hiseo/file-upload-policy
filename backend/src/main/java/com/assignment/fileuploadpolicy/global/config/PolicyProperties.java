package com.assignment.fileuploadpolicy.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.policy")
public record PolicyProperties(
        int customExtensionMaxLength,
        int customExtensionMaxCount
) {
}