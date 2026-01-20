package com.example.n8n.models.dtos.response;

import java.time.LocalDateTime;

import com.example.n8n.enums.HttpMethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookResponse 
{
     private String id;
    private String title;
    private HttpMethod method;
    private String secret;
    private LocalDateTime createdAt;
    
}
