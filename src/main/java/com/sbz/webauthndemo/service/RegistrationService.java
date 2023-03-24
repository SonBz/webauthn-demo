package com.sbz.webauthndemo.service;

import com.sbz.webauthndemo.model.AppUser;
import com.sbz.webauthndemo.model.Authenticator;
import com.sbz.webauthndemo.repository.AuthenticatorRepository;
import com.sbz.webauthndemo.repository.UserRepository;
import com.sbz.webauthndemo.utility.BytesUtil;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Getter
public class RegistrationService implements CredentialRepository {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private AuthenticatorRepository authRepository;

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        return authRepository.findAllByUserUsername(username).stream()
                .map(credential -> PublicKeyCredentialDescriptor.builder()
                        .id(new ByteArray(credential.getCredentialId()))
                        .build())
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        log.debug("getUserHandleForUsername - username: {}", username);
        return userRepo.findByUsername(username)
                .map(appUser -> new ByteArray(BytesUtil.longToBytes(appUser.getId())));
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        AppUser user = userRepo.findByHandle(userHandle.getBytes());
        return Optional.of(user.getUsername());
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        log.debug("lookup - credentialId: {}", credentialId);
        return authRepository.findByCredentialId(credentialId.getBytes())
                .map(credential -> RegisteredCredential.builder()
                        .credentialId(new ByteArray(credential.getCredentialId()))
                        .userHandle(new ByteArray(credential.getUser().getHandle()))
                        .publicKeyCose(new ByteArray(credential.getPublicKey()))
                        .signatureCount(credential.getCount())
                        .build()
                );
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return authRepository.findAllByCredentialId(credentialId.getBytes()).stream()
                .map(credential ->RegisteredCredential.builder()
                        .credentialId(new ByteArray(credential.getCredentialId()))
                        .userHandle(new ByteArray(credential.getUser().getHandle()))
                        .publicKeyCose(new ByteArray(credential.getPublicKey()))
                        .signatureCount(credential.getCount())
                        .build())
                .collect(Collectors.toSet());
    }
}
