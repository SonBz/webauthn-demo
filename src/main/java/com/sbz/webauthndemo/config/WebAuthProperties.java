package com.sbz.webauthndemo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "authn")
@Getter
@Setter
public class WebAuthProperties {

    private String hostName;
    private String display;
    private Set<String> origin;
}
