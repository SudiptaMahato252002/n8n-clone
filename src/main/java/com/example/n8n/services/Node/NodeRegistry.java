package com.example.n8n.services.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.n8n.enums.Platform;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NodeRegistry 
{
    private final Map<Platform,NodeExecutor> executors=new HashMap<>();
    private final List<NodeExecutor> nodeExecutors;

    public NodeRegistry(List<NodeExecutor> nodeExecutors) 
    {
        this.nodeExecutors = nodeExecutors;
    }
    
    @PostConstruct
    public void registerExceutors()
    {
        log.info("Registering node executors...");
        for(NodeExecutor executor:nodeExecutors)
        {
            Platform platform=executor.getSuportedPlatform();
            executors.put(platform,executor);
            log.info("Registered executor for platform: {}", platform);
            log.info("Total executors registered: {}", executors.size());
        }
        
    }

    public NodeExecutor getExecutor(Platform platform)
    {
        NodeExecutor executor=executors.get(platform);
        if(executor==null)
        {
            throw new IllegalArgumentException("No executor found for platform: " + platform);
        }
        return executor;
        
    }

    public boolean hasExecutor(Platform platform)
    {
        return executors.containsKey(platform);
    }
}
