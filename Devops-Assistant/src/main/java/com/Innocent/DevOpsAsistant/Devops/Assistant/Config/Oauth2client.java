package com.Innocent.DevOpsAsistant.Devops.Assistant.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

public class Oauth2client {
    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
            ClientRegistrationRepository clientRegistrationRepository) {
        // In-memory storage of tokens
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }
}
