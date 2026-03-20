package com.example.n8n.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.n8n.models.entity.AgentStep;

@Repository
public interface AgentStepRepo extends JpaRepository<AgentStep,String> {
    
}
