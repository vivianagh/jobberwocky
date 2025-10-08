package io.github.vivianagh.jobberwocky.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter @Setter
@ConfigurationProperties(prefix = "external")
public class ExternalSourceProperties {
    private boolean enabled = false;
    private String baseUrl;
    private int connectTimeoutMs = 2000;
    private int readTimeoutMs = 3000;
}
