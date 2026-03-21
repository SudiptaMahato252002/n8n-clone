package com.example.n8n.services.Agent.Memory;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.n8n.enums.MemoryStrategy;
import com.example.n8n.models.workflow.AgentMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentMemoryServcie 
{
    private final SimpleBufferMemory simpleBufferMemory;

    public void addMessage(String sessionKey, AgentMessage message, MemoryStrategy strategy, int windowSize)
    {
        log.debug("[MEMORY] addMessage strategy={} sessionKey={} role={}", strategy, sessionKey, message.getRole());
        switch (strategy) 
        {
            case SIMPLE_BUFFER->simpleBufferMemory.addMessage(sessionKey, message);     
            default->simpleBufferMemory.addMessage(sessionKey, message);
        }
    }

    public List<AgentMessage> getMessages(String sessionKey,MemoryStrategy strategy)
    {
        List<AgentMessage> messages =switch (strategy) {
            case SIMPLE_BUFFER->simpleBufferMemory.getMessages(sessionKey);
            default->simpleBufferMemory.getMessages(sessionKey);
        };
        log.debug("[MEMORY] getMessages strategy={} sessionKey={} count={}", strategy, sessionKey, messages.size());
        return messages;
    }

    public void clear(String sessionKey,MemoryStrategy strategy)
    {
        switch (strategy) 
        {
            case SIMPLE_BUFFER->simpleBufferMemory.clear(sessionKey);
            default->simpleBufferMemory.clear(sessionKey);        
        }
    }

    public int getMessageCount(String sessionKey, MemoryStrategy strategy) 
    {
        return switch (strategy) 
        {
            case SIMPLE_BUFFER -> simpleBufferMemory.getMessageCount(sessionKey);
            default->simpleBufferMemory.getMessageCount(sessionKey);
        };
    }
    
}
