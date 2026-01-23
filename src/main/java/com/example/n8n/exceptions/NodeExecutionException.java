package com.example.n8n.exceptions;

public class NodeExecutionException extends RuntimeException 
{
    public NodeExecutionException(String message)
    {
        super(message);
    }
    public NodeExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
