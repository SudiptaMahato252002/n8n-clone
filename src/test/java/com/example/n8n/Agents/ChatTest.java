package com.example.n8n.Agents;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.n8n.services.Agent.ModelService;
import com.example.n8n.services.Agent.Tools.WebSearchTool;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChatTest 
{
    @Test
    void executeChat() throws Exception
    {
//         WebSearchTool webSearchTool=new WebSearchTool();
//         // ModelService modelService=new ModelService();
//         // String response1=modelService.simpleChat(apiKey);
//         System.out.println(response1);
//         ObjectMapper objectMapper=new ObjectMapper();
//         Map<String, Object> json =objectMapper.readValue(response1, Map.class);
//         System.out.println(json);
//         List<Map<String, Object>> choices =
//         (List<Map<String, Object>>) json.get("choices");
//         System.out.println(choices.toString());

// // 2. Get first message
//         Map<String, Object> message =
//         (Map<String, Object>) choices.get(0).get("message");
//         System.out.println(message.toString());
// // // 3. Extract content
//         String content = (String) message.get("content");
//         System.out.println(content);
//         List<Map<String,Object>> toolCalls=(List<Map<String,Object>>)message.get("tool_calls");
//         System.out.println(toolCalls.toString());

//         if(toolCalls!=null && !toolCalls.isEmpty())
//         {
//             System.out.println("LLM wants to call a tool 🚀");
//             Map<String, Object> toolCall = toolCalls.get(0);

//             Map<String, Object> function =
//                 (Map<String, Object>) toolCall.get("function");

//             String functionName = (String) function.get("name");
//             String arguments = (String) function.get("arguments");

//             System.out.println("Function: " + functionName);
//             System.out.println("Arguments: " + arguments);

//             // ✅ parse args
//             Map<String, Object> argsMap =
//                 objectMapper.readValue(arguments, Map.class);

//             // 🔥 EXECUTE CORRECT TOOL
//             Object toolResult = null;

//             if ("WEB_SEARCH".equals(functionName)) 
//             {
//                 toolResult = webSearchTool.run(argsMap, null);
//             }

//             System.out.println("Tool Result: " + toolResult);

    //     }
    //     else
    //     {
    //         System.out.println("No tool call 🚀");
    //     }
    }
    
}
