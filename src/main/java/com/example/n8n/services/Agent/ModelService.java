package com.example.n8n.services.Agent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.n8n.models.workflow.AgentMessage;
import com.example.n8n.models.workflow.ToolCall;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModelService 
{
    private final ObjectMapper objectMapper;
    private static final int TIMEOUT_SECONDS=60;

    public ModelResponse chat(String modelString,String apiKey,List<AgentMessage> messages,List<Map<String,Object>> toolSchemas)throws Exception
    {
        String[] parts=resolveProvider(modelString);
        String provider=parts[0];
        String modelName=parts[1];

        return switch(provider)
        {
            case "openai" -> callOpenAICompatible(
                    "https://api.openai.com/v1/chat/completions",
                    apiKey, modelName, messages, toolSchemas);
            case "groq"   -> callOpenAICompatible(
                    "https://api.groq.com/openai/v1/chat/completions",
                    apiKey, modelName, messages, toolSchemas);
            case "gemini" -> callOpenAICompatible(
                    "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions",
                    apiKey, modelName, messages, toolSchemas);
            default -> throw new IllegalArgumentException("Unknown model provider: " + provider);
        };

    }

    private ModelResponse callOpenAICompatible(String url,String apiKey,String model,List<AgentMessage> messages,List<Map<String, Object>> toolSchemas)throws Exception
    {
        Map<String,Object> body=new HashMap<>();
        body.put("model", model);
        body.put("messages",buildMessageList(messages));
        body.put("temperature", 0.7);
        body.put("max_tokens", 1024);
        
        String requestjson=objectMapper.writeValueAsString(body);
        HttpClient client=HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                        .build();
        HttpRequest request=HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Beare "+apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(requestjson))
                        .build();
        HttpResponse<String> response=client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if(response.statusCode()!=200)
        {
            throw new RuntimeException("Model API error HTTP " + response.statusCode() + ": " + response.body());
        }
        return parseResponse(response.body());
    }

    @SuppressWarnings("unchecked")
    private ModelResponse parseResponse(String responseBody) throws Exception
    {
        Map<String,Object> json=objectMapper.readValue(responseBody,Map.class);
        List<Map<String,Object>> choices=(List<Map<String,Object>>) json.get("choices");
        if (choices == null || choices.isEmpty()) 
        {
            throw new RuntimeException("Model returned no choices");
        }

        Map<String,Object> message=(Map<String,Object>)choices.get(0).get("message");
        String content = (String) message.getOrDefault("content", "");
        String finishReason = (String) choices.get(0).getOrDefault("finish_reason", "stop");
        List<ToolCall> toolCalls = new ArrayList<>();

        List<Map<String,Object>> rawToolCalls=(List<Map<String,Object>>)message.get("tool_calls");
        if(rawToolCalls!=null)
        {
            for(Map<String,Object> tc:rawToolCalls)
            {
                Map<String,Object> fn=(Map<String, Object>) tc.get("function");
                String args=(String)fn.get("");
                Map<String,Object> argsJson=objectMapper.readValue(args,Map.class);
            
                toolCalls.add(ToolCall.builder().id((String)tc.get("id")).toolName((String)fn.get("name")).input(argsJson).build());

            
            }
        }

        return ModelResponse.builder()
                .content(content)
                .toolCalls(toolCalls)
                .finishReason(finishReason)
                .wantsToolCall(!toolCalls.isEmpty())
                .build();
    }


    private List<Map<String,Object>> buildMessageList(List<AgentMessage> messages)
    {
        return messages.stream().map(m->{
            Map<String,Object> msg=new HashMap<>();
            msg.put("role",m.getRole());
            msg.put("content",m.getContent()!=null?m.getContent():"");
            if(m.getToolCallId()!=null) msg.put("tool_call_id", m.getToolCallId());
            if(m.getToolName()!=null) msg.put("name",m.getToolName());
            return msg;
        }).toList();
    }

    private String[] resolveProvider(String modelString)
    {
        if (modelString == null || modelString.isBlank()) 
        {
            return new String[]{"groq", "llama-3.3-70b-versatile"};
        }
        return new String[]{"groq",modelString.trim()};
    }
    
    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelResponse
    {
        private String content;
        private List<ToolCall> toolCalls;
        private String finishReason;
        private boolean wantsToolCall;
    }
}
