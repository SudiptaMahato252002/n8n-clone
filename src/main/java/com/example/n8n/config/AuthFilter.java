package com.example.n8n.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.n8n.services.Auth.CustomUserDetailsService;
import com.example.n8n.utils.JWTutils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class AuthFilter extends OncePerRequestFilter 
{
    private final JWTutils jwtUtils;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)throws ServletException, IOException 
    {
        try 
        {
            String token=getJwtFromRequest(request);

            if(StringUtils.hasText(token)&& jwtUtils.validateToken(token))
            {
                String username=jwtUtils.extractUsername(token);
                UserDetails userDetails=customUserDetailsService.loadUserByUsername(username);
                if(jwtUtils.validateAccessToken(token, userDetails))
                {
                    UsernamePasswordAuthenticationToken authentication=new UsernamePasswordAuthenticationToken(username, null,userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Set authentication for user: {}", username);
                }
                else 
                {
                    log.warn("Invalid access token or refresh token used for authentication");
                }   
            }
            
        } 
        catch (Exception e) 
        {
            log.error("Could not set user authentication in security context", e);
        }
        
        filterChain.doFilter(request, response);
    }
    private String getJwtFromRequest(HttpServletRequest request)
    {
        String bearerToken=request.getHeader("Authorization");
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer "))
        {
            return bearerToken.substring(7);
        }
        return null;
    }
    
}
