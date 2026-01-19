package com.example.n8n.models.dtos.request;

import java.util.Map;

import com.example.n8n.enums.TriggerType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkflowRequest 
{
    private String userId;
    private String title;
    private TriggerType triggerType;
    private Map<String,Object> nodes;
    private Map<String,Object> connections;
    private WebhookConfigRequest webhook;
    
}
