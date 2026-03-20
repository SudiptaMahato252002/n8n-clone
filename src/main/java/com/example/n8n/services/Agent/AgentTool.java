package com.example.n8n.services.Agent;

import java.util.Map;

import com.example.n8n.models.workflow.ExecutionContext;

public interface AgentTool 
{
    String getName();
    String getDescription();
    Map<String,Object> getInputShema();
    Object run(Map<String,Object> input,ExecutionContext context) throws Exception;
}
