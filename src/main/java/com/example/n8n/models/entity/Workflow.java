package com.example.n8n.models.entity;

import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.n8n.enums.TriggerType;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workflows")
public class Workflow 
{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name="user_id",nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private String title;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled=true;
    
    @Column(name="trigger_type",nullable = false)
    private TriggerType triggerType;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb",nullable = false)
    private Map<String,Object> nodes;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb",nullable = false)
    private Map<String,Object> connections;

    @Column(name = "webhook_id")
    private String webhookId;
    
    @ManyToOne
    @JoinColumn(name = "webhook_id",insertable = false,updatable = false)
    private Webhook webhook;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
}
