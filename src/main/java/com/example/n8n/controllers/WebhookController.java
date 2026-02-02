package com.example.n8n.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.n8n.models.dtos.response.ExecutionResponse;
import com.example.n8n.models.entity.Webhook;
import com.example.n8n.repo.WebhookRepo;
import com.example.n8n.services.Workflow.WorkflowExecutionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class WebhookController 
{
    private final WebhookRepo webhookRepo;
    private final WorkflowExecutionService workflowExecutionService;

    @RequestMapping(
        value = "/{webhookId}",
        method = {
            RequestMethod.GET,
            RequestMethod.POST,
            RequestMethod.PUT,
            RequestMethod.DELETE,
            RequestMethod.PATCH
        }
    )
    public ResponseEntity<?> handleWebhook(@PathVariable String webhookId,
                                            @RequestHeader(required = false) String authHeader,
                                            @RequestBody(required = false) Map<String, Object> body)
    {   
        log.info("Webhook triggered: {}", webhookId);
        try 
        {
            Webhook webhook=webhookRepo.findById(webhookId).orElse(null);
            if(webhook==null)
            {
                log.warn("Webhook not found: {}", webhookId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Webhook not found"));
                
            }

            if(webhook.getSecret()!=null||!webhook.getSecret().isEmpty())
            {
                if(authHeader==null||!authHeader.equals(webhook.getSecret()))
                {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error","Unauthorized"));
                }
            }
            ExecutionResponse response= workflowExecutionService.triggerWebhookWorkflow(webhookId, body!=null?body:Map.of());
            log.info("Webhook workflow triggered: executionId={}", response.getId());
            log.info(response.toString());

            return ResponseEntity.ok(Map.of(
                "message", "Workflow triggered successfully",
                "executionId", response.getId()
            ));
        
        } 
        catch (Exception e) 
        {
             log.error("Error handling webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
            // TODO: handle exception
        }
    }
}
