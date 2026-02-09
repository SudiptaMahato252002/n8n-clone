package com.example.n8n.controllers;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.n8n.models.dtos.request.LoginRequest;
import com.example.n8n.models.dtos.request.RefreshTokenRequest;
import com.example.n8n.models.dtos.request.SignupRequest;
import com.example.n8n.models.dtos.response.AuthResponse;
import com.example.n8n.models.dtos.response.UserResponse;
import com.example.n8n.services.Auth.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController 
{
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(@RequestBody SignupRequest request)
    {
        AuthResponse response=authService.signUpUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request)
    {
        AuthResponse response=authService.loginUser(request);
        return ResponseEntity.ok(response);

    }

     @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() 
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        UserResponse user = authService.getCurrentUser(email);
        return ResponseEntity.ok(user);
    }



    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshAccessToken(@RequestBody RefreshTokenRequest request)
    {
        AuthResponse response=authService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String,String>> logout(@RequestBody(required = false) RefreshTokenRequest request)
    {
        if(request!=null&&request.getRefreshToken()!=null)
        {
            authService.logout(request.getRefreshToken());
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

}
