package com.licentarazu.turismapp.repository;

import com.licentarazu.turismapp.model.ConfirmationToken;
import com.licentarazu.turismapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {
    
    Optional<ConfirmationToken> findByToken(String token);
    
    List<ConfirmationToken> findByUser(User user);
    
    List<ConfirmationToken> findByUserOrderByCreatedAtDesc(User user);
    
    List<ConfirmationToken> findByUserEmail(String email);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM ConfirmationToken c WHERE c.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM ConfirmationToken c WHERE c.user = :user")
    void deleteByUser(User user);
}
