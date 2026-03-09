package com.example.n8n.services.Node.Executors;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.n8n.enums.Platform;
import com.example.n8n.exceptions.NodeExecutionException;
import com.example.n8n.models.workflow.ExecutionContext;
import com.example.n8n.models.workflow.WorkflowNode;
import com.example.n8n.services.Credentials.CredentialService;
import com.example.n8n.services.Node.NodeExecutor;
import com.example.n8n.utils.TemplateResolver;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class GroqAINodeExecutor implements NodeExecutor
{
    private final CredentialService credentialService;
    private final TemplateResolver templateResolver;
    private final ObjectMapper objectMapper;

    private final String GROQ_API_URL="https://api.groq.com/openai/v1/chat/completions";
    private static final int TIMEOUT_SECONDS = 60;

    @Override
    public Platform getSuportedPlatform() 
    {
        return Platform.GROQ_AI;
    }

    @Override
    public Object execute(WorkflowNode node, ExecutionContext context, String credentialsId) 
    {

        try 
        {
            Map<String,Object> credentials=credentialService.getDecryptedCredentials(credentialsId);
            String apiKey=(String)credentials.get("apiKey");
            if(apiKey== null || apiKey.isEmpty())
            {
                throw new NodeExecutionException("Groq AI credentials missing apiKey");
            }
            Map<String,String> config=node.getConfig();
            String promtTemplate=config.get("prompt");
            String model=config.getOrDefault("model","llama-3.3-70b-versatile" );
            String temperatureStr = config.getOrDefault("temperature", "0.7");
            String maxTokensStr = config.getOrDefault("maxTokens", "1024");
            
            if(promtTemplate==null||promtTemplate.isEmpty())
            {
                throw new NodeExecutionException("Groq AI node missing prompt configuration");
            }

            Map<String,Object> contextMap=context.toMap();
            String prompt= templateResolver.resolve(promtTemplate, contextMap);
            log.debug("Executing Groq AI with prompt: {}", prompt);
            double temperature;
            int maxTokens;
            try 
            {
                temperature = Double.parseDouble(temperatureStr);
                maxTokens = Integer.parseInt(maxTokensStr);
            } 
            catch (NumberFormatException e) 
            {
                throw new NodeExecutionException("Invalid temperature or maxTokens value", e);
            }

            Map <String,Object> requestBody=new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages",List.of(Map.of("role","user","content",prompt)));
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", maxTokens);
            
            String responseText=callGroqApi(apiKey, requestBody);
            Map<String,Object> responseJson=parseGroqResponse(responseText);

            log.info("Groq AI execution successful");
            
            return responseJson;
        
        } 
        catch (Exception e) 
        {
            log.error("Groq AI execution failed", e);
            throw new RuntimeException(e);
        }

    }

    private String callGroqApi(String apiKey,Map<String,Object> requestBody) throws Exception
    {
        HttpClient client=HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .build();
        
        String requestBodyJson=objectMapper.writeValueAsString(requestBody);

        HttpRequest request=HttpRequest.newBuilder()
                .uri(URI.create(GROQ_API_URL))
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer "+apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();
        
        log.debug("Sending request to Groq API");

        HttpResponse<String> response=client.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode()!=200)
        {
            log.error("Groq API returned error status: {} - {}", response.statusCode(), response.body());
            throw new NodeExecutionException(
                "Groq API request failed with status " + response.statusCode() + ": " + response.body()
            );
        }


        return response.body();
    }

    private Map<String,Object> parseGroqResponse(String responseText) throws Exception
    {
        Map<String,Object> fullResponse=objectMapper.readValue(responseText, Map.class);
        Map<String, Object> result = new HashMap<>();
        List<Map<String,Object>> choices=(List<Map<String,Object>>) fullResponse.get("choices");
        if(choices!=null & !choices.isEmpty())
        {
            Map<String,Object> firstChoice=choices.get(0);
            Map<String,Object> message=(Map<String,Object>)firstChoice.get("message");
            if(message!=null)
            {
                String content = (String) message.get("content");
                result.put("response", content);
                result.put("text", content);
            }
        }

        Map<String,Object> usage = (Map<String, Object>) fullResponse.get("usage");
        if(usage!=null)
        {
            result.put("usage", usage);
            result.put("promptTokens", usage.get("prompt_tokens"));
            result.put("completionTokens", usage.get("completion_tokens"));                
            result.put("totalTokens", usage.get("total_tokens"));
        }

        result.put("model", fullResponse.get("model"));
        result.put("id", fullResponse.get("id"));
        result.put("created", fullResponse.get("created"));
            
        // result.put("raw", fullResponse);
         return result;
    }
    
}
