package com.innowise.auth_service.jpa.repository;

import com.innowise.auth_service.jpa.entity.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredentialsRepository extends JpaRepository<Credentials, Long> {
    
    Optional<Credentials> findByLogin(String login);
    
    Optional<Credentials> findByUserId(Long userId);

    boolean existsByLogin(String login);

    boolean existsByUserId(Long userId);

    boolean deleteByUserId(Long userId);
}
