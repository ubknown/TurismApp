package com.licentarazu.turismapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Dezactivează CSRF pentru testare
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/**").permitAll() // permite accesul la user endpoints
                        .anyRequest().permitAll() // permite tot
                )
                .httpBasic(Customizer.withDefaults()); // Poți scoate dacă nu vrei deloc login HTTP

        return http.build();
    }
}
