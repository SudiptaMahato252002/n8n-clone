package com.example.n8n.exceptions;

public class InvalidWorkflowException extends RuntimeException
{
    public InvalidWorkflowException(String message)
    {
        super(message);
    }
    
}
