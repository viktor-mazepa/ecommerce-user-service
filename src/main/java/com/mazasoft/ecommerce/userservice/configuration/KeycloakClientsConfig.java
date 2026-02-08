package com.mazasoft.ecommerce.userservice.configuration;

import com.mazasoft.ecommerce.userservice.client.KeycloakAdminClient;
import com.mazasoft.ecommerce.userservice.client.KeycloakTokenClient;
import com.mazasoft.ecommerce.userservice.configuration.properties.KeycloakProperties;
import com.mazasoft.ecommerce.userservice.providers.KeycloakAdminTokenProvider;
import com.mazasoft.ecommerce.userservice.ports.IdentityAdminPort;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(KeycloakProperties.class)
public class KeycloakClientsConfig {
    @Bean
    RestClient keycloakRestClient(RestClient.Builder builder) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        //factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(10));
        return builder
                .requestFactory(factory)
                .build();
    }

    @Bean
    KeycloakTokenClient loginTokenClient(RestClient keycloakRestClient, KeycloakProperties properties) {
        return new KeycloakTokenClient(
                keycloakRestClient,
                properties.tokenUrl(),
                properties.loginClientId(),
                properties.loginClientSecret()
        );
    }

    @Bean
    KeycloakTokenClient adminTokenClient(RestClient keycloakRestClient, KeycloakProperties properties) {
        return new KeycloakTokenClient(
                keycloakRestClient,
                properties.tokenUrl(),
                properties.adminClientId(),
                properties.adminClientSecret()
        );
    }

    @Bean
    KeycloakAdminTokenProvider adminTokenProvider(KeycloakTokenClient adminTokenClient) {
        return new KeycloakAdminTokenProvider(adminTokenClient);
    }

    @Bean
    IdentityAdminPort keycloakAdminClient(RestClient keycloakRestClient, KeycloakAdminTokenProvider tokenProvider, KeycloakProperties properties) {
        return new KeycloakAdminClient(
                keycloakRestClient,
                tokenProvider,
                properties.adminBaseUrl()
        );
    }
}
