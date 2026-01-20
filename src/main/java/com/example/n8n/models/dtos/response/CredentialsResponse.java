package com.example.n8n.models.dtos.response;

import java.time.LocalDateTime;

import com.example.n8n.enums.Platform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CredentialsResponse 
{
    private String id;
    private String userId;
    private String title;
    private Platform platform;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
}
