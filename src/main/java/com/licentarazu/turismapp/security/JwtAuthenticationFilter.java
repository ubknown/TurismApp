package com.licentarazu.turismapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String authHeader = request.getHeader("Authorization");

        // ✅ SKIP JWT PROCESSING for public endpoints entirely
        if (isPublicEndpoint(requestURI)) {
            logger.info("🟢 JWT Filter - Skipping public endpoint: {} {}", method, requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // Log all booking-related requests for debugging
        if (requestURI.contains("/api/bookings")) {
            logger.info("🔵 JWT Filter - {} {}, Auth Header: {}", 
                       method, requestURI, authHeader != null ? "Present" : "Missing");
        }

        // ✅ CRITICAL FIX: Enhanced JWT validation with null checks
        if (authHeader != null && authHeader.startsWith("Bearer ") && authHeader.length() > 7) {
            String token = authHeader.substring(7);
            
            // ✅ Add null check for token
            if (token != null && !token.trim().isEmpty()) {
                String email = jwtUtil.extractUsername(token);

                if (requestURI.contains("/api/bookings")) {
                    logger.info("🔵 JWT Filter - Extracted email: {}", email);
                }

                // ✅ Enhanced validation: email not null, not empty, and no existing auth
                if (email != null && !email.trim().isEmpty() && 
                    SecurityContextHolder.getContext().getAuthentication() == null) {
                    
                    try {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                        
                        // ✅ Additional null check for userDetails and token validation
                        if (userDetails != null && jwtUtil.validateToken(token)) {
                            UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails,
                                            null,
                                            userDetails.getAuthorities()
                                    );

                            SecurityContextHolder.getContext().setAuthentication(authToken);
                            
                            if (requestURI.contains("/api/bookings")) {
                                logger.info("✅ JWT Filter - Authentication set for: {}", email);
                            }
                        } else {
                            if (requestURI.contains("/api/bookings")) {
                                logger.warn("❌ JWT Filter - Token validation failed for: {}", email);
                            }
                        }
                    } catch (RuntimeException e) {
                        // ✅ SECURITY: Log security attempt but don't expose details
                        if (requestURI.contains("/api/bookings")) {
                            logger.error("❌ JWT Filter - Exception during authentication: {}", e.getMessage());
                        }
                        SecurityContextHolder.clearContext();
                    }
                }
            }
        } else if (requestURI.contains("/api/bookings")) {
            logger.warn("❌ JWT Filter - No valid Bearer token for booking request");
        }

        filterChain.doFilter(request, response);
    }

    // ✅ Helper method to check if an endpoint should skip JWT processing
    private boolean isPublicEndpoint(String requestURI) {
        // List of public endpoints that should skip JWT entirely
        String[] publicPatterns = {
            "/api/auth/",
            "/api/admin/login",
            "/api/units/public",
            "/api/units/search",
            "/api/units/filter",
            "/api/units/available",
            "/api/units/proximity",
            "/api/units/advanced-filter",
            "/api/units/debug/",
            "/api/accommodation-units/public",
            "/api/accommodation-units/debug/",
            "/api/reviews/unit/",
            "/api/reviews/accommodation-unit/",
            "/uploads/",
            "/api/uploads/",
            "/actuator/health",
            "/health",
            "/swagger-ui/",
            "/v3/api-docs/"
        };
        
        for (String pattern : publicPatterns) {
            if (requestURI.startsWith(pattern) || requestURI.contains(pattern)) {
                return true;
            }
        }
        
        // Special case for /api/units/{id} and /api/units/{id}/photos patterns
        if (requestURI.matches("/api/units/\\d+") || 
            requestURI.matches("/api/units/\\d+/photos") ||
            requestURI.equals("/api/units")) {
            return true;
        }
        
        return false;
    }
}
