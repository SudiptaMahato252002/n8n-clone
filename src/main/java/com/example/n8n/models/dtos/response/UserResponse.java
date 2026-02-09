package com.example.n8n.models.dtos.response;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse 
{
    private String id;
    private String email;
    private String name;
    private Set<String> roles;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}
