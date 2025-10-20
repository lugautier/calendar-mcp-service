package com.enterprise.calendar.config;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GraphConfig {

    private final GraphProperties properties;

    @Bean
    public ClientSecretCredential clientSecretCredential() {
        return new ClientSecretCredentialBuilder()
            .tenantId(properties.getTenantId())
            .clientId(properties.getClientId())
            .clientSecret(properties.getClientSecret())
            .build();
    }

    @Bean
    public GraphServiceClient graphServiceClient(ClientSecretCredential credential) {
        return new GraphServiceClient(credential, properties.getScope());
    }
}
