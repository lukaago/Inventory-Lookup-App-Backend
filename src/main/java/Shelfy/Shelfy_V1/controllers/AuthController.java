package Shelfy.Shelfy_V1.controllers;

import Shelfy.Shelfy_V1.services.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;

import java.util.Map;

// Controller for handling authentication-related endpoints.
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserDetailsService users;

    public AuthController(AuthenticationConfiguration config, JwtService jwtService, UserDetailsService userDetailsService) throws Exception {
        this.authManager = config.getAuthenticationManager();
        this.jwtService = jwtService;
        this.users = userDetailsService;
    }

    // Cookies are sameSite=lax such that the frontend is forced to go through a proxy to keep the cookies during site changes
    // Endpoint for user login. Authenticates the user and returns JWT tokens in HttpOnly cookies.
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest req) {
        // Authenticate the user using the provided username and password
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        // If authentication is successful, generate JWT tokens
        var user = (UserDetails) auth.getPrincipal();
        var roles = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        String access = jwtService.generateAccessToken(user.getUsername(), roles);
        String refresh = jwtService.generateRefreshToken(user.getUsername());

        // Create HttpOnly cookies for the access and refresh tokens
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", access)
                // HttpOnly cookie to prevent JavaScript access
                .httpOnly(true)
                // In production, set to true to ensure cookies are only sent over HTTPS
                .secure(true)
                // Cookie valid for the entire site
                .path("/")
                // Mititate CSRF risks
                .sameSite("Lax")
                // Access token valid for 15 minutes
                .maxAge(60 * 15) // 15 Minuten
                // Build the cookie
                .build();
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refresh)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(60 * 60 * 24 * 7) // 7 Tage
                .build();

        // Return the cookies in the response headers
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }

    // Endpoint for user logout. Clears the JWT cookies.
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Clear the cookies by setting their maxAge to 0
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }

    // Endpoint for refreshing JWT tokens using a valid refresh token.
    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(@CookieValue(name="refreshToken", required=false) String token) {
        // Validate the provided refresh token
        if (token == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        var claims = jwtService.parse(token).getPayload();
        if (!"refresh".equals(claims.get("typ"))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
        // Generate new access and refresh tokens
        String username = claims.getSubject();
        var user = users.loadUserByUsername(username);
        var roles = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        String access = jwtService.generateAccessToken(username, roles);
        String refresh = jwtService.generateRefreshToken(username);

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", access)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(60 * 15)
                .build();
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refresh)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(60 * 60 * 24 * 7)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }

    // Endpoint to get information about the currently authenticated user.
    @GetMapping("/me")
    public ResponseEntity<?> me(@CookieValue(name = "accessToken", required = false) String accessToken) {
        // If no access token is provided, return 401 Unauthorized
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No access token");
        }
        // Parse and validate the access token
        try {
            var claims = jwtService.parse(accessToken).getPayload();
            String username = claims.getSubject();
            var user = users.loadUserByUsername(username);
            var roles = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
            return ResponseEntity.ok(Map.of(
                "username", username,
                "roles", roles
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }
    }

    // Record classes for login and token refresh requests
    public record LoginRequest(String username, String password) {}
    public record TokenRefreshRequest(String refreshToken) {}

}
