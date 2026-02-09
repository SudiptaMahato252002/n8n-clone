package com.example.n8n.services.Auth;

import java.time.LocalDateTime;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.n8n.models.entity.RefreshToken;
import com.example.n8n.models.entity.User;
import com.example.n8n.repo.RefreshTokenRepo;
import com.example.n8n.utils.JWTutils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServcie 
{
    private final RefreshTokenRepo refreshTokenRepo;
    private final JWTutils jwtUtils;

    @Transactional
    public RefreshToken createRefreshToken(User user,UserDetails userDetails)
    {
        String token=jwtUtils.generateRefreshToken(userDetails);
        
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtUtils.getRefreshTokenExpirationInSeconds());
        
        RefreshToken refreshToken=RefreshToken.builder()
            .token(token)
            .user(user)
            .expiresAt(expiresAt)
            .revoked(false)
            .build();

            return refreshTokenRepo.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken verifyRefreshToken(String token)
    {
        RefreshToken refreshToken=refreshTokenRepo.findByToken(token).orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));
        
        if (!refreshToken.isValid()) 
        {
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }

        if (!jwtUtils.validateRefreshToken(token)) 
        {
            throw new IllegalArgumentException("Invalid refresh token signature");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeRefreshToken(String token)
    {
        RefreshToken refreshToken=refreshTokenRepo.findByToken(token).orElseThrow(()-> new IllegalArgumentException("Refresh Token not found"));
        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepo.save(refreshToken);

        log.info("Refresh token revoked for user: {}", refreshToken.getUser().getEmail());
    }



    
}
