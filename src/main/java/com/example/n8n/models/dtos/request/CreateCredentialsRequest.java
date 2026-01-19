package com.example.n8n.models.dtos.request;

import java.util.Map;

import com.example.n8n.enums.Platform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCredentialsRequest 
{
    private String title;
    private Platform platform;
    private Map<String, Object> credentials;
}
