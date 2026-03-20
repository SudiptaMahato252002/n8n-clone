package com.example.n8n.services.Agent;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AgentToolRegistry 
{
    Map<String,AgentTool> tools=new HashMap<>();
    List<AgentTool> toolList;

    public AgentToolRegistry(List<AgentTool> toolList)
    {
        this.toolList=toolList;
    }

    public void registerTools()
    {
        log.info("Registering agent tools...");
        for(AgentTool tool:toolList)
        {
            tools.put(tool.getName(), tool);
            log.info("Registered agent tool: {}", tool.getName());
        }
        log.info("Total agent tools registered: {}", tools.size());
    }

    public AgentTool getTool(String name)
    {
        AgentTool tool = tools.get(name);
        if (tool == null) 
        {
            throw new IllegalArgumentException("No agent tool found with name: " + name);
        }
        return tool;
    }
    public boolean hasTool(String name)
    {
        return tools.containsKey(name);
    }
    public Collection<AgentTool> getAllTools()
    {
        return tools.values();
    }
    public List<AgentTool> toolsForNode(List<String> allowedNames)
    {
        return allowedNames.stream().filter(tools::containsKey).map(tools::get).toList();
    }

    public List<Map<String,Object>> buildToolSchema(List<AgentTool> nodeTools)
    {
        return nodeTools.stream().map(tool->{
            Map<String,Object> function=new HashMap<>();
            function.put("name", tool.getName());
            function.put("description",tool.getDescription());
            function.put("parameters",Map.of(
                "type","object",
                "properties",tool.getInputShema(),
                "required", tool.getInputShema().keySet().stream()
                    .filter(k->{
                        Object fieldDef=tool.getInputShema().get(k);
                        if(fieldDef instanceof Map<?,?>m)
                        {
                            return Boolean.TRUE.equals(m.get("required"));
                        }
                        return false;
                    }).toList()
                ));
                Map<String,Object> wrapper=new HashMap<>();
                wrapper.put("type", "function");
                wrapper.put("function",function);
                return wrapper;
            
        }).toList();
    }
}
