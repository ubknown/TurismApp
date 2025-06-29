package com.licentarazu.turismapp.repository;

import com.licentarazu.turismapp.model.OwnerApplication;
import com.licentarazu.turismapp.model.OwnerStatus;
import com.licentarazu.turismapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OwnerApplicationRepository extends JpaRepository<OwnerApplication, Long> {
    
    Optional<OwnerApplication> findByUser(User user);
    
    Optional<OwnerApplication> findByEmail(String email);
    
    boolean existsByUser(User user);
    
    boolean existsByEmail(String email);
    
    List<OwnerApplication> findByStatus(OwnerStatus status);
    
    List<OwnerApplication> findByStatusOrderBySubmittedAtDesc(OwnerStatus status);
    
    List<OwnerApplication> findAllByOrderBySubmittedAtDesc();
}
