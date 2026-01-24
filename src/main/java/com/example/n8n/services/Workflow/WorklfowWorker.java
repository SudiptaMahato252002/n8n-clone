package com.example.n8n.services.Workflow;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.n8n.enums.ExecutionStatus;
import com.example.n8n.models.entity.Execution;
import com.example.n8n.models.entity.Workflow;
import com.example.n8n.models.workflow.ExecutionContext;
import com.example.n8n.models.workflow.WorkflowNode;
import com.example.n8n.repo.ExecutionRepo;
import com.example.n8n.repo.WorkflowRepo;
import com.example.n8n.services.Execution.ExecutionContextService;
import com.example.n8n.services.Execution.ExecutionService;
import com.example.n8n.services.Node.NodeExecutorService;
import com.example.n8n.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class WorklfowWorker 
{
    private Boolean workerEnabled=true;

    private final RedisTemplate<String,Object> redisTemplate;
    private static final String STREAM_KEY="workflow:executions";
    private static final String CONSUMER_GROUP="workflow-wokers";
    private static final String CONSUMER_NAME="Woker-1";

    private final JsonUtils jsonUtils;
    private final ExecutionService executionService;
    private final WorkflowRepo workflowRepo;
    private final ExecutionRepo executionRepo;
    private final ExecutionContextService executionContextService;
    private final ObjectMapper objectMapper;
    private final NodeExecutorService nodeExecutorService;

    @Scheduled(fixedDelay = 3000)
    public void pollAndExecute()
    {
        if(!workerEnabled)
        {
            return;
        }
        try 
        {
            ensureConsumerGroup();
            List<MapRecord<String,Object,Object>> records=redisTemplate.opsForStream().read(
                                        Consumer.from(CONSUMER_GROUP, CONSUMER_NAME),
                                        StreamReadOptions.empty().count(1).block(Duration.ofMillis(3000)),
                                        StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed()));

            if(records==null||records.isEmpty())
            {
                return;
            }

            for(MapRecord<String,Object,Object> record:records)
            {
                processRecord(record);
            }
            
        } 
        catch (Exception e) 
        {
            log.error("Error polling Redis stream", e);
        }

    }

    private void processRecord(MapRecord<String,Object,Object> record)
    {
        try 
        {
            Map<Object,Object> message=record.getValue();
            String executionId=(String)message.get("executionId");
            String workflowId=(String)message.get("workflowId");
            String jsonPaylaod=(String)message.get("triggerPayload");
            
            log.info("Processing execution: {}, workflow: {}", executionId, workflowId);
                
            Map<String,Object> payload=jsonUtils.jsonToMap(jsonPaylaod);

            executeWorkflow(executionId, workflowId, payload);

            redisTemplate.opsForStream().acknowledge(STREAM_KEY,CONSUMER_GROUP,record.getId());
                
        } 
        catch (Exception e) 
        {
            log.error("Error processing record", e);
        }
        
    }

    private void executeWorkflow(String executionId,String workflowId,Map<String,Object> payload)
    {
        try 
        {
            Workflow workflow=workflowRepo.findById(workflowId).orElse(null);
            if(workflow==null)
            {
                log.error("Workflow not found: {}", workflowId);
                executionService.updateExectionStatus(executionId, ExecutionStatus.FAILED);
                return;
            }
            Execution execution=executionRepo.findById(executionId).orElse(null);
            if(execution==null)
            {
                log.error("Execution not found: {}", executionId);
                return;
            }
            int tasksDone=0;
            executionService.updateExectionStatus(executionId, ExecutionStatus.RUNNING);
            ExecutionContext context=executionContextService.buildContext(payload);
            Map<String,Object> nodesMap=workflow.getNodes();
            Map<String,Object> connectionMap=workflow.getConnections();

            Map<String,Object> logs=new HashMap<>();
            Map<String,WorkflowNode> nodes=convertToWorkflowNode(nodesMap);

            List<String> executionOrder=getTopologicalOrder(nodes, connectionMap);

            for(String nodeId:executionOrder)
            {
                WorkflowNode workflowNode=nodes.get(nodeId);
                try 
                {
                    log.info("Executing node: {} ({})", nodeId, workflowNode.getType());   
                    Object result=nodeExecutorService.excuteNode(workflowNode,context);
                    executionContextService.addNodeResult(context, nodeId, result);
                    logs.put(nodeId,"Success");
                    Map<String,Object> outputs=execution.getOutputs();
                    if (outputs == null) 
                    {
                       outputs = new HashMap<>();
                    }
                    outputs.put(nodeId,result);
                    tasksDone++;

                    executionService.updateExecutionProgress(executionId, tasksDone, logs);
                    executionService.updateExecutionOutputs(executionId, outputs);
                    log.info("Node {} executed successfully", nodeId);  
                } 
                catch (Exception e) 
                {
                    log.error("Error executing node {}: {}", nodeId, e.getMessage(), e);

                    logs.put(nodeId, "Error: " + e.getMessage());
                    executionService.updateExecutionProgress(executionId, tasksDone, logs);
                    executionService.updateExectionStatus(executionId, ExecutionStatus.FAILED);
                    return;
                }

            }
            executionService.updateExectionStatus(executionId, ExecutionStatus.SUCCESS);
            log.info("Workflow execution completed successfully: {}", executionId);

        } 
        catch (Exception e) 
        {
            log.error("Error executing workflow: {}", e.getMessage(), e);
            executionService.updateExectionStatus(executionId, ExecutionStatus.FAILED);
        }
    }

    private Map<String,WorkflowNode> convertToWorkflowNode(Map<String,Object> nodesMap)
    {
        Map<String,WorkflowNode> nodes=new HashMap<>();
        for(Map.Entry<String,Object> entry:nodesMap.entrySet())
        {
            String nodeId=entry.getKey();
            Map<String,Object> nodeData=(Map<String,Object>)entry.getValue();
            WorkflowNode node=objectMapper.convertValue(nodeData,WorkflowNode.class);
            nodes.put(nodeId,node);
        }
        return nodes;
    }

    
    private List<String> getTopologicalOrder(Map<String,WorkflowNode> nodes,Map<String,Object> connections)
    {
        return new ArrayList<>(nodes.keySet());
    }
    
    
    private void ensureConsumerGroup()
    {
        try 
        {
            redisTemplate.opsForStream().createGroup(STREAM_KEY, CONSUMER_GROUP);
            log.info("Consumer group created: {}", CONSUMER_GROUP);       
        } 
        catch (Exception e) 
        {
            // TODO: handle exception
        }
    }
    
}
