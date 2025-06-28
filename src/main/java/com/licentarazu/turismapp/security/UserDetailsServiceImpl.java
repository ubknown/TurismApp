package com.licentarazu.turismapp.security;

import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.info("🔍 Loading user by email: {}", email);
        logger.info("🔍 Email length: {}, trimmed: '{}'", email.length(), email.trim());
        
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            logger.error("❌ User not found in database for email: '{}'", email);
            logger.error("❌ Checking for case sensitivity or whitespace issues...");
            // Try to find user with case-insensitive search
            logger.error("❌ Throwing UsernameNotFoundException");
            throw new UsernameNotFoundException("User not found: " + email);
        }
        
        logger.info("✅ User found - ID: {}, Email: '{}', Role: {}, Enabled: {}", 
                   user.getId(), user.getEmail(), user.getRole(), user.getEnabled());
        logger.info("🔑 Password hash from DB: {}", user.getPassword());
        logger.info("🔑 Password hash length: {}", user.getPassword().length());
        
        if (!user.getEnabled()) {
            logger.warn("⚠️ User account is disabled: {}", email);
            throw new org.springframework.security.authentication.DisabledException("User account is disabled");
        }

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .disabled(!user.getEnabled())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
                
        logger.info("🔐 UserDetails created - Username: '{}', Authorities: {}, Enabled: {}", 
                   userDetails.getUsername(), userDetails.getAuthorities(), userDetails.isEnabled());
        logger.info("🔐 Account checks - NonExpired: {}, NonLocked: {}, CredentialsNonExpired: {}", 
                   userDetails.isAccountNonExpired(), userDetails.isAccountNonLocked(), userDetails.isCredentialsNonExpired());
        
        return userDetails;
    }
}
