package com.licentarazu.turismapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // ✅ CRITICAL FIX: Enhanced JWT validation with null checks
        if (authHeader != null && authHeader.startsWith("Bearer ") && authHeader.length() > 7) {
            String token = authHeader.substring(7);
            
            // ✅ Add null check for token
            if (token != null && !token.trim().isEmpty()) {
                String email = jwtUtil.extractUsername(token);

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
                        }
                    } catch (RuntimeException e) {
                        // ✅ SECURITY: Log security attempt but don't expose details
                        // TODO: Add proper security logging for monitoring
                        SecurityContextHolder.clearContext();
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
