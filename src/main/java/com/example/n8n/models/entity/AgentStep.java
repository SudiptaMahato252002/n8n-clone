package com.example.n8n.models.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
@Table(name="agent_steps")
public class AgentStep 
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;
    
    @Column(name = "step_number", nullable = false)
    private int stepNumber;

    @Column(columnDefinition = "TEXT")
    private String thought;

    @Column(name = "tool_name")
    private String toolName;

    @Column(name = "tool_input", columnDefinition = "TEXT")
    private String toolInput;
    
    @Column(name = "tool_output", columnDefinition = "TEXT")
    private String toolOutput;
    
    @Column(name = "tool_success")
    @Builder.Default
    private boolean toolSuccess=true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
