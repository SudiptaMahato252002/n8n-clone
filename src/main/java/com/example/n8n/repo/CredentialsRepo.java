package com.example.n8n.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.n8n.enums.Platform;
import com.example.n8n.models.entity.Credentials;

@Repository
public interface CredentialsRepo extends JpaRepository<Credentials, String> 
{
    List<Credentials> findByUserId(String userId);
    
    Optional<Credentials> findByIdAndUserId(String id, String userId);
    
    List<Credentials> findByUserIdAndPlatform(String userId, Platform platform);
    
}
