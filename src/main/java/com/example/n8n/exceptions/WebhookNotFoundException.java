package com.example.n8n.exceptions;

public class WebhookNotFoundException extends RuntimeException
{
    public WebhookNotFoundException(String message)
    {
        super(message);
    }
    
}
