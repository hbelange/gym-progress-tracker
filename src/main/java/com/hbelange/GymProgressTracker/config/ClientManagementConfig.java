package com.hbelange.GymProgressTracker.config;

import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

@Configuration
public class ClientManagementConfig {
    
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        
        RegisteredClient registeredClient = 
            RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId("client")
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://www.manning.com/authorized")
                .scope(OidcScopes.OPENID)
                .build();
        ;
        return new InMemoryRegisteredClientRepository(registeredClient);
    }
}
