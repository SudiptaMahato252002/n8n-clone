package com.example.n8n.models.dtos.response;

import java.time.LocalDateTime;
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
public class WorkflowResponse {
    
    private String id;
    private String userId;
    private String title;
    private Boolean enabled;
    private TriggerType triggerType;
    private Map<String, Object> nodes;
    private Map<String, Object> connections;
    private String webhookId;
    private WebhookResponse webhook;
    private Long executionCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}