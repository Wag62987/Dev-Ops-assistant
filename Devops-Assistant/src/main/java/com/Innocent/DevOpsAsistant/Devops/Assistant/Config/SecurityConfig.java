package com.Innocent.DevOpsAsistant.Devops.Assistant.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomSuccessHandler successHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // JWT APIs are stateless
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                // OAuth endpoints
                .requestMatchers(
                    "/oauth2/**",
                    "/login/**"
                ).permitAll()

                // JWT protected APIs
                .requestMatchers("/repos/**","/deploy/**","/ci-status/**","/github/**").authenticated()

                .anyRequest().authenticated()
            )

            // OAuth login (browser flow)
            .oauth2Login(oauth -> oauth
                .successHandler(successHandler)
            )

            // JWT validation (API flow)
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(Customizer.withDefaults())
            );

        return http.build();
    }
}
