package com.example.n8n.exceptions;

public class CredentialNotFoundException extends RuntimeException
{
    public CredentialNotFoundException(String message)
    {
        super(message);
    }
    
}
