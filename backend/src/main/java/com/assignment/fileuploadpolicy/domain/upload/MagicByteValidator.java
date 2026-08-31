package com.assignment.fileuploadpolicy.domain.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 대표적인 실행 파일 시그니처(매직바이트) 검증. (CONSIDERATIONS.md 1-1 참고)
 * 확장자 정책이 1차 방어선이라면, 이건 "확장자만 속인 실행 파일"을 잡는 2차 방어선이다.
 * 모든 포맷을 판별하지는 않는다 - 위협도가 높은 실행 파일류만 우선 적용했고,
 * 종합적인 판별이 필요하면 Apache Tika 등 도입이 실무 확장 지점이다.
 */
@Component
public class MagicByteValidator {

    // Windows PE(EXE/DLL) 헤더: 4D 5A ("MZ")
    private static final byte[] WINDOWS_PE_SIGNATURE = {0x4D, 0x5A};
    // 스크립트 셔뱅: 23 21 ("#!") - Linux에서 확장자 없이도 실행 가능 (CONSIDERATIONS.md 1-8)
    private static final byte[] SHEBANG_SIGNATURE = {0x23, 0x21};

    private static final int PEEK_SIZE = 8;

    public boolean isDangerousSignature(MultipartFile file) {
        byte[] header = peekHeader(file);
        return startsWith(header, WINDOWS_PE_SIGNATURE) || startsWith(header, SHEBANG_SIGNATURE);
    }

    private byte[] peekHeader(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            byte[] buffer = new byte[PEEK_SIZE];
            int read = in.read(buffer);
            if (read <= 0) {
                return new byte[0];
            }
            return read == PEEK_SIZE ? buffer : Arrays.copyOf(buffer, read);
        } catch (IOException e) {
            // 헤더를 못 읽으면 이 판단 하나만 건너뛴다. 확장자 차단 검사는
            // 별도로 여전히 작동하므로 전체 방어선이 무력화되지는 않는다.
            return new byte[0];
        }
    }

    private boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }
}