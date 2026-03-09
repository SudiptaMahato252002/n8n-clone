package com.example.n8n.GroqAi;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.n8n.enums.Platform;
import com.example.n8n.models.workflow.ExecutionContext;
import com.example.n8n.models.workflow.WorkflowNode;
import com.example.n8n.services.Credentials.CredentialService;
import com.example.n8n.services.Node.Executors.EmailNodeExecutors;
import com.example.n8n.services.Node.Executors.GroqAINodeExecutor;
import com.example.n8n.utils.JsonUtils;
import com.example.n8n.utils.TemplateResolver;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExecutorTest 
{
    @Test
    void testExecuteWithHardcodedValues() {

        // ✅ Create mocks
        CredentialService credentialService = mock(CredentialService.class);
        TemplateResolver templateResolver = mock(TemplateResolver.class);

        ObjectMapper objectMapper = new ObjectMapper();

        // ✅ Mock credential response
        Map<String, Object> creds = new HashMap<>();
        creds.put("apiKey", "");

        when(credentialService.getDecryptedCredentials("cred-1"))
                .thenReturn(creds);

        // ✅ Mock template resolution
        when(templateResolver.resolve(anyString(), any()))
                .thenReturn("Write a haiku about programming");

        // ✅ Create executor
        GroqAINodeExecutor executor =
                new GroqAINodeExecutor(credentialService, templateResolver, objectMapper);

        // ✅ Create workflow node
        WorkflowNode node = new WorkflowNode();
        node.setId("1");
        node.setType(Platform.GROQ_AI);
        node.setCredentialsId("cred-1");

        Map<String, String> config = new HashMap<>();
        config.put("prompt", "Explain why fast inference is critical for reasoning models");
        config.put("model", "openai/gpt-oss-120b");
        config.put("temperature", "0.7");
        config.put("maxTokens", "100");
        node.setConfig(config);

        ExecutionContext context = ExecutionContext.builder().build();

        // ✅ Execute
        try {
            executor.execute(node, context, "cred-1");
        } catch (Exception e) {
            // API will fail (dummy key) — that’s fine
        }

        // ✅ Verify basic flow happened
        verify(credentialService, times(1))
                .getDecryptedCredentials("cred-1");

        verify(templateResolver, times(1))
                .resolve(anyString(), any());

        System.out.println("Simple execution flow test passed.");
    }

    @Test
    void testRealGroqExecution() {

        // ⚠️ NEVER hardcode key — use env variable
        String apiKey = "";

        // 🔹 Fake CredentialService returning real key
        CredentialService credentialService = new CredentialService(null, null) {
            @Override
            public Map<String, Object> getDecryptedCredentials(String id) {
                Map<String, Object> creds = new HashMap<>();
                creds.put("apiKey", apiKey);
                return creds;
            }
        };

        // 🔹 Real TemplateResolver
        TemplateResolver templateResolver = new TemplateResolver();

        ObjectMapper objectMapper = new ObjectMapper();

        GroqAINodeExecutor executor =
                new GroqAINodeExecutor(credentialService, templateResolver, objectMapper);

        WorkflowNode node = new WorkflowNode();
        node.setId("1");
        node.setType(Platform.GROQ_AI);
        node.setCredentialsId("cred-1");

        Map<String, String> config = new HashMap<>();
        config.put("prompt", "MAKE A ADD TWO INTEGER JAVA CODE");
        config.put("model", "llama-3.3-70b-versatile");
        config.put("temperature", "0.7");
        config.put("maxTokens", "150");
        node.setConfig(config);

        ExecutionContext context = ExecutionContext.builder().build();

        // 🔥 Execute
        Object result = executor.execute(node, context, "cred-1");

        assertNotNull(result);

        System.out.println("======== GROQ RESPONSE ========");
        System.out.println(result);
        System.out.println("================================");
    }



    @Test
    void testGroqGeneratesStoryAndEmailSendsIt() throws Exception {
        
        // ⚠️ NEVER hardcode keys — use env variables
        String groqApiKey = "";
        String resendApiKey = "";
        String fromEmail = "onboarding@resend.dev";
        String toEmail = "sudipta.mahato.ece25@heritageit.edu.in";


        // 🔹 Fake CredentialService
        CredentialService credentialService = new CredentialService(null, null) {
            @Override
            public Map<String, Object> getDecryptedCredentials(String id) {
                Map<String, Object> creds = new HashMap<>();
                
                if (id.equals("groq-cred")) {
                    creds.put("apiKey", groqApiKey);
                } 
                else if (id.equals("email-cred")) {
                    creds.put("apiKey", resendApiKey);
                    creds.put("fromEmail", fromEmail);
                }
                
                return creds;
            }
        };

        // 🔹 Real TemplateResolver
        TemplateResolver templateResolver = new TemplateResolver();

        ObjectMapper objectMapper = new ObjectMapper();

        // 🔹 Create Executors
        GroqAINodeExecutor groqExecutor = 
                new GroqAINodeExecutor(credentialService, templateResolver, objectMapper);

        EmailNodeExecutors emailExecutor = 
                new EmailNodeExecutors(credentialService, templateResolver);

        // ============================================================
        // STEP 1: GROQ AI NODE - GENERATE STORY
        // ============================================================

        WorkflowNode groqNode = new WorkflowNode();
        groqNode.setId("node_1");
        groqNode.setType(Platform.GROQ_AI);
        groqNode.setCredentialsId("groq-cred");

        Map<String, String> groqConfig = new HashMap<>();
        groqConfig.put("prompt", "Write a 5-line about robot learning to paint exatly 5 line not more");
        groqConfig.put("model", "llama-3.3-70b-versatile");
        groqConfig.put("temperature", "0.9");
        groqConfig.put("maxTokens", "500");
        groqNode.setConfig(groqConfig);

        ExecutionContext context = ExecutionContext.builder().build();

        // 🔥 Execute Groq AI
        Object groqResult = groqExecutor.execute(groqNode, context, "groq-cred");

        assertNotNull(groqResult);

        System.out.println("======== GROQ AI STORY ========");
        System.out.println(groqResult);
        System.out.println("================================");

        // 🔹 Add Groq result to context (so email can use it)
        context.addNodeResult("node_1", groqResult);
        System.out.println("======== CONTEXT ========");
        // Map<String,Object> contextMap=objectMapper.readValue(context.toString(),Map.class);
        System.out.println(context.toMap());

        System.out.println("================");
        // ============================================================
        // STEP 2: EMAIL NODE - SEND THE STORY
        // ============================================================

        WorkflowNode emailNode = new WorkflowNode();
        emailNode.setId("node_2");
        emailNode.setType(Platform.RESEND_EMAIL);
        emailNode.setCredentialsId("email-cred");

        Map<String, String> emailConfig = new HashMap<>();
        emailConfig.put("to", toEmail);
        emailConfig.put("subject", "Your AI Generated Story");
        emailConfig.put("body", "Here is your story:\n\n{{ $node.node_1.text }}");
        emailNode.setConfig(emailConfig);

        // 🔥 Execute Email
        Object emailResult = emailExecutor.execute(emailNode, context, "email-cred");

        assertNotNull(emailResult);

        System.out.println("======== EMAIL SENT ========");
        System.out.println(emailResult);
        System.out.println("============================");

        System.out.println("\n✅ SUCCESS! Story generated and emailed!");
    }
}
