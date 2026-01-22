package com.example.n8n.services.Workflow;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.n8n.exceptions.InvalidWorkflowException;

@Service
public class WorkflowValidationService 
{
    public void validateWorkflow(Map<String,Object> nodes,Map<String,Object> connections)
    {
        if(nodes==null||nodes.isEmpty())
        {
            throw new InvalidWorkflowException("Workflow must contain at least one node.");
        }
        validateNodes(nodes);
        validateConnections(nodes,connections);
        validateNoCycles(nodes, connections);
        
    }

    private void validateNodes(Map<String,Object> nodes)
    {
        for(Map.Entry<String,Object> entry:nodes.entrySet())
        {
            String nodeId=entry.getKey();
            Object value=entry.getValue();
            if(!(value instanceof Map))
            {
                throw new InvalidWorkflowException("Node " + nodeId + " must be an object");
            }

            Map<String,Object> nodeProperties=(Map<String,Object>)value;
            if(!nodeProperties.containsKey("type"))
            {
                throw new InvalidWorkflowException("Node " + nodeId + " must have a type");
            }

        }
    }

    private void validateConnections(Map<String,Object> nodes,Map<String,Object> connections)
    {
        if (connections == null) 
        {
            return; 
        }

        for(Map.Entry<String, Object> entry:connections.entrySet())
        {
            String sourceNode=entry.getKey();
            if(!nodes.containsKey(sourceNode))
            {
                throw new InvalidWorkflowException("Connection references non-existent source node: " + sourceNode);
            }
            Object targetNode=entry.getValue();
            if(targetNode instanceof List)
            {
                List<String> targets=(List<String>)targetNode;
                for(String target:targets)
                {
                    if(!nodes.containsKey(connections))
                    {
                        throw new InvalidWorkflowException("Connection references non-existent target node: " + target);
                    }
                }
            }
        }
        
        // Implement connection validation logic here
    }

    private void validateNoCycles(Map<String,Object> nodes,Map<String,Object> connections)
    {
        if (connections == null) 
        {
            return; 
        }
        Set<String> visited=new HashSet<>();
        Set<String> recStack=new HashSet<>();
        for(String node:nodes.keySet())
        {
            if(hasCycle(node, connections, visited, recStack))
            {
                throw new InvalidWorkflowException("Workflow contains a cycle");
            }
        }
    }

    private boolean hasCycle(String node,Map<String,Object> connections,Set<String> visited,Set<String> recStack)
    {

        if(recStack.contains(node))
        {
            return true;
        }
        if(visited.contains(node))
        {
            return false;
        }

        visited.add(node);
        recStack.add(node); 
        Object targets=connections.get(node);
        if(targets instanceof List)
        {
            List<String> targetNodes=(List<String>)targets;
            for(String target:targetNodes)
            {
                if(hasCycle(target, connections, visited, recStack))
                {
                    return true;
                }
            }
        }

        recStack.remove(node);
        return false;
    }
    
}
