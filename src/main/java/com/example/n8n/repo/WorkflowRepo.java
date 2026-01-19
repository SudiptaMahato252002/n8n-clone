package com.example.n8n.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.n8n.models.entity.Workflow;

@Repository
public interface WorkflowRepo extends JpaRepository<Workflow,String>
{
    List<Workflow> findByUserId(String userId);
    
    Optional<Workflow> findByIdAndUserId(String id, String userId);
    
    Optional<Workflow> findByWebhookId(String webhookId);
    
}
