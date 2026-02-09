package com.example.n8n.utils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JWTutils 
{
    private static final String secretKey="a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2";
    private long Expiration=1000*60*60;
    private long accessTokenExpiration=900000;
    private long refreshTokenExpiration=604800000;

    private Claims extractAllClaims(String token)
    {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build().parseClaimsJws(token)
                .getBody();
    }

    private <T> T extractClaim(String token,Function<Claims,T> claimResolver)
    {
        final Claims claims=extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    public String extractUsername(String token)
    {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token)
    {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public String extractTokenType(String token)
    {
        return extractClaim(token, claims->claims.get("type",String.class));
    }


    public String generateAccessToken(UserDetails userDetails)
    {
        Map<String,Object> claims=new HashMap<>();
        claims.put("type", "access");
        return buildToken(claims, userDetails.getUsername(),accessTokenExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails)
    {
        Map<String,Object> claims=new HashMap<>();
        claims.put("type","refresh");
        return buildToken(claims, userDetails.getUsername(), refreshTokenExpiration);
    }
    
    public String generateAccessToken(Map<String, Object> extraClaims, UserDetails userDetails) 
    {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("type", "access");
        return buildToken(extraClaims, userDetails.getUsername(), accessTokenExpiration);
    }

    public long getExpirationTime() 
    {
        return Expiration;
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            String subject,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }
    
    private Key getSigningKey()
    {
        byte[] keyBytes=secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokeValid(String token,UserDetails userDetails)
    {
        try 
        {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())&&!isTokenExpired(token));
            
        } 
        catch (Exception e) 
        {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean isTokenExpired(String token)
    {
        return extractExpiration(token).before(new Date());
    }
    
    
    public Boolean validateAccessToken(String token,UserDetails userDetails)
    {
        try 
        {
            final String username=extractUsername(token);
            final String tokenType=extractTokenType(token);

            return username.equals(userDetails.getUsername()) && !isTokenExpired(token) && "access".equals(tokenType);
            
        } 
        catch (Exception e) 
        {
            log.error("Access token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Boolean validateRefreshToken(String token)
    {
        try 
        {
            String tokenType=extractTokenType(token);
            return !isTokenExpired(token) && "refresh".equals(tokenType);
            
        } 
        catch (Exception e) 
        {
            log.error("Refresh token validation failed: {}", e.getMessage());
            return false;
        }
    }



    public Boolean validateToken(String token)
    {
        try 
        {
            extractAllClaims(token);
            return !isTokenExpired(token);
            
        } 
        catch (Exception e) 
        {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }

    }

    public Long getAccessTokenExpirationInSeconds()
    {
        return accessTokenExpiration/1000;
    }

    public Long getRefreshTokenExpirationInSeconds()
    {
        return refreshTokenExpiration;
    }

}
