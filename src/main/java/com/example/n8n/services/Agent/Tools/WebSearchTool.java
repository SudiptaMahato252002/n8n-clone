package com.example.n8n.services.Agent.Tools;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.n8n.models.workflow.ExecutionContext;
import com.example.n8n.services.Agent.AgentTool;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSearchTool implements AgentTool
{
    private ObjectMapper objectMapper=new ObjectMapper();
    private String apiKey="a6f332bf0877ed592e4aeac18ff5c9aab9392e0d9bda1e70de52211e82e678b6";
    private int maxResults=8;
    private static final int TIMEOUT_SECOONDS=20;

    @Override
    public String getName() 
    {
        return "WEB_SEARCH";
    }

    @Override
    public String getDescription() 
    {
        return "Searches the web for current information. Returns a list of results with title, snippet, and URL. Use this when you need up-to-date information not in your training data.";
    }

    @Override
    public Map<String, Object> getInputShema() 
    {

        Map<String,Object> schema=new HashMap<>();
        schema.put("query",Map.of(
            "type","string",
            "description","The search query string",
            "required","true"
        ));
        schema.put("numResults",Map.of(
            "type", "integer",
            "description", "How many results to return (default: 5, max: 10)",
            "required", false
        ));

        return schema;
    }

    @Override
    public Object run(Map<String, Object> input, ExecutionContext context) throws Exception 
    {
        String query=(String)input.get("query");
        if (query == null || query.isBlank()) 
        {
            throw new IllegalArgumentException("WebSearchTool: 'query' is required");
        }
        int num=input.containsKey("numResults")?((Number)input.get("numResults")).intValue():maxResults;
        num=Math.min(num,10);

        if (apiKey == null || apiKey.isBlank()) 
        {
            log.warn("[WEB_SEARCH] No API key configured — returning mock result");
            return mockResult(query);
        }

        log.info("[WEB_SEARCH] Searching: {}", query);
        String url=buildUrl(query, num);
        HttpClient client=HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(TIMEOUT_SECOONDS))
                        .build();
        HttpRequest request=HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(TIMEOUT_SECOONDS))
                    .GET()
                    .build();

        HttpResponse<String> response=client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) 
        {
            throw new RuntimeException("WebSearchTool API error: HTTP " + response.statusCode() + " — " + response.body());
        }
        return parseResponse(response.body(), num);
        
    }

    @SuppressWarnings("unchecked")
    private List<Map<String,String>> parseResponse(String resposneBody,int num) throws Exception
    {
        Map<String,Object> json=objectMapper.readValue(resposneBody,Map.class);
        List<Map<String,String>> results=new ArrayList<>();
        if(json.containsKey("organic_results"))
        {   
            List<Map<String,Object>> organic=(List<Map<String,Object>>)json.get("organic_results");
            for(Map<String,Object> item:organic)
            {
                if (results.size() >= num) break;
                Map<String, String> r = new HashMap<>();
                r.put("title",   String.valueOf(item.getOrDefault("title",   "")));
                r.put("snippet", String.valueOf(item.getOrDefault("snippet", "")));
                r.put("url",     String.valueOf(item.getOrDefault("link",    "")));
                results.add(r);
            }
        }
        return results;
    }

    private String buildUrl(String query,int num)
    {
        String encoded=URLEncoder.encode(query,StandardCharsets.UTF_8);
        String url="https://serpapi.com/search.json"
                + "?q=" + encoded
                + "&num=" + num
                + "&api_key=" + apiKey
                + "&engine=google";
        return url;
    }

    private List<Map<String, String>> mockResult(String query) 
    {
        return List.of(Map.of(
            "title",   "Mock result for: " + query,
            "snippet", "No API key configured. Set agent.tools.websearch.api-key in application.properties.",
            "url",     "https://example.com"
        ));
    }
    
}
