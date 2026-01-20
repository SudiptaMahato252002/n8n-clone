package com.example.n8n.exceptions;

public class WorkflowNotFoundException extends RuntimeException 
{
    public WorkflowNotFoundException(String message)
    {
        super(message);
    }
    
}
