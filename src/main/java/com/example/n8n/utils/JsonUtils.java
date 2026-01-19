package com.example.n8n.utils;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonUtils 
{
    private final ObjectMapper objectMapper;
    
    public String ObjectToJson(Object obj)
    {
        try 
        {
            return objectMapper.writeValueAsString(obj);
            
        } 
        catch (JsonProcessingException e) 
        {
            log.error("Error converting object to JSON", e);
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }


    public <T> T jsonToObject(String json,Class<T> clazz)
    {
        try
        {
            return objectMapper.readValue(json, clazz);
        }
        catch(JsonProcessingException e)
        {
            log.error("Error converting JSON to object", e);
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }

    public Map<String,Object> jsonToMap(String json)
    {
        try 
        {
            return objectMapper.readValue(json, Map.class);   
        } 
        catch (JsonProcessingException e) 
        {
            log.error("Error converting JSON to Map", e);
            throw new RuntimeException("Failed to convert JSON to Map", e);
        }
    }
}
