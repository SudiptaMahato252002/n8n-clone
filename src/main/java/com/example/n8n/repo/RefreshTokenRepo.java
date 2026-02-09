package com.example.n8n.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.n8n.models.entity.RefreshToken;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken,String>
{
    Optional<RefreshToken> findByToken(String token);

}
