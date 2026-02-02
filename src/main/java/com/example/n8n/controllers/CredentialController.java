package com.example.n8n.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.n8n.models.dtos.request.CreateCredentialsRequest;
import com.example.n8n.models.dtos.response.CredentialsResponse;
import com.example.n8n.services.Credentials.CredentialService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/credentials")
@RequiredArgsConstructor
public class CredentialController 
{

    private final CredentialService credentialService;
    
    
    @PostMapping
    public ResponseEntity<CredentialsResponse> createCredentials(CreateCredentialsRequest request)
    {
        CredentialsResponse response=credentialService.createCredentials(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<CredentialsResponse>> listCredentials(@PathVariable String userId) {
        List<CredentialsResponse> credentials = credentialService.getAllCredentialsForUser(userId);
        return ResponseEntity.ok(credentials);
    }

    @GetMapping("/{id}/{userId}")
    public ResponseEntity<CredentialsResponse> getCredentials(@PathVariable String id,@PathVariable String userId) {
        CredentialsResponse response = credentialService.getCredentialsById(id,userId);
        return ResponseEntity.ok(response);
    }

    
    @DeleteMapping("/{id}/{userId}")
    public ResponseEntity<Void> deleteCredentials(@PathVariable String id,
                                                @PathVariable String userId) 
    {
        credentialService.deleteCredentials(id,userId);
        return ResponseEntity.noContent().build();
    }
}
