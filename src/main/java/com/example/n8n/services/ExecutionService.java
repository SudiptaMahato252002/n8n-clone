package com.example.n8n.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.n8n.enums.ExecutionStatus;
import com.example.n8n.models.dtos.response.ExecutionResponse;
import com.example.n8n.models.entity.Execution;
import com.example.n8n.repo.ExecutionRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExecutionService 
{
    private final ExecutionRepo executionRepo;
    //LATER ADD OUTBOX PATTERN FOR EXECUTION EVENTS

    @Transactional
    public Execution createExection(String workflowId,int totalTasks,Map<String,Object> triggerPayload)
    {
        Map<String,Object> output=new HashMap<>();
        output.put("triggerPayload", triggerPayload);
        Execution execution=Execution.builder()
                .workflowId(workflowId)
                .status(ExecutionStatus.PENDING)
                .totalTasks(totalTasks)
                .tasksDone(0)
                .outputs(output)
                .logs(new HashMap<>())
                .build();

        Execution saved=executionRepo.save(execution);
        log.info("Execution created with id: {}", saved.getId());
        return saved;
    }

    @Transactional
    public void updateExectionStatus(String executionId,ExecutionStatus status)
    {
        log.info("Updating execution: {} to status: {}", executionId, status);
        Execution execution=executionRepo.findById(executionId).orElseThrow(()->new RuntimeException("Execution not found with id: " + executionId));
        execution.setStatus(status);
        if (status == ExecutionStatus.SUCCESS || status == ExecutionStatus.FAILED) {
            execution.setCompletedAt(LocalDateTime.now());
        }
        executionRepo.save(execution);
    }

    @Transactional
    public void updateExecutionOutputs(String executionId,Map<String,Object> output)
    {
        log.debug("Updating execution output: {}", executionId);
        Execution execution=executionRepo.findById(executionId).orElseThrow(()->new RuntimeException("Execution not found with id: " + executionId));
        execution.setOutputs(output);
        executionRepo.save(execution);
    }

    @Transactional
    public void updateExecutionProgress(String executionId,int tasksDone,Map<String,Object> logs)
    {
        log.debug("Updating execution progress: {}, tasks done: {}", executionId, tasksDone);
        Execution execution=executionRepo.findById(executionId).orElseThrow(()->new RuntimeException("Execution not found with id: " + executionId));
        execution.setTasksDone(tasksDone);
        if (logs != null) 
        {
            execution.setLogs(logs);
        }
        executionRepo.save(execution);
    }
    
    @Transactional(readOnly = true)
    public List<ExecutionResponse> listExecutions(String workflowId) {
        log.info("Listing executions for workflow: {}", workflowId);
        
        return executionRepo.findByWorkflowIdOrderByStartedAtDesc(workflowId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExecutionResponse getExecutionResponseById(String executionId)
    {
        log.info("Fetching execution: {}", executionId);
        Execution execution=executionRepo.findById(executionId).orElseThrow(()->new RuntimeException("Execution not found with id: " + executionId));
        return toResponse(execution);
    }

    private ExecutionResponse toResponse(Execution execution) {
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