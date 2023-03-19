package com.sbz.webauthndemo.config;

import com.sbz.webauthndemo.service.RegistrationService;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RelyingPartyConfig {

    @Bean
    @Autowired
    public RelyingParty relyingParty(RegistrationService regisrationRepository, WebAuthProperties properties) {
        RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
                .id(properties.getHostName())
                .name(properties.getDisplay())
                .build();

        return RelyingParty.builder()
                .identity(rpIdentity)
                .credentialRepository(regisrationRepository)
                .origins(properties.getOrigin())
                .build();
    }
}
