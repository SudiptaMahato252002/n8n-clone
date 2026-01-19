package com.example.n8n.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EncryptionUtils 
{
    private String Algorithm="AES";
    private SecretKey secretKey;
    public String encrypt(String data)
    {
        try 
        {
            Cipher cipher=Cipher.getInstance(Algorithm);
            cipher.init(Cipher.ENCRYPT_MODE,secretKey);
            byte[] encryptedBytes=cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } 
        catch (Exception e) 
        {
            log.error("Error encrypting data", e);
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    public String decrypt(String encryptedText)
    {
        try 
        {
            Cipher cipher=Cipher.getInstance(Algorithm);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes=Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes=cipher.doFinal(decodedBytes);
            return new String(decryptedBytes,StandardCharsets.UTF_8);
    
        } 
        catch (Exception e) 
        {
            log.error("Error decrypting data", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }
    
}
