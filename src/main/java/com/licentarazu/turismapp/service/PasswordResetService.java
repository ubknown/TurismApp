package com.licentarazu.turismapp.service;

import com.licentarazu.turismapp.model.PasswordResetToken;
import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Transactional
    public PasswordResetToken createPasswordResetToken(User user) {
        // Invalidate any existing tokens for this user
        passwordResetTokenRepository.invalidateUserTokens(user, LocalDateTime.now());
        
        // Create new token
        PasswordResetToken token = new PasswordResetToken(user);
        return passwordResetTokenRepository.save(token);
    }

    public Optional<PasswordResetToken> getPasswordResetToken(String token) {
        return passwordResetTokenRepository.findByToken(token);
    }

    @Transactional
    public boolean validateAndUseToken(String token) {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
        
        if (tokenOpt.isEmpty()) {
            return false;
        }
        
        PasswordResetToken resetToken = tokenOpt.get();
        
        if (!resetToken.isValid()) {
            return false;
        }
        
        // Mark token as used
        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);
        
        return true;
    }

    @Transactional
    public void deleteExpiredTokens() {
        passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
