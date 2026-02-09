package com.example.n8n.services.Auth;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.n8n.models.dtos.request.LoginRequest;
import com.example.n8n.models.dtos.request.SignupRequest;
import com.example.n8n.models.dtos.response.AuthResponse;
import com.example.n8n.models.dtos.response.UserResponse;
import com.example.n8n.models.entity.RefreshToken;
import com.example.n8n.models.entity.User;
import com.example.n8n.repo.UserRepo;
import com.example.n8n.utils.JWTutils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService 
{
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JWTutils jwTutils;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenServcie refreshTokenServcie;

    @Transactional
    public AuthResponse signUpUser(SignupRequest request)
    {
        log.info("Registering new user: {}", request.getEmail());
        if(userRepo.existsByEmail(request.getEmail()))
        {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        Set<String> roles = new HashSet<>();
        roles.add("USER");

        User user=User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .enabled(true)
                .build();

        User savedUser = userRepo.save(user);
        log.info("User registered successfully: {}", savedUser.getEmail());

        UserDetails userDetails=org.springframework.security.core.userdetails.User.builder()
                                                        .username(savedUser.getUsername())
                                                        .password(savedUser.getPassword())
                                                        .roles("USER")
                                                        .build();
        String accessToken=jwTutils.generateAccessToken(userDetails);
        RefreshToken refreshToken=refreshTokenServcie.createRefreshToken(savedUser, userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .expiresIn(jwTutils.getAccessTokenExpirationInSeconds())
                .user(toUserResponse(savedUser))
                .build();                

    }

    @Transactional
    public AuthResponse loginUser(LoginRequest request)
    {
        log.info("User login attempt: {}", request.getEmail());
        Authentication authenticaion=authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user=userRepo.findByEmail(request.getEmail()).orElseThrow(()->new UsernameNotFoundException("User not found"));
        user.setLastLogin(LocalDateTime.now());

        userRepo.save(user);

        UserDetails userDetails=(UserDetails)authenticaion.getPrincipal();
        String accessToken=jwTutils.generateAccessToken(userDetails);
        RefreshToken refreshToken=refreshTokenServcie.createRefreshToken(user, userDetails);
        
        log.info("User logged in successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .expiresIn(jwTutils.getAccessTokenExpirationInSeconds())
                .user(toUserResponse(user))
                .build();               
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email)
    {
        User user=userRepo.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("User not found"));
        return toUserResponse(user);
    }

    public AuthResponse refreshAccessToken(String refreshTokenString)
    {
        log.info("Refreshing access token");
        RefreshToken refreshToken=refreshTokenServcie.verifyRefreshToken(refreshTokenString);

        User user=refreshToken.getUser();
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> "ROLE_" + role)
                        .toArray(String[]::new))
                .build();

        String newAccessToken=jwTutils.generateAccessToken(userDetails);
        log.info("Access token refreshed for user: {}", user.getEmail());
        
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshTokenString)
                .expiresIn(jwTutils.getAccessTokenExpirationInSeconds())
                .user(toUserResponse(user))
                .build();
        
    }

     @Transactional
    public void logout(String refreshTokenString) 
    {
        if (refreshTokenString != null && !refreshTokenString.isEmpty()) 
        {
            refreshTokenServcie.revokeRefreshToken(refreshTokenString);
        }
    }
    

    
    
    private UserResponse toUserResponse(User user) 
    {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .roles(user.getRoles())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }

}
