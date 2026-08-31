package com.assignment.fileuploadpolicy.domain.policy.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public final class ExtensionNormalizer {

    private static final Pattern ALLOWED_PATTERN = Pattern.compile("^[a-z0-9]+$");

    private static final Pattern DISALLOWED_CONTROL_OR_FORMAT_CHARS =
            Pattern.compile("[\\p{Cc}\\p{Cf}]");

    private ExtensionNormalizer() {
    }

    public static Optional<String> normalize(String rawInput) {
        if (rawInput == null) {
            return Optional.empty();
        }

        if (rawInput.indexOf('\u0000') >= 0) {
            return Optional.empty();
        }

        String value = rawInput.strip();

        value = Normalizer.normalize(value, Normalizer.Form.NFKC);

        if (DISALLOWED_CONTROL_OR_FORMAT_CHARS.matcher(value).find()) {
            return Optional.empty();
        }

        value = value.toLowerCase(Locale.ROOT);

        value = stripLeadingAndTrailingDots(value);

        if (!ALLOWED_PATTERN.matcher(value).matches()) {
            return Optional.empty();
        }

        return Optional.of(value);
    }

    private static String stripLeadingAndTrailingDots(String value) {
        int start = 0;
        int end = value.length();
        while (start < end && value.charAt(start) == '.') {
            start++;
        }
        while (end > start && value.charAt(end - 1) == '.') {
            end--;
        }
        return value.substring(start, end);
    }
}