package com.mantasguajiras.backend.security.jwt;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.mantasguajiras.backend.user.entity.User;

@Component
public class JwtTokenProvider {

    private final JwtService jwtService;

    public JwtTokenProvider(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public String generateToken(User user) {
        return jwtService.generateToken(user);
    }

    public String getUsernameFromToken(String token) {
        return jwtService.extractUsername(token);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        return jwtService.isTokenValid(
                token,
                userDetails.getUsername()
        );
    }
}