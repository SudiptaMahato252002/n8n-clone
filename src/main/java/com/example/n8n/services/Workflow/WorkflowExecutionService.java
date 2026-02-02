package com.example.n8n.services.Workflow;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.n8n.enums.TriggerType;
import com.example.n8n.exceptions.WorkflowNotFoundException;
import com.example.n8n.models.dtos.response.ExecutionResponse;
import com.example.n8n.models.entity.Execution;
import com.example.n8n.models.entity.Workflow;
import com.example.n8n.repo.WorkflowRepo;
import com.example.n8n.services.Execution.ExecutionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowExecutionService 
{
    private final ExecutionService executionService;
    private final WorkflowRepo workflowRepo;

    @Transactional
    public ExecutionResponse triggerWebhookWorkflow(String webhookId,Map<String,Object> payload)
    {
        log.info("Triggering webhook workflow: webhookId={}", webhookId);
        Workflow workflow=workflowRepo.findByWebhookId(webhookId).orElseThrow(()->new WorkflowNotFoundException(webhookId));
        if (!workflow.getEnabled()) 
        {
            throw new IllegalStateException("Workflow " + workflow.getId() + " is disabled");
        }
        int totalTasks=workflow.getNodes()!=null?workflow.getNodes().size():0;
        if (totalTasks == 0) 
        {
            throw new IllegalStateException("Workflow " + workflow.getId() + " has no nodes");
        }
        Execution execution=executionService.createExecution(workflow.getId(), totalTasks, payload!=null?payload:new HashMap<>());
        log.info("Webhook workflow triggered successfully: executionId={}", execution.getId());
        
        return toResponse(execution);
    }
    
    @Transactional
    public ExecutionResponse triggerManualWorkflow(String workflowId,String userId,Map<String,Object> payload)
    {
        log.info("Triggering workflow: workflowId={}", workflowId);
        Workflow workflow=workflowRepo.findByIdAndUserId(workflowId,userId).orElseThrow(()->new WorkflowNotFoundException("Workflow not found: " + workflowId));
        if(!workflow.getEnabled())
        {
            throw new IllegalStateException("Workflow " + workflow.getId() + " is disabled");   
        }
        
        if (workflow.getTriggerType() != TriggerType.MANUAL) {
            throw new IllegalArgumentException("Workflow " + workflowId + " is not a manual trigger workflow");
        }
        
        int totalTasks=workflow.getNodes()!=null?workflow.getNodes().size():0;
        if (totalTasks == 0) 
        {
            throw new IllegalStateException("Workflow " + workflow.getId() + " has no nodes");
        }
        
        Execution execution=executionService.createExecution(workflow.getId(), totalTasks, payload!=null?payload:new HashMap<>());
        log.info("Webhook workflow triggered successfully: executionId={}", execution.getId());
        return toResponse(execution);
    }

    private ExecutionResponse toResponse(Execution execution)
    {   
        return ExecutionResponse.builder()
                .id(execution.getId())
                .workflowId(execution.getWorkflowId())
                .status(execution.getStatus())
                .totalTasks(execution.getTotalTasks())
                .tasksDone(execution.getTasksDone())
                .outputs(execution.getOutputs())
                .logs(execution.getLogs())
                .startedAt(execution.getStartedAt())
                .completedAt(execution.getCompletedAt())
                .build();
    }

}
