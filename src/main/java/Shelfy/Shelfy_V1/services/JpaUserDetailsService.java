package Shelfy.Shelfy_V1.services;

import Shelfy.Shelfy_V1.repositories.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

// Service class for loading user details from the database for authentication.
@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public JpaUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Load user details by username for authentication purposes.
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        // Fetch the user from the database
        var u = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        // Map the user's roles to Spring Security authorities
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN")); // <- the key part

        // Return a UserDetails object with the user's information and authorities
        return new User(
                u.getUsername(), u.getPassword(),
                u.isEnabled(), true, true, true,
                authorities
        );
    }
}
