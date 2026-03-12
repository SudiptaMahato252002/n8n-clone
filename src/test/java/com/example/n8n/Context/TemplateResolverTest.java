package com.example.n8n.Context;

import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.example.n8n.models.workflow.ExecutionContext;
import com.example.n8n.utils.TemplateResolver;

public class TemplateResolverTest 
{

    TemplateResolver resolver = new TemplateResolver();

    // ─────────────────────────────────────────────
    // SCENARIO: Webhook fires → Groq runs → Email uses both
    // ─────────────────────────────────────────────

    @Test
    void shouldResolveWebhookBodyVariable() {
        // User sends { "email": "john@example.com" } via webhook
        ExecutionContext context = ExecutionContext.builder().build();
        context.setTriggerPayload(Map.of("email", "john@example.com"));

        String template = "Send to {{ $json.body.email }}";
        String result = resolver.resolve(template, context.toMap());

        System.out.println("Input:    " + template);
        System.out.println("Output:   " + result);

        assertThat(result).isEqualTo("Send to john@example.com");
    }

    

    @Test
    void shouldResolveGroqNodeResponse() {
        // Groq AI (n1) ran and returned a response
        ExecutionContext context = ExecutionContext.builder().build();

        Map<String, Object> groqResult = new HashMap<>();
        groqResult.put("response", "Once upon a time a robot learned to paint.");
        groqResult.put("model", "llama-3.3-70b-versatile");
        context.addNodeResult("n1", groqResult);

        // Email body template references Groq's output
        String template = "Here is your story:\n\n{{ $node.n1.response }}";
        String result = resolver.resolve(template, context.toMap());

        System.out.println("Input:    " + template);
        System.out.println("Output:   " + result);

        assertThat(result).contains("Once upon a time a robot learned to paint.");
    }

    @Test
    void shouldResolveEmailNodeMessageId() {
        // Email node (n1) ran and returned messageId
        ExecutionContext context = ExecutionContext.builder().build();

        Map<String, Object> emailResult = new HashMap<>();
        emailResult.put("messageId", "msg_abc123");
        emailResult.put("status", "sent");
        context.addNodeResult("n1", emailResult);

        // Groq prompt references email's messageId
        String template = "Email was sent with id {{ $node.n1.messageId }}";
        String result = resolver.resolve(template, context.toMap());

        System.out.println("Input:    " + template);
        System.out.println("Output:   " + result);

        assertThat(result).isEqualTo("Email was sent with id msg_abc123");
    }

    @Test
    void shouldResolveChainedNodes() {
        // Full chain: webhook → n1 (Groq) → n2 (Email)
        // n2's template references both webhook payload AND n1's result
        ExecutionContext context = ExecutionContext.builder().build();

        context.setTriggerPayload(Map.of("username", "Alice"));

        Map<String, Object> groqResult = new HashMap<>();
        groqResult.put("response", "Alice is a great programmer.");
        context.addNodeResult("n1", groqResult);

        String template = "Hi {{ $json.body.username }}, AI says: {{ $node.n1.response }}";
        String result = resolver.resolve(template, context.toMap());

        System.out.println("Input:    " + template);
        System.out.println("Output:   " + result);

        assertThat(result).isEqualTo("Hi Alice, AI says: Alice is a great programmer.");
    }

    @Test
    void shouldReturnTemplateUnchangedWhenVariableNotFound() {
        // If a variable doesn't exist in context, Mustache returns empty string
        // This test documents that behavior so you're not surprised
        ExecutionContext context = ExecutionContext.builder().build();

        String template = "Hello {{ $node.n99.response }}";
        String result = resolver.resolve(template, context.toMap());

        System.out.println("Input:    " + template);
        System.out.println("Output:   '" + result + "'");

        // Mustache renders missing vars as empty string — not null, not the original
        assertThat(result).isEqualTo("Hello ");
    }

    @Test
    void shouldHandleTemplateWithNoVariables() {
        ExecutionContext context = ExecutionContext.builder().build();

        String template = "Hello World";
        String result = resolver.resolve(template, context.toMap());

        assertThat(result).isEqualTo("Hello World");
    }
    
}
