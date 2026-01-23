package com.example.n8n.models.workflow;

import java.util.Map;

import com.example.n8n.enums.Platform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowNode 
{

    private String id;
    private String label;
    private String credentialsId;
    private Platform type;
    private Position Position;
    private Map<String,String> config;

    @Data
    @Builder
    public static class Position
    {
        private int x;
        private int y;
    }
    
}
