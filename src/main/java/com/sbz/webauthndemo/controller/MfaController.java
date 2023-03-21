package com.sbz.webauthndemo.controller;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@RestController
@RequestMapping("api/mfa")
public class MfaController {
    private final QrDataFactory qrDataFactory;
    private final QrGenerator qrGenerator;
    private final CodeVerifier verifier;
    private final String secret;

    public MfaController(SecretGenerator secretGenerator,
                         QrDataFactory qrDataFactory,
                         QrGenerator qrGenerator,
                         CodeVerifier verifier) {
        secret = secretGenerator.generate();
        this.qrDataFactory = qrDataFactory;
        this.qrGenerator = qrGenerator;
        this.verifier = verifier;
    }


    @GetMapping("setup")
    public String setup() throws QrGenerationException {
        QrData data = qrDataFactory.newBuilder()
                .label("example@example.com")
                .secret(secret)
                .issuer("AppName")
                .build();

        // Generate the QR code image data as a base64 string which
        // can be used in an <img> tag:
        return getDataUriForImage(
                qrGenerator.generate(data),
                qrGenerator.getImageMimeType()
        );
    }

    @GetMapping("verify/{code}")
    public ResponseEntity<String> verify(@PathVariable String code) {
        if (verifier.isValidCode(secret, code)) {
            return ResponseEntity.ok("OK");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("INVALID CODE");
    }
}
