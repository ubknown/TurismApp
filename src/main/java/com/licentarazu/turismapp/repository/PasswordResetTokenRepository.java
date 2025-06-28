package com.licentarazu.turismapp.repository;

import com.licentarazu.turismapp.model.PasswordResetToken;
import com.licentarazu.turismapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    List<PasswordResetToken> findByUserEmail(String email);
    
    List<PasswordResetToken> findByUser(User user);
    
    @Query("SELECT p FROM PasswordResetToken p WHERE p.user = :user AND p.usedAt IS NULL AND p.expiresAt > :now")
    List<PasswordResetToken> findValidTokensByUser(User user, LocalDateTime now);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);
    
    @Modifying
    @Transactional
    @Query("UPDATE PasswordResetToken p SET p.usedAt = :usedAt WHERE p.user = :user AND p.usedAt IS NULL")
    void invalidateUserTokens(User user, LocalDateTime usedAt);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken p WHERE p.user = :user")
    void deleteByUser(User user);
}
