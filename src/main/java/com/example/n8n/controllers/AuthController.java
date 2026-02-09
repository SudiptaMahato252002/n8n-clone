package com.example.n8n.controllers;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.n8n.models.dtos.request.LoginRequest;
import com.example.n8n.models.dtos.request.SignupRequest;
import com.example.n8n.models.dtos.response.AuthResponse;
import com.example.n8n.models.dtos.response.UserResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController 
{
    

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(@RequestBody SignupRequest request)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(null);

    }

    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request)
    {
       
        return ResponseEntity.status(HttpStatus.CREATED).body(null);

    }

}
