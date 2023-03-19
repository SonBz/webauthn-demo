package com.sbz.webauthndemo.repository;

import com.sbz.webauthndemo.model.AppUser;
import com.sbz.webauthndemo.model.Authenticator;
import com.yubico.webauthn.data.ByteArray;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthenticatorRepository extends JpaRepository<Authenticator, Long> {
    Optional<Authenticator> findByCredentialId(ByteArray credentialId);
    List<Authenticator> findAllByUser (AppUser user);
    List<Authenticator> findAllByCredentialId(ByteArray credentialId);
}
