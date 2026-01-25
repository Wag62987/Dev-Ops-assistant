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

 private final CustomSuccesHandler succesHandler;

    @Bean
    public SecurityFilterChain FilterChain(HttpSecurity http){
        http
         .csrf(csrf -> csrf
            .ignoringRequestMatchers("/repos/**") // 👈 important change it after testing
        )
                .authorizeHttpRequests(authorize->
                        authorize
                          .requestMatchers("/repos/import").permitAll()
                                .anyRequest().authenticated()
                        )

                .oauth2Login(oauth->
                        oauth
                        .successHandler(succesHandler))
                .oauth2Client(Customizer.withDefaults());

        return http.build();
    }
    
}
