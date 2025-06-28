package com.licentarazu.turismapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.licentarazu.turismapp.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
