package Shelfy.Shelfy_V1.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


// Configuration class for setting up CORS (Cross-Origin Resource Sharing) settings.
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Define CORS configuration
        var cors = new CorsConfiguration();
        // Set allowed origins, methods, headers, and other settings
        cors.setAllowedOrigins(List.of("http://localhost:3000"));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cookie"));
        cors.setExposedHeaders(List.of("Set-Cookie"));
        cors.setAllowCredentials(true);
        // Apply CORS configuration to all paths
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }
}

