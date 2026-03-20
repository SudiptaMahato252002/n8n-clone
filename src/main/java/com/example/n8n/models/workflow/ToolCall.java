package com.example.n8n.models.workflow;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCall 
{
    private String id;
    private String toolName;
    private Map<String,Object> input;
    
}
