package com.example.n8n.models.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name ="refresh_tokens", indexes = {
    @Index(name="idx_token",columnList = "token",unique = true),
    @Index(name="idx_user_id",columnList="user_id")
} )
public class RefreshToken 
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "revoked", nullable = false)
    @Builder.Default
    private Boolean revoked=false;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;
    public boolean isExpired()
    {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    public boolean isValid()
    {
        return !revoked && !isExpired();
    }
}
