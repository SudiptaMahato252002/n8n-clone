package com.example.n8n.models.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.n8n.enums.AgentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="agent_sessions")
public class AgentSesssion 
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name="execution_id",nullable = false)
    private String executionId;
    @Column(name="node_id",nullable = false)
    private String nodeId;
    @Column(name="workflow_id",nullable = false)
    private String workflowId;
    
    @Builder.Default
    @Column(nullable = false)
    private AgentStatus status=AgentStatus.RUNNING;
   
    @Builder.Default
    @Column(name = "total_steps")
    private int totalSteps=0;

    @Builder.Default
    @Column(name = "max_steps")
    private int maxSteps=10;

    @Column(name = "final_output", columnDefinition = "TEXT")
    private String finalOutput;
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
     @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime updatedAt;
}
