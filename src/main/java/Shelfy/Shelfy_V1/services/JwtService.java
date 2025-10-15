package Shelfy.Shelfy_V1.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;

import static io.jsonwebtoken.Jwts.SIG.HS256;

// Service class for generating and parsing JWT tokens.
@Component
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    // Constructor to initialize the JwtService with configuration values.
    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.issuer}") String issuer,
            @Value("${security.jwt.access-token-ttl}") Duration accessTtl,
            @Value("${security.jwt.refresh-token-ttl}") Duration refreshTtl) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.accessTtl = accessTtl;
        this.refreshTtl = refreshTtl;
    }

    // Generate a JWT access token with the specified subject and roles.
    public String generateAccessToken(String subject, Collection<String> roles) {
        Instant now = Instant.now();
        // Build and return the JWT token
        return Jwts.builder()
                // Set the subject (typically the username)
                .subject(subject)
                // Set the issuer of the token
                .issuer(issuer)
                // Custom claim to indicate token type
                .claim("roles", roles)
                // Custom claim to indicate token type
                .issuedAt(Date.from(now))
                // Set the expiration time for the token
                .expiration(Date.from(now.plus(accessTtl)))
                // Sign the token with the secret key and HS256 algorithm
                .signWith(key, HS256)
                // Compact the JWT to a URL-safe string
                .compact();
    }

    // Generate a JWT refresh token with the specified subject.
    public String generateRefreshToken(String subject) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .issuer(issuer)
                .claim("typ", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(refreshTtl)))
                .signWith(key, HS256)
                .compact();
    }

    // Parse and validate a JWT token, returning the claims if valid.
    public Jws<Claims> parse(String token) {
        // Parse and validate the JWT token
        return Jwts.parser()
                // Set the expected issuer for validation
                .requireIssuer(issuer)
                // Set the signing key for signature validation
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }

}