package com.example.n8n.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.n8n.models.entity.Webhook;

@Repository
public interface WebhookRepo extends JpaRepository<Webhook, String> 
{
    Optional<Webhook> findById(String id);
    
}
