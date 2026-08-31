package com.assignment.fileuploadpolicy.domain.upload;

import java.util.ArrayList;
import java.util.List;

/**
 * 파일명에서 검사 대상 확장자 조각들을 추출한다. (CONSIDERATIONS.md 1-2, 1-3 참고)
 * - 이중 확장자(file.exe.txt)는 '.' 기준으로 분리된 모든 조각을 반환한다
 *   (마지막 확장자만 보지 않아 file.exe.txt 같은 우회를 방지한다)
 * - ".env", ".gitignore"처럼 '.'으로 시작하고 그 뒤에 다른 '.'이 없는 파일은
 *   확장자 없는 파일로 취급한다 (빈 리스트 반환)
 * - 반환값은 정규화 전 원본 조각이다. 호출자가 ExtensionNormalizer로 정규화한다.
 */
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