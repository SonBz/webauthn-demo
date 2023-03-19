package com.sbz.webauthndemo.model;


import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.data.AttestedCredentialData;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ByteArray;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Entity
@Getter
@NoArgsConstructor
public class Authenticator {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String name;

    @Lob
    @Column(nullable = false)
    private byte[] credentialId;

    @Lob
    @Column(nullable = false)
    private byte[] publicKey;

    @Column(nullable = false)
    private Long count;

    @Lob
    @Column(nullable = true)
    private byte[] aaguid;

    @ManyToOne
    private AppUser user;

    public Authenticator(RegistrationResult result, AuthenticatorAttestationResponse response, AppUser user, String name) {
        Optional<AttestedCredentialData> attestationData = response.getAttestation().getAuthenticatorData().getAttestedCredentialData();
        this.credentialId = result.getKeyId().getId().getBytes();
        this.publicKey = result.getPublicKeyCose().getBytes();
        this.aaguid = attestationData.get().getAaguid().getBytes();
        this.count = result.getSignatureCount();
        this.name = name;
        this.user = user;
    }
}
