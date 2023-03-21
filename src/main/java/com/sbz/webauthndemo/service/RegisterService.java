package com.sbz.webauthndemo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sbz.webauthndemo.model.AppUser;
import com.sbz.webauthndemo.model.Authenticator;
import com.sbz.webauthndemo.repository.AuthenticatorRepository;
import com.sbz.webauthndemo.repository.UserRepository;
import com.sbz.webauthndemo.utility.Utility;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.exception.RegistrationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import java.io.IOException;

@Service
public class RegisterService {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private AuthenticatorRepository authRepository;
    @Autowired
    private RelyingParty relyingParty;

    public AppUser newUser(String username, String display) {
        AppUser user = userRepo.findByUsername(username);
        if (user == null) {
            UserIdentity userIdentity = UserIdentity.builder()
                    .name(username)
                    .displayName(display)
                    .id(Utility.generateRandom(32))
                    .build();
            return userRepo.save(new AppUser(userIdentity));
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username " + username + " already exists. Choose a new name.");
        }
    }

    public String newPublicKey(AppUser appUser, HttpSession session) {
        AppUser existingUser = userRepo.findByHandle(appUser.getHandle());
        if (existingUser != null) {
            UserIdentity userIdentity = appUser.toUserIdentity();
            StartRegistrationOptions registrationOptions = StartRegistrationOptions.builder()
                    .user(userIdentity)
                    .build();
            PublicKeyCredentialCreationOptions registration = relyingParty.startRegistration(registrationOptions);
            session.setAttribute(userIdentity.getName(), registration);
            try {
                return registration.toCredentialsCreateJson();
            } catch (JsonProcessingException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing JSON.", e);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User " + appUser.getUsername() + " does not exist. Please register.");
        }
    }

    public Authenticator newAuth(String credential, String username, HttpSession session) {
        try {
            AppUser user = userRepo.findByUsername(username);
            PublicKeyCredentialCreationOptions requestOptions = (PublicKeyCredentialCreationOptions) session.getAttribute(user.getUsername());
            if (requestOptions != null) {
                var pkc = PublicKeyCredential.parseRegistrationResponseJson(credential);
                FinishRegistrationOptions options = FinishRegistrationOptions.builder()
                        .request(requestOptions)
                        .response(pkc)
                        .build();
                RegistrationResult result = relyingParty.finishRegistration(options);
                Authenticator savedAuth = new Authenticator(result, pkc.getResponse(), user, username);
                return authRepository.save(savedAuth);
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cached request expired. Try to register again!");
            }
        } catch (RegistrationFailedException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Registration failed.", e);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to save credenital, please try again!", e);
        }
    }

}
