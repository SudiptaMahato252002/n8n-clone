package com.example.n8n.models.workflow;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionContext 
{
    @Builder.Default
    private Map<String,Object> json=new HashMap<>();
    @Builder.Default
    private Map<String,Object> node=new HashMap<>();

    public void setTriggerPayload(Map<String,Object> payload)
    {
        Map<String,Object> bodyMap=new HashMap<>();
        bodyMap.put("body", payload);
        this.json=bodyMap;
    }
    
    public void addNodeResult(String nodeId,Object result)
    {
        this.node.put(nodeId,result);

    }
    
    public Map<String,Object> getTriggerPayload()
    {
        return (Map<String,Object>)this.json.get("body");
    }
    
    public Object getNodeResult(String nodeId)
    {
        return this.node.get(nodeId);
    }
    
    public Map<String,Object> toMap()
    {
        Map<String,Object> contextMap=new HashMap<>();
        contextMap.put("$json", this.json);
        contextMap.put("$node", contextMap);
        return contextMap;
    }
}


