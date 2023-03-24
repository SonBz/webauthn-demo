package com.sbz.webauthndemo.service;

import com.sbz.webauthndemo.model.AppUser;
import com.sbz.webauthndemo.repository.AuthenticatorRepository;
import com.sbz.webauthndemo.repository.UserRepository;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Service
@Transactional
public class MfaService {

    private final QrGenerator qrGenerator;
    private final String secret;
    private final SecretGenerator secretGenerator;
    @Autowired
    private AuthenticatorRepository authenticatorRepository;
    @Autowired
    private UserRepository userRepository;

    public MfaService() {
        secretGenerator = new DefaultSecretGenerator(64);
        secret = secretGenerator.generate();
        qrGenerator = new ZxingPngQrGenerator();
    }

    public String createQR(String username) {
        AppUser appUser = userRepository.findByUsername(username)
                .orElse(new AppUser());
        if (null != appUser.getSecretCode()) {
            return null;
        }
        QrData data =  new QrData.Builder()
                .label(username)
                .secret(secret)
                .issuer("AppName")
                .algorithm(HashingAlgorithm.SHA1) // More on this below
                .digits(6)
                .period(30)
                .build();
        try {
            appUser.setSecretCode(secret);
            return getDataUriForImage(qrGenerator.generate(data), qrGenerator.getImageMimeType());
        } catch (QrGenerationException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean verify(String code, String username) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        return userRepository.findByUsername(username)
                .map(appUser -> verifier.isValidCode(appUser.getSecretCode(), code))
                .orElse(false);
    }
}
