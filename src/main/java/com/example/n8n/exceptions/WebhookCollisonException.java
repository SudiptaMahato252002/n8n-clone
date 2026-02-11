package com.example.n8n.exceptions;

public class WebhookCollisonException extends RuntimeException 
{
       public WebhookCollisonException(String message)
    {
        super(message);
    }
}
