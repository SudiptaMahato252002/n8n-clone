package com.example.n8n.Context;

import com.example.n8n.models.workflow.ExecutionContext;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple test showing how ExecutionContext data evolves step by step
 * during a 3-node workflow: WEBHOOK → GROQ_AI → RESEND_EMAIL
 *
 * We are NOT testing real node execution here — just how the context
 * shape changes as each node's result is recorded.
 */
public class ExecutionContextEvolutionTest {

    @Test
    void contextShouldBeEmptyBeforeTrigger() {
        ExecutionContext context = ExecutionContext.builder().build();

        // $json and $node exist but are empty maps
        assertThat(context.toMap().get("$json")).isNotNull();
        assertThat(context.toMap().get("$node")).isNotNull();

        Map<String, Object> json = (Map<String, Object>) context.toMap().get("$json");
        Map<String, Object> node = (Map<String, Object>) context.toMap().get("$node");

        assertThat(json).isEmpty();
        assertThat(node).isEmpty();

        System.out.println("=== INITIAL STATE ===");
        System.out.println(context.toMap());
        // { $json: {}, $node: {} }
    }

    @Test
    void contextShouldHoldTriggerPayloadAfterWebhookFires() {
        ExecutionContext context = ExecutionContext.builder().build();

        // Simulates: webhook fires with this body
        Map<String, Object> webhookPayload = new HashMap<>();
        webhookPayload.put("message", "Summarize this text for me");
        webhookPayload.put("userId", "user_abc");

        context.setTriggerPayload(webhookPayload);

        Map<String, Object> contextMap = context.toMap();
        Map<String, Object> json = (Map<String, Object>) contextMap.get("$json");

        // $json now has { body: { message: "...", userId: "..." } }
        assertThat(json).containsKey("body");

        Map<String, Object> body = (Map<String, Object>) json.get("body");
        assertThat(body.get("message")).isEqualTo("Summarize this text for me");
        assertThat(body.get("userId")).isEqualTo("user_abc");

        // $node is still empty — no node has run yet
        Map<String, Object> node = (Map<String, Object>) contextMap.get("$node");
        assertThat(node).isEmpty();

        System.out.println("=== AFTER WEBHOOK TRIGGER (n0) ===");
        System.out.println(contextMap);
        /*
         * {
         *   $json: { body: { message: "Summarize this text for me", userId: "user_abc" } },
         *   $node: {}
         * }
         */
    }

    @Test
    void contextShouldHoldN1ResultAfterGroqAiExecutes() {
        ExecutionContext context = ExecutionContext.builder().build();

        // Step 1: webhook fires
        Map<String, Object> webhookPayload = new HashMap<>();
        webhookPayload.put("message", "Summarize this text for me");
        context.setTriggerPayload(webhookPayload);

        // Step 2: n1 (GROQ_AI) returns a result
        // In real execution, nodeExecutorService.executeNode() returns this
        Map<String, Object> n1Result = new HashMap<>();
        n1Result.put("summary", "This text asks for a summary.");
        n1Result.put("model", "llama-3.3-70b-versatile");
        n1Result.put("tokensUsed", 42);

        // WorkflowWorker calls this after each node:
        context.addNodeResult("n1", n1Result);

        Map<String, Object> contextMap = context.toMap();
        Map<String, Object> node = (Map<String, Object>) contextMap.get("$node");

        // $node now has n1's result
        assertThat(node).containsKey("n1");

        Map<String, Object> n1Data = (Map<String, Object>) node.get("n1");
        assertThat(n1Data.get("summary")).isEqualTo("This text asks for a summary.");

        // $json is still the original webhook payload — unchanged
        Map<String, Object> json = (Map<String, Object>) contextMap.get("$json");
        Map<String, Object> body = (Map<String, Object>) json.get("body");
        assertThat(body.get("message")).isEqualTo("Summarize this text for me");

        System.out.println("=== AFTER n1 (GROQ_AI) ===");
        System.out.println(contextMap);
        /*
         * {
         *   $json: { body: { message: "Summarize this text for me" } },
         *   $node: {
         *     n1: { summary: "This text asks for a summary.", model: "llama-3.3-70b-versatile", tokensUsed: 42 }
         *   }
         * }
         *
         * NOTE: EmailNodeExecutor calls templateResolver.resolve("{{n1.output}}", contextMap)
         * which reads from $node.n1 — this is where the template gets its data.
         */
    }

    @Test
    void contextShouldHoldAllNodeResultsAfterFullExecution() {
        ExecutionContext context = ExecutionContext.builder().build();

        // Step 1: webhook fires
        Map<String, Object> webhookPayload = new HashMap<>();
        webhookPayload.put("message", "Summarize this text for me");
        context.setTriggerPayload(webhookPayload);

        // Step 2: n1 (GROQ_AI) result added
        Map<String, Object> n1Result = new HashMap<>();
        n1Result.put("summary", "This text asks for a summary.");
        n1Result.put("tokensUsed", 42);
        context.addNodeResult("n1", n1Result);

        // Step 3: n2 (RESEND_EMAIL) result added
        Map<String, Object> n2Result = new HashMap<>();
        n2Result.put("messageId", "email_abc123");
        n2Result.put("to", "user@email.com");
        n2Result.put("status", "sent");
        context.addNodeResult("n2", n2Result);

        Map<String, Object> contextMap = context.toMap();
        Map<String, Object> node = (Map<String, Object>) contextMap.get("$node");

        // Both n1 and n2 are present
        assertThat(node).containsKeys("n1", "n2");

        Map<String, Object> n2Data = (Map<String, Object>) node.get("n2");
        assertThat(n2Data.get("status")).isEqualTo("sent");
        assertThat(n2Data.get("messageId")).isEqualTo("email_abc123");

        // $json is still untouched — it only ever holds the trigger payload
        Map<String, Object> json = (Map<String, Object>) contextMap.get("$json");
        assertThat(json).containsKey("body");
        assertThat(node).doesNotContainKey("n0"); // webhook node never adds a result

        System.out.println("=== FINAL STATE (after n2 RESEND_EMAIL) ===");
        System.out.println(contextMap);
        /*
         * {
         *   $json: { body: { message: "Summarize this text for me" } },
         *   $node: {
         *     n1: { summary: "This text asks for a summary.", tokensUsed: 42 },
         *     n2: { messageId: "email_abc123", to: "user@email.com", status: "sent" }
         *   }
         * }
         */
    }

    @Test
    void getTriggerPayloadShouldReturnBodyFromJson() {
        ExecutionContext context = ExecutionContext.builder().build();

        Map<String, Object> payload = new HashMap<>();
        payload.put("foo", "bar");
        context.setTriggerPayload(payload);

        // getTriggerPayload() unwraps $json.body
        Map<String, Object> retrieved = context.getTriggerPayload();
        assertThat(retrieved.get("foo")).isEqualTo("bar");
    }

    @Test
    void getNodeResultShouldReturnResultByNodeId() {
        ExecutionContext context = ExecutionContext.builder().build();

        Map<String, Object> result = new HashMap<>();
        result.put("answer", "42");
        context.addNodeResult("n1", result);

        Object retrieved = context.getNodeResult("n1");
        assertThat(retrieved).isEqualTo(result);

        // Non-existent node returns null
        assertThat(context.getNodeResult("n99")).isNull();
    }
}
