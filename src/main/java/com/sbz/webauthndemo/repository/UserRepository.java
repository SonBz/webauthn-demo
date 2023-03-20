package com.sbz.webauthndemo.repository;

import com.sbz.webauthndemo.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    AppUser findByUsername(String name);
    AppUser findByHandle(byte[] handle);
}
