package com.example.n8n.services.Workflow;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.n8n.enums.TriggerType;
import com.example.n8n.exceptions.WebhookCollisonException;
import com.example.n8n.exceptions.WebhookNotFoundException;
import com.example.n8n.exceptions.WorkflowNotFoundException;
import com.example.n8n.models.dtos.request.CreateWorkflowRequest;
import com.example.n8n.models.dtos.request.UpdateWorkflowRequest;
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
        // log.info("Starting validation");
        // workflowValidationService.validateWorkflow(request.getNodes(), request.getConnections());
        // log.info("Validation done");
        String webhookId=null;
        if(request.getWebhook()!=null)
        {
            Webhook webhook=createWebhook(request.getWebhook());
            webhookId=webhook.getId();
        }
        
        
        Workflow workflow=Workflow.builder()
                .userId(userId)
                .title(request.getTitle())
                .triggerType(request.getTriggerType())
                .nodes(request.getNodes())
                .connections(request.getConnections())
                .webhookId(webhookId)
                .enabled(true)
                .build();
        Workflow saved=workflowRepo.save(workflow);
        return toResponse(saved);
    }

    public WorkflowResponse updateWorkflow(String id,UpdateWorkflowRequest request,String userId)
    {
        log.info("Updating workflow: {} for user: {}", id, userId);
        
        Workflow workflow = workflowRepo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new WorkflowNotFoundException("Workflow not found with id: " + id));

        TriggerType oldTrigger=workflow.getTriggerType();
        TriggerType newTrigger=request.getTriggerType();
        if(request.getTitle()!=null)
        {
            workflow.setTitle(request.getTitle());
        }

        if(request.getEnabled()!=null)
        {
            workflow.setEnabled(request.getEnabled());
        }

        if(request.getNodes()!=null&&request.getConnections()!=null)
        {
            workflowValidationService.validateWorkflow(request.getNodes(), request.getConnections());
            workflow.setNodes(request.getNodes());
            workflow.setConnections(request.getConnections());
        }
        //please hadle the webhook change later

        if(request.getTriggerType()!=null)
        {
            if(oldTrigger == TriggerType.WEBHOOK && newTrigger == TriggerType.MANUAL)
            {
                deleteWebhook(workflow.getWebhookId());
                workflow.setWebhookId(null);
            }

            else if(oldTrigger == TriggerType.MANUAL && newTrigger == TriggerType.WEBHOOK)
            {
                Webhook webhook = createWebhook(request.getWebhook());
                workflow.setWebhookId(webhook.getId());
            }

            else if(oldTrigger == TriggerType.WEBHOOK && newTrigger == TriggerType.WEBHOOK)
            {
                updateWebhook(request.getWebhook());
            }

            workflow.setTriggerType(newTrigger);
        }
    
        Workflow updated=workflowRepo.save(workflow);
        log.info("Workflow updated: {}", id);
        
        return toResponse(updated);


    
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
            long count = workflowRepo.findByUserId(userId).stream()
                    .filter(w -> workflow.getWebhookId().equals(w.getWebhookId()) && !w.getId().equals(workflowId))
                    .count();
            if(count==0)
            {
                webhookRepo.deleteById(workflow.getWebhookId());
                log.info("Deleted webhook: {}", workflow.getWebhookId());
            }
            else
            {
                log.info("Webhook {} still in use by {} other workflows", workflow.getWebhookId(), count);
            }
            }
            
        workflowRepo.delete(workflow);
        log.info("Workflow deleted: {}", workflowId);
    }




    private Webhook createWebhook(WebhookConfigRequest request)
    {
        if(request.getId()==null||request.getId().isEmpty())
        {
            throw new IllegalArgumentException("Webhook ID must be provided");
        }

        if(webhookRepo.existsById(request.getId()))
        {
            log.warn("Webhook ID collision detected: {}", request.getId());
            throw new WebhookCollisonException(
                "Webhook ID already exists. Please retry with a new ID."
            );
        }
        Webhook webhook=Webhook.builder()
                .id(request.getId())
                .title(request.getTitle())
                .method(request.getMethod())
                .secret(request.getSecret())
                .build();
        log.info("Webhook created with ID: {}", webhook.getId());
        return webhookRepo.save(webhook);

    }

    private Webhook updateWebhook(WebhookConfigRequest request)
    {
        Webhook webhook=webhookRepo.findById(request.getId()).orElseThrow(()->new WebhookNotFoundException("Webhook not found"));
        
        webhook.setTitle(request.getTitle());
        webhook.setSecret(request.getSecret());
        webhook.setMethod(request.getMethod());
        return webhookRepo.save(webhook);
    }

    private void deleteWebhook(String webhookId)
    {
        if(webhookId!=null)
        {
            webhookRepo.deleteById(webhookId);
            log.info("Deleted webhook: {}", webhookId);
        }
    }

    private WorkflowResponse toResponse(Workflow workflow) {
        Long executionCount = (long) executionRepo.findByWorkflowId(workflow.getId()).size();
        
        WebhookResponse webhookResponse = null;
        if (workflow.getWebhookId() != null) 
        {
            Webhook webhook=webhookRepo.findById(null).orElse(null);
            if(webhook!=null)
            {
                webhookResponse = WebhookResponse.builder()
                        .id(webhook.getId())
                        .title(webhook.getTitle())
                        .method(webhook.getMethod())
                        .secret(webhook.getSecret())
                        .createdAt(webhook.getCreatedAt())
                        .build();
            }
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
