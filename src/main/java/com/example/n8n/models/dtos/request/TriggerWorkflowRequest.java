package com.example.n8n.models.dtos.request;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerWorkflowRequest 
{
    private Map<String,Object> input;
    
}
