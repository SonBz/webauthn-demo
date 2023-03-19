package com.sbz.webauthndemo.utility;

import com.yubico.webauthn.data.ByteArray;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class Utility {

    private static final SecureRandom random = new SecureRandom();

    public static ByteArray generateRandom(int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return new ByteArray(bytes);
    }
}
