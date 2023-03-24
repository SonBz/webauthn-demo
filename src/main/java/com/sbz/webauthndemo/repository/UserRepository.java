package com.sbz.webauthndemo.repository;

import com.sbz.webauthndemo.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String name);
    AppUser findByHandle(byte[] handle);
}
