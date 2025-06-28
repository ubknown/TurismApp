package com.licentarazu.turismapp.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.licentarazu.turismapp.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for API endpoints (since we're using JWT)
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configure session management (stateless for REST API)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configure authorization rules
            .authorizeHttpRequests(authz -> authz
                // ✅ AUTH ENDPOINTS - Must be FIRST for highest priority
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers("/api/auth/forgot-password").permitAll()
                .requestMatchers("/api/auth/reset-password").permitAll()
                .requestMatchers("/api/auth/confirm").permitAll()
                .requestMatchers("/api/auth/resend-confirmation").permitAll()
                .requestMatchers("/api/auth/test-email-config").permitAll()
                .requestMatchers("/api/auth/**").permitAll()  // Catch-all for any other auth endpoints
                
                // Public endpoints - no authentication required
                .requestMatchers("/api/units/public/**").permitAll()
                .requestMatchers("/api/units/public").permitAll()
                .requestMatchers("/api/units").permitAll()
                .requestMatchers("/api/units/{id}").permitAll()
                .requestMatchers("/api/units/search").permitAll()
                .requestMatchers("/api/units/filter").permitAll()
                .requestMatchers("/api/units/available").permitAll()
                .requestMatchers("/api/units/proximity").permitAll()
                .requestMatchers("/api/units/advanced-filter").permitAll()
                .requestMatchers("/api/units/{id}/photos").permitAll()
                .requestMatchers("/api/accommodation-units/public/**").permitAll()
                .requestMatchers("/api/accommodation-units/public").permitAll()
                
                // Units browsing and details (public access)
                .requestMatchers("/api/units").permitAll()
                .requestMatchers("/api/units/{id}").permitAll()
                .requestMatchers("/api/units/search").permitAll()
                .requestMatchers("/api/units/filter").permitAll()
                .requestMatchers("/api/units/available").permitAll()
                .requestMatchers("/api/units/proximity").permitAll()
                .requestMatchers("/api/units/advanced-filter").permitAll()
                
                // Reviews endpoints (public read access)
                .requestMatchers("/api/reviews/unit/**").permitAll()
                .requestMatchers("/api/reviews/accommodation-unit/**").permitAll()
                
                // File uploads (public for viewing)
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/api/uploads/**").permitAll()
                
                // Health check and actuator endpoints
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/health").permitAll()
                
                // Swagger UI (if enabled)
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                
                // ✅ CRITICAL FIX: Debug endpoints now require authentication (removed public access)
                // TODO: Consider adding @Profile("dev") to debug endpoints for additional security
                .requestMatchers("/api/units/debug/**").authenticated()
                .requestMatchers("/api/accommodation-units/debug/**").authenticated()
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            
            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins (frontend URLs) - NO WILDCARDS when allowCredentials = true
        configuration.setAllowedOrigins(List.of(
            "http://localhost:5173",    // Vite dev server (primary)
            "http://localhost:5174",    // Vite dev server (alternative port)
            "http://localhost:3000",    // React dev server alternative
            "http://127.0.0.1:5173",    // Alternative localhost
            "http://127.0.0.1:5174",    // Alternative localhost
            "http://127.0.0.1:3000"     // Alternative localhost
        ));
        
        // Allow all necessary HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        ));
        
        // Allow specific headers (avoid "*" when allowCredentials = true)
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type", 
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "X-Requested-With",
            "Cache-Control"
        ));
        
        // Expose headers that the frontend can access
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        
        // Allow credentials (important for JWT tokens and cookies)
        configuration.setAllowCredentials(true);
        
        // Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);
        
        // Apply CORS configuration to all API endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
