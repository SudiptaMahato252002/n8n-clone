package com.example.n8n.exceptions;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.n8n.models.dtos.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler 
{
    @ExceptionHandler(CredentialNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCredentialsNotFoundException(CredentialNotFoundException ex)
    {
        ErrorResponse response=ErrorResponse.builder()
                .message(ex.getMessage())
                .status(404)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(404).body(response);
    }
    
    @ExceptionHandler(InvalidWorkflowException.class)
    public ResponseEntity<ErrorResponse> handleInvalidWorkflowException(InvalidWorkflowException ex)
    {
        ErrorResponse response=ErrorResponse.builder()
                .message(ex.getMessage())
                .status(400)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(WorkflowNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWorkflowNotFoundException(WorkflowNotFoundException ex)
    {
        ErrorResponse response=ErrorResponse.builder()
                .message(ex.getMessage())
                .status(404)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(404).body(response);
    }
    
}
