package com.example.n8n.models.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse 
{
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String type = "Bearer";
    private UserResponse user;
    private Long expiresIn;

    public AuthResponse(String accessToken,String refreshToken, UserResponse user) {
        this.accessToken = accessToken;
        this.refreshToken= refreshToken;
        this.user = user;
        this.type = "Bearer";
    }
    
}
