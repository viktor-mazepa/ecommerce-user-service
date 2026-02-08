package com.mazasoft.ecommerce.userservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

public class KeycloakTokenClient {
    private final RestClient restClient;
    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;

    public KeycloakTokenClient(RestClient restClient, String tokenUrl, String clientId, String clientSecret) {
        this.restClient = restClient;
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public AdminTokenResponse clientCredentialsGrant() {
        MultiValueMap<String, String> form = baseForm("client_credentials");
        return postForm(tokenUrl, form, AdminTokenResponse.class);
    }

    private MultiValueMap<String, String> baseForm(String grantType) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", grantType);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        return form;
    }

    private <T> T postForm(String url, MultiValueMap<String, String> form, Class<T> bodyType) {
        return restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(form)
                .retrieve()
                .body(bodyType);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AdminTokenResponse(String access_token, long expires_in, String token_type, String scope) {}


}
