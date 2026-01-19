package com.example.n8n.models.entity;

import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.n8n.enums.ExecutionStatus;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "executions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Execution 
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "workflow_id", nullable = false)
    private String workflowId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", insertable = false, updatable = false)
    private Workflow workflow;
    
    @Column(nullable = false)
    @Builder.Default
    private ExecutionStatus status=ExecutionStatus.PENDING;
    
    @Column(name = "total_tasks", nullable = false)
    private int totalTasks;

    @Column(name = "tasks_done")
    @Builder.Default
    private int tasksDone=0;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb",nullable = false)
    private Map<String,Object> outputs;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb",nullable = false)
    private Map<String,Object> logs;
    
    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @UpdateTimestamp
    @Column(name = "completed_at")    
    private LocalDateTime completedAt;

    
}
