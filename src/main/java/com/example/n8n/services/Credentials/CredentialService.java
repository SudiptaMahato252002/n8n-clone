package com.example.n8n.services.Credentials;



import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.n8n.exceptions.CredentialNotFoundException;
import com.example.n8n.models.dtos.request.CreateCredentialsRequest;
import com.example.n8n.models.dtos.response.CredentialsResponse;
import com.example.n8n.models.entity.Credentials;
import com.example.n8n.repo.CredentialsRepo;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CredentialService 
{
    private final CredentialEncryptionService credentialEncryptionService;
    private final CredentialsRepo repo;    

    @Transactional
    public CredentialsResponse createCredentials(CreateCredentialsRequest request)
    {
        String encryptedCredentials=credentialEncryptionService.encryptCredentials(request.getCredentials());
        Credentials credential=Credentials.builder()
                .title(request.getTitle())
                .platform(request.getPlatform())
                .credentials(encryptedCredentials)
                .build();
        Credentials saved=repo.save(credential);
        log.info("Credentials created with id: {}", saved.getId());
        
        return toResponse(saved);
    }

    @Transactional
    public void deleteCredentials(String id,String userId)
    {
        log.info("Deleting credentials: {} for user: {}", id, userId);
        Credentials credential=repo.findByIdAndUserId(id,userId).orElseThrow(()->new CredentialNotFoundException("Credentials not found with id: " +id));
        repo.delete(credential);
        log.info("Credentials deleted with id: {}", id);
    }

    @Transactional(readOnly = true)
    public CredentialsResponse getCredentialsById(String id,String userId)
    {
        log.info("Fetching credentials: {} for user: {}", id, userId);
        Credentials credential=repo.findByIdAndUserId(id,userId).orElseThrow(()->new CredentialNotFoundException("Credentials not found with id: " +id));
        return toResponse(credential);
    }

    @Transactional(readOnly = true)
    public List<CredentialsResponse> getAllCredentialsForUser(String userId)
    {
        log.info("Fetching all credentials for user: {}", userId);
        List<Credentials> credentialsList=repo.findByUserId(userId);
        return credentialsList.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Map<String,Object> getDecryptedCredentials(String id,String userId)
    {
        log.debug("Getting decrypted data for credential: {}",id);
        Credentials credential=repo.findByIdAndUserId(id, userId).orElseThrow(()->new CredentialNotFoundException("Credentials not found with id: " +id));
        return credentialEncryptionService.decryptCredentials(credential.getCredentials());
    }
    
    private CredentialsResponse toResponse(Credentials credentials) {
        return CredentialsResponse.builder()
                .id(credentials.getId())
                .userId(credentials.getUserId())
                .title(credentials.getTitle())
                .platform(credentials.getPlatform())
                .createdAt(credentials.getCreatedAt())
                .updatedAt(credentials.getUpdatedAt())
                .build();
    }

}
