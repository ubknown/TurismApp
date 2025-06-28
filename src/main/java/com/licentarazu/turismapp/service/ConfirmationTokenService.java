package com.licentarazu.turismapp.service;

import com.licentarazu.turismapp.model.ConfirmationToken;
import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.repository.ConfirmationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Service
public class ConfirmationTokenService {

    @Autowired
    private ConfirmationTokenRepository confirmationTokenRepository;

    public ConfirmationToken createConfirmationToken(User user) {
        // Delete any existing tokens for this user first
        deleteTokensForUser(user);
        
        ConfirmationToken token = new ConfirmationToken(user);
        return confirmationTokenRepository.save(token);
    }

    public Optional<ConfirmationToken> getToken(String token) {
        return confirmationTokenRepository.findByToken(token);
    }

    public ConfirmationToken confirmToken(ConfirmationToken token) {
        token.setConfirmedAt(LocalDateTime.now());
        return confirmationTokenRepository.save(token);
    }

    public void deleteExpiredTokens() {
        confirmationTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
    
    public void deleteTokensForUser(User user) {
        confirmationTokenRepository.deleteByUser(user);
    }
    
    public Optional<ConfirmationToken> getLatestTokenForUser(User user) {
        return confirmationTokenRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .findFirst();
    }
}
