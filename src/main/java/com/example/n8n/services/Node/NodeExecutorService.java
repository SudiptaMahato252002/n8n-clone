package com.example.n8n.services.Node;

import org.springframework.stereotype.Service;

import com.example.n8n.exceptions.NodeExecutionException;
import com.example.n8n.models.workflow.ExecutionContext;
import com.example.n8n.models.workflow.WorkflowNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NodeExecutorService 
{
    private final NodeRegistry registry;

    public Object excuteNode(WorkflowNode node,ExecutionContext context)
    {
        try 
        {
            if (node.getCredentialsId() == null || node.getCredentialsId().isEmpty()) {
                throw new NodeExecutionException("Node " + node.getId() + " has no credentials configured");
            }
            NodeExecutor executor=registry.getExecutor(node.getType());
            
            long startTime=System.currentTimeMillis();
            Object result=executor.execute(node, context, node.getCredentialsId());
            long duration=System.currentTimeMillis()-startTime;

            log.info("Node {} executed successfully in {}ms", node.getId(), duration);
            return result;

        }
        catch (NodeExecutionException e) 
        {
            log.error("Node execution failed: {}", e.getMessage());
            throw e;
        } 
        catch (Exception e) 
        {
            log.error("Unexpected error executing node {}: {}", node.getId(), e.getMessage(), e);
            throw new NodeExecutionException("Node execution failed: " + e.getMessage(), e);
        }
    }
}
