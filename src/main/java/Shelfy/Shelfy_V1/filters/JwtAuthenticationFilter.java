package Shelfy.Shelfy_V1.filters;

import Shelfy.Shelfy_V1.services.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;


// Filter that intercepts HTTP requests to authenticate users based on JWT tokens.
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    // Extracts and validates the JWT token from the request,
    // setting the authentication in the security context if valid.
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        // Extract token from Authorization header or cookies
        String token = null;
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        // Check if the Authorization header contains a Bearer token
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        } else if (request.getCookies() != null) { // Fallback to cookies if no Authorization header
            for (var cookie : request.getCookies()) { // Look for the accessToken cookie
                if ("accessToken".equals(cookie.getName())) { // Found the accessToken cookie
                    token = cookie.getValue(); // Extract the token value
                    break;
                }
            }
        }

        // If a token is found, validate and parse it
        if (token != null) {
            try {
                var jws = jwtService.parse(token);
                var body = jws.getPayload();
                String username = body.getSubject();
                // Extract roles from the token claims
                @SuppressWarnings("unchecked")
                List<String> roles = body.get("roles", List.class);
                // Map roles to Spring Security authorities
                List<SimpleGrantedAuthority> authorities =
                        // Handle case where roles might be null
                        (roles == null)
                                // No roles, assign empty list of authorities
                                ? Collections.<SimpleGrantedAuthority>emptyList()
                                // Map each role to a SimpleGrantedAuthority
                                : roles.stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());
                // Create an authentication token and set it in the security context
                var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
                // Set the authentication in the security context
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException ex) {
                // Do nothing if token is invalid
            }
        }
        // Continue the filter chain
        filterChain.doFilter(request, response);
    }

    // Skip filtering for login endpoint
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth/login");
    }
}
