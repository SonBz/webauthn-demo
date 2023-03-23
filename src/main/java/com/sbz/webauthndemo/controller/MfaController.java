package com.sbz.webauthndemo.controller;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Controller
public class MfaController {
    private final QrGenerator qrGenerator;
    private final String secret;
    private final SecretGenerator secretGenerator;

    public MfaController() {
        secretGenerator = new DefaultSecretGenerator(64);
        secret = secretGenerator.generate();
        qrGenerator = new ZxingPngQrGenerator();
    }

    @GetMapping("/mfa")
    public String mfaPage() {
        return "mfa";
    }
    @GetMapping("/mfa")
    public String setup() throws QrGenerationException {
        QrData data =  new QrData.Builder()
                .label("example@example.com")
                .secret(secret)
                .issuer("AppName")
                .algorithm(HashingAlgorithm.SHA1) // More on this below
                .digits(6)
                .period(30)
                .build();

        // Generate the QR code image data as a base64 string which
        // can be used in an <img> tag:
        return getDataUriForImage(
                qrGenerator.generate(data),
                qrGenerator.getImageMimeType()
        );
    }

    @PostMapping("/mfa/verify")
    @ResponseBody
    public ResponseEntity<String> verify(@RequestParam String confirm) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        if (verifier.isValidCode(secret, confirm)) {
            return ResponseEntity.ok("OK");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("INVALID CODE");
    }
}
