package com.example.n8n.services.Workflow;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.n8n.exceptions.WorkflowNotFoundException;
import com.example.n8n.models.dtos.request.CreateWorkflowRequest;
import com.example.n8n.models.dtos.request.WebhookConfigRequest;
import com.example.n8n.models.dtos.response.WebhookResponse;
import com.example.n8n.models.dtos.response.WorkflowResponse;
import com.example.n8n.models.entity.Webhook;
import com.example.n8n.models.entity.Workflow;
import com.example.n8n.repo.ExecutionRepo;
import com.example.n8n.repo.WebhookRepo;
import com.example.n8n.repo.WorkflowRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowService 
{
    private final WorkflowValidationService workflowValidationService;
    private final WorkflowRepo workflowRepo;
    private final WebhookRepo webhookRepo;
    private final ExecutionRepo executionRepo;
    
    @Transactional
    public WorkflowResponse createWorkflow(CreateWorkflowRequest request,String userId)
    {
        workflowValidationService.validateWorkflow(request.getNodes(), request.getConnections());
        Webhook webhook=createWebhook(request.getWebhook());
        Workflow workflow=Workflow.builder()
                .userId(userId)
                .title(request.getTitle())
                .triggerType(request.getTriggerType())
                .nodes(request.getNodes())
                .connections(request.getConnections())
                .webhookId(webhook.getId())
                .enabled(true)
                .build();
        Workflow saved=workflowRepo.save(workflow);
        return toResponse(saved);
    }

     @Transactional(readOnly = true)
    public WorkflowResponse getWorkflow(String id, String userId) {
        log.info("Fetching workflow: {} for user: {}", id, userId);
        
        Workflow workflow = workflowRepo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new WorkflowNotFoundException("Workflow not found with id: " + id));
        
        return toResponse(workflow);
    }
    
    @Transactional(readOnly = true)
    public List<WorkflowResponse> listWorkflows(String userId) {
        log.info("Listing workflows for user: {}", userId);
        
        return workflowRepo.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public void deleteWorkflow(String workflowId,String userId)
    {
         log.info("Deleting workflow: {} for user: {}", workflowId, userId);
        Workflow workflow=workflowRepo.findByIdAndUserId(workflowId, userId).orElseThrow(()->new RuntimeException("Workflow not found with id: " + workflowId));
        if(workflow.getWebhookId()!=null)
        {
            webhookRepo.deleteById(workflow.getWebhookId());
        }
        workflowRepo.delete(workflow);
        log.info("Workflow deleted: {}", workflowId);
    }




    private Webhook createWebhook(WebhookConfigRequest request)
    {
        Webhook webhook=Webhook.builder()
                .title(request.getTitle())
                .method(request.getMethod())
                .secret(request.getSecret())
                .build();
        return webhookRepo.save(webhook);

    }

    private WorkflowResponse toResponse(Workflow workflow) {
        Long executionCount = (long) executionRepo.findByWorkflowId(workflow.getId()).size();
        
        WebhookResponse webhookResponse = null;
        if (workflow.getWebhookId() != null) {
            webhookRepo.findById(workflow.getWebhookId()).ifPresent(webhook -> {
                // Create webhook response inline
            });
        }
        
        return WorkflowResponse.builder()
                .id(workflow.getId())
                .userId(workflow.getUserId())
                .title(workflow.getTitle())
                .enabled(workflow.getEnabled())
                .triggerType(workflow.getTriggerType())
                .nodes(workflow.getNodes())
                .connections(workflow.getConnections())
                .webhookId(workflow.getWebhookId())
                .webhook(webhookResponse)
                .executionCount(executionCount)
                .createdAt(workflow.getCreatedAt())
                .updatedAt(workflow.getUpdatedAt())
                .build();
    }


}
