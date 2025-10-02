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

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        var user = (UserDetails) auth.getPrincipal();
        var roles = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        String access = jwtService.generateAccessToken(user.getUsername(), roles);
        String refresh = jwtService.generateRefreshToken(user.getUsername());

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", access)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(60 * 15) // 15 Minuten
                .build();
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refresh)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(60 * 60 * 24 * 7) // 7 Tage
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(@RequestBody TokenRefreshRequest req) {
        var claims = jwtService.parse(req.refreshToken()).getPayload();
        if (!"refresh".equals(claims.get("typ"))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
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

    @GetMapping("/me")
    public ResponseEntity<?> me(@CookieValue(name = "accessToken", required = false) String accessToken) {
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No access token");
        }
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

    public record LoginRequest(String username, String password) {}
    public record TokenRefreshRequest(String refreshToken) {}

}
