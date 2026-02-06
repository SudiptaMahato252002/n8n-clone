package com.example.n8n.services.Execution;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.n8n.models.dtos.outbox.ExecutionEventPayload;
import com.example.n8n.utils.JsonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExecutionQueueService 
{
    private final RedisTemplate<String,Object> redisTemplate;
    private static final String STREAMING_KEY="workflow:executions";
    private final JsonUtils jsonUtils;

    public void publishToRedis(ExecutionEventPayload payload)
    {

        try 
        {
            Map<String,String> message=new HashMap<>();
            message.put("executionId", payload.getExecutionId());
            message.put("workflowId", payload.getWorkflowId());
            message.put("triggerPayload", jsonUtils.ObjectToJson(payload.getTriggerPayload()));
            StringRecord record=StreamRecords.string(message).withStreamKey(STREAMING_KEY);

            redisTemplate.opsForStream().add(record);
            log.info("Execution published to Redis successfully: {}", payload.getExecutionId());
                    
        } 
        catch (Exception e) 
        {
            log.error("Failed to publish execution to Redis: {}", payload.getExecutionId(), e);
            throw new RuntimeException("Failed to publish to Redis", e);
            
        }
        
    }

}
