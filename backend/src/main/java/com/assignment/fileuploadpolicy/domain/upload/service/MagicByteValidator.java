package com.assignment.fileuploadpolicy.domain.upload.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class MagicByteValidator {

    private static final byte[] WINDOWS_PE_SIGNATURE = {0x4D, 0x5A};
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