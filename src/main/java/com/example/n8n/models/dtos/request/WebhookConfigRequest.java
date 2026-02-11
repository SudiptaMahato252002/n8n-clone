package com.example.n8n.models.dtos.request;

import com.example.n8n.enums.HttpMethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookConfigRequest 
{
    private String id;
    private String title;
    private HttpMethod method;
    private String secret;
    
}
