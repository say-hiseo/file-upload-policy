package com.assignment.fileuploadpolicy.domain.policy;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 확장자 입력값 정규화 파이프라인. (CONSIDERATIONS.md 1-4 참고)
 *
 * 정책 등록(A)과 업로드 검증(B) 양쪽에서 반드시 이 클래스 하나만 거치도록 한다.
 *
 * 처리 순서:
 *   1. null byte 거부
 *   2. 공백 제거 (앞뒤) - tab/space/newline 등 일반 공백을 우선 정리
 *   3. NFKC 정규화 (동일하게 보이는 문자가 다른 바이트로 조합되는 것 방지)
 *   4. 유니코드 제어/포맷 문자 거부 (trim 이후에도 "중간에" 남아있는 제어문자,
 *      RTLO U+202E 등 파일명 위장 기법 방지) - 가장자리 공백은 이미 2단계에서
 *      제거됐으므로, 여기서 걸리는 건 중간에 삽입된 비정상적인 문자다.
 *   5. 소문자 변환
 *   6. 앞뒤 '.' 제거
 *   7. 허용 문자 화이트리스트 검증: ^[a-z0-9]+$
 *
 * 7번 화이트리스트가 최종 안전망 역할을 한다.
 */
public final class ExtensionNormalizer {

    private static final Pattern ALLOWED_PATTERN = Pattern.compile("^[a-z0-9]+$");

    // Unicode 카테고리 Cc(제어문자) + Cf(포맷문자, RTLO U+202E 포함) 거부
    private static final Pattern DISALLOWED_CONTROL_OR_FORMAT_CHARS =
            Pattern.compile("[\\p{Cc}\\p{Cf}]");

    private ExtensionNormalizer() {
    }

    public static Optional<String> normalize(String rawInput) {
        if (rawInput == null) {
            return Optional.empty();
        }

        // 1. null byte 거부 (trim보다 먼저 - null byte는 위치와 무관하게 항상 위험)
        if (rawInput.indexOf('\u0000') >= 0) {
            return Optional.empty();
        }

        // 2. 앞뒤 공백 제거 (tab, space, newline 등)
        String value = rawInput.strip();

        // 3. NFKC 정규화
        value = Normalizer.normalize(value, Normalizer.Form.NFKC);

        // 4. 제어/포맷 문자 거부 (trim 이후 "중간"에 남아있는 것만 대상)
        if (DISALLOWED_CONTROL_OR_FORMAT_CHARS.matcher(value).find()) {
            return Optional.empty();
        }

        // 5. 소문자 변환
        value = value.toLowerCase(Locale.ROOT);

        // 6. 앞뒤 '.' 제거
        value = stripLeadingAndTrailingDots(value);

        // 7. 화이트리스트 검증
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