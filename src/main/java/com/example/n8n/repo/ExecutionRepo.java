package com.example.n8n.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.n8n.models.entity.Execution;

@Repository
public interface ExecutionRepo extends JpaRepository<Execution,String>
{
    List<Execution> findByWorkflowId(String workflowId);
    
    List<Execution> findByWorkflowIdOrderByStartedAtDesc(String workflowId);    
}
