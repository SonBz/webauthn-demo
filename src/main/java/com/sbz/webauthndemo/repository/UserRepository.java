package com.sbz.webauthndemo.repository;

import com.sbz.webauthndemo.model.AppUser;
import com.yubico.webauthn.data.ByteArray;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    AppUser findByUsername(String name);
    AppUser findByHandle(ByteArray handle);
}
