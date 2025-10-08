package io.github.vivianagh.jobberwocky.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;


@Configuration
@EnableConfigurationProperties(ExternalJobsProps.class)
public class RestClientConfig {

    @Bean("externalJobsRestClient")
    public RestClient externalJobsRestClient(ExternalJobsProps p) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(p.getConnectTimeoutMs());
        factory.setReadTimeout(p.getReadTimeoutMs());

        return RestClient.builder()
                .baseUrl(p.getBaseUrl())
                .requestFactory(factory)
                .build();
    }
}

@ConfigurationProperties(prefix = "external-jobs")
class ExternalJobsProps {
    private String baseUrl;
    private int connectTimeoutMs = 2000;
    private int readTimeoutMs = 3000;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
}
