package com.example.n8n.models.dtos.outbox;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExceutionEventPayload 
{
    private String executionId;
    private String workflowId;
    private Map<String,Object> triggerPayload;
    
}
