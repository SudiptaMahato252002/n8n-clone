package com.example.n8n.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.n8n.models.dtos.response.ExecutionResponse;
import com.example.n8n.services.Execution.ExecutionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ExecutionController 
{
    private final ExecutionService executionService;

    @GetMapping("/{id}")
    public ResponseEntity<ExecutionResponse> getExecution(@PathVariable String id)
    {
        ExecutionResponse response=executionService.getExecutionById(id);
        return ResponseEntity.ok(response);
    }   

    @GetMapping("/workflow/{workflowId}")
    public ResponseEntity<List<ExecutionResponse>> listExecutionsByWorkflow(@PathVariable String workflowId) {
        List<ExecutionResponse> executions = executionService.listExecutions(workflowId);
        return ResponseEntity.ok(executions);
    }
}
