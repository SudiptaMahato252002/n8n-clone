package com.example.n8n.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.n8n.models.workflow.ExecutionContext;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ExecutionContextService 
{

    public ExecutionContext buildContext(Map<String,Object> triggerPayload)
    {
        ExecutionContext context=ExecutionContext.builder().build();
        context.setTriggerPayload(triggerPayload!=null?triggerPayload:new HashMap<>());
        return context;
    }

    public void addNodeResult(ExecutionContext context,String nodeId,Object result)
    {
        log.debug("Adding node result to context: {}", nodeId);
        context.addNodeResult(nodeId, result);  
    }
    
    public Map<String,Object> toMap(ExecutionContext context)
    {
        return context.toMap();
    }
}
