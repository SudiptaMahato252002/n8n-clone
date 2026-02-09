package com.example.n8n.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.n8n.models.entity.User;

public interface UserRepo extends JpaRepository<User,String> 
{
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
}
