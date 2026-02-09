package com.example.n8n.exceptions;

public class UsernameNotFoundException extends RuntimeException
{
    public UsernameNotFoundException(String message)
    {
        super(message);
    }
}
