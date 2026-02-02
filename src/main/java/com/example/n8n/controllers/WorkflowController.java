package com.example.n8n.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.n8n.models.dtos.request.CreateWorkflowRequest;
import com.example.n8n.models.dtos.request.TriggerWorkflowRequest;
import com.example.n8n.models.dtos.response.ExecutionResponse;
import com.example.n8n.models.dtos.response.WorkflowResponse;
import com.example.n8n.services.Workflow.WorkflowExecutionService;
import com.example.n8n.services.Workflow.WorkflowService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
public class WorkflowController 
{
    private final WorkflowService workflowService;
    private final WorkflowExecutionService workflowExecutionService;

    @PostMapping("/{userId}")
    public ResponseEntity<WorkflowResponse> createWokrflow(CreateWorkflowRequest request,@PathVariable String userId)
    {
        WorkflowResponse response=workflowService.createWorkflow(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

     @GetMapping("/{id}/{userId}")
    public ResponseEntity<WorkflowResponse> getWorkflow(@PathVariable String id,@PathVariable String userId) {
        WorkflowResponse response = workflowService.getWorkflow(id, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<WorkflowResponse>> listWorkflows(@PathVariable String userId) 
    {
        List<WorkflowResponse> workflows = workflowService.listWorkflows(userId);
        return ResponseEntity.ok(workflows);
    }

    @DeleteMapping("/{id}/{userId}")
    public ResponseEntity<Void> deleteWorkflow(@PathVariable String id,@PathVariable String userId) {
        workflowService.deleteWorkflow(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/{userId}/trigger")
    public ResponseEntity<ExecutionResponse> triggerWorkflow(@PathVariable String id,@PathVariable String userId,@RequestBody TriggerWorkflowRequest request)
    {
        ExecutionResponse response=workflowExecutionService.triggerManualWorkflow(id,userId, request.getInput());
        return ResponseEntity.ok(response);
    }

}
