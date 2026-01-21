package com.example.n8n.models.dtos.response;

import java.time.LocalDateTime;
import java.util.Map;

import com.example.n8n.enums.ExecutionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResponse 
{
    private String id;
    private String workflowId;
    private ExecutionStatus status;
    private Integer totalTasks;
    private Integer tasksDone;
    private Map<String, Object> outputs;
    private Map<String, Object> logs;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    
}
