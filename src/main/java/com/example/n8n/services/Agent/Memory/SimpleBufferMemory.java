package com.example.n8n.services.Agent.Memory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.example.n8n.models.workflow.AgentMessage;
import com.example.n8n.utils.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class SimpleBufferMemory implements ChatMemory
{
    private final RedisTemplate<String,Object> redisTemplate;
    private final JsonUtils jsonUtils;

    private static final String KEY_PREFIX = "agent:memory:simple:";
    private static final long   TTL_HOURS  = 24;

    @Override
    public void addMessage(String sessionKey, AgentMessage message) 
    {
        String key=key(sessionKey);
        List<AgentMessage> history=getMessages(sessionKey);
        history.add(message);
        persist(key,history);
        log.debug("[SIMPLE_BUFFER] sessionKey={} role={} total={}", sessionKey, message.getRole(), history.size());
    }

    @Override
    public List<AgentMessage> getMessages(String sessionKey) 
    {
        try 
        {
            String key=key(sessionKey);
            Object rawMessage=redisTemplate.opsForValue().get(key);
            if(rawMessage==null)return new ArrayList<>();
            String json=rawMessage instanceof String s?s:jsonUtils.ObjectToJson(rawMessage);
            return jsonUtils.jsonToObject(json, new TypeReference<List<AgentMessage>>() {});    
        } 
        catch (Exception e) 
        {
            log.warn("[SIMPLE_BUFFER] Failed to load for sessionKey={}: {}", sessionKey, e.getMessage());
            return new ArrayList<>();
            // TODO: handle exception
        }
        
    }

    @Override
    @SuppressWarnings("unchecked")
    public void clear(String sessionKey) 
    {
        redisTemplate.delete(key(sessionKey));
        log.info("[SIMPLE_BUFFER] Cleared sessionKey={}", sessionKey);
        
    }

    @Override
    public int getMessageCount(String sessionKey) 
    {
        return getMessages(key(sessionKey)).size();
        
    }

    @SuppressWarnings("unchecked")
    private void persist(String key,List<AgentMessage> messages)
    {
        redisTemplate.opsForValue().set(key,jsonUtils.ObjectToJson(messages),TTL_HOURS,TimeUnit.HOURS);
    }
    private String key(String sessionKey)
    {
        return KEY_PREFIX+sessionKey;
    }
}
