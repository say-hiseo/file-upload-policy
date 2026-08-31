package com.assignment.fileuploadpolicy.domain.upload.service;

import java.util.ArrayList;
import java.util.List;

public final class FileNameParser {

    private FileNameParser() {
    }

    public static List<String> extractExtensionFragments(String filename) {
        if (filename == null || filename.isBlank()) {
            return List.of();
        }

        String withoutLeadingDots = filename.replaceFirst("^\\.+", "");
        if (!withoutLeadingDots.contains(".")) {
            return List.of();
        }

        String[] parts = filename.split("\\.");
        List<String> fragments = new ArrayList<>();
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isBlank()) {
                fragments.add(parts[i]);
            }
        }
        return fragments;
    }
}