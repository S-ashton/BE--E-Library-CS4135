package com.elibrary.api_gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class GatewayJwtService {

    private final JwtParser jwtParser;

    public GatewayJwtService(JwtProperties jwtProperties) {
        SecretKey signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.jwtParser = Jwts.parser()
            .verifyWith(signingKey)
            .requireIssuer(jwtProperties.getIssuer())
            .build();
    }

    public Claims parseToken(String token) {
        return jwtParser.parseSignedClaims(token).getPayload();
    }
}
