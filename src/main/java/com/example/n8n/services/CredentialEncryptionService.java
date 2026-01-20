package com.example.n8n.services;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.n8n.utils.EncryptionUtils;
import com.example.n8n.utils.JsonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CredentialEncryptionService 
{
    private final EncryptionUtils encryptionUtils;
    private final JsonUtils jsonUtils;

    public String encryptCredentials(Map<String,Object> credentials)
    {  
        log.debug("Encrypting credentials data"); 
        String json=jsonUtils.ObjectToJson(credentials);
        return encryptionUtils.encrypt(json);
    }

    public Map<String,Object> decryptCredentials(String encryptedCredentials)
    {
        log.debug("Decrypting credentials data");
        String decryptedString=encryptionUtils.decrypt(encryptedCredentials);
        return jsonUtils.jsonToMap(decryptedString);
    }
}
