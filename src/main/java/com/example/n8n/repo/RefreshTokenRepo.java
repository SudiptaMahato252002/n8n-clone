package com.example.n8n.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.n8n.models.entity.RefreshToken;

@Repository
public interface RefreshTokenRepo extends JpaRepository<RefreshToken,String>
{
    Optional<RefreshToken> findByToken(String token);

}
