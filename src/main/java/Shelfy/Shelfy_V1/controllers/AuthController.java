package Shelfy.Shelfy_V1.controllers;

import Shelfy.Shelfy_V1.services.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

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
    public Map<String, String> login(@RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        var user = (UserDetails) auth.getPrincipal();
        var roles = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        String access = jwtService.generateAccessToken(user.getUsername(), roles);
        String refresh = jwtService.generateRefreshToken(user.getUsername());
        return Map.of("accessToken", access, "refreshToken", refresh);
    }

    @PostMapping("/refresh")
    public Map<String, String> refresh(@RequestBody TokenRefreshRequest req) {
        var claims = jwtService.parse(req.refreshToken()).getPayload();
        if (!"refresh".equals(claims.get("typ"))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
        String username = claims.getSubject();
        var user = users.loadUserByUsername(username); // optional DB check here
        var roles = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        String access = jwtService.generateAccessToken(username, roles);
        String refresh = jwtService.generateRefreshToken(username); // rotation
        return Map.of("accessToken", access, "refreshToken", refresh);
    }

    public record LoginRequest(String username, String password) {}
    public record TokenRefreshRequest(String refreshToken) {}

}
