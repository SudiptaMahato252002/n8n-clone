package com.example.n8n.exceptions;

public class UsernameNotFoundException extends RuntimeException
{
    UsernameNotFoundException(String message)
    {
        super(message);
    }
}
