package com.example.n8n.services.Node;

import com.example.n8n.enums.Platform;
import com.example.n8n.models.workflow.ExecutionContext;
import com.example.n8n.models.workflow.WorkflowNode;

public interface NodeExecutor 
{
    Platform getSuportedPlatform();
    Object execute(WorkflowNode node,ExecutionContext context,String credentialsId);
    
}
