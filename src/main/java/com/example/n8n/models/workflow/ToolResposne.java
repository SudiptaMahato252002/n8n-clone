package com.example.n8n.models.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolResposne 
{
    private String toolCallId;
    private String toolName;
    private Object output;
    private boolean success;
    private String errorMessage;
    
}
