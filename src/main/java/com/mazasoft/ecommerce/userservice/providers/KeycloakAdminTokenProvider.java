package com.mazasoft.ecommerce.userservice.providers;

import com.mazasoft.ecommerce.userservice.client.KeycloakTokenClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

public class KeycloakAdminTokenProvider {
    private static final Logger log = LoggerFactory.getLogger(KeycloakAdminTokenProvider.class);

    private final KeycloakTokenClient adminTokenClient;
    private final ReentrantLock lock = new ReentrantLock();

    private volatile String cachedToken;
    private volatile Instant expiresAt = Instant.EPOCH;

    public KeycloakAdminTokenProvider(KeycloakTokenClient adminTokenClient) {
        this.adminTokenClient = adminTokenClient;
    }

    public String getBearerToken() {
        if (cachedToken != null && Instant.now().isBefore(expiresAt.minusSeconds(15))) {
            return cachedToken;
        }
        lock.lock();
        try {
            if (cachedToken != null && Instant.now().isBefore(expiresAt.minusSeconds(15))) {
                return cachedToken;
            }
            KeycloakTokenClient.AdminTokenResponse resp = adminTokenClient.clientCredentialsGrant();
            if (resp == null || resp.access_token() == null || resp.access_token().isBlank()) {
                throw new IllegalStateException("Keycloak admin token response is empty");
            }
            cachedToken = resp.access_token();
            expiresAt = Instant.now().plusSeconds(Math.max(30, resp.expires_in()));
            log.debug("Fetched new Keycloak admin token; expiresAt={}", expiresAt);
            return cachedToken;
        } finally {
            lock.unlock();
        }
    }
}
