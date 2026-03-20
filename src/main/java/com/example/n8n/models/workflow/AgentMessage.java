package com.example.n8n.models.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AgentMessage 
{
    private String role;
    private String content;
    private String toolCallId;
    private String toolName;
    
}
