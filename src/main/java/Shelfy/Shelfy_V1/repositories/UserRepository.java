package Shelfy.Shelfy_V1.repositories;

import Shelfy.Shelfy_V1.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;


// Repository interface for managing User entities.
public interface UserRepository extends JpaRepository<User, Long> {
    // Find a user by their username.
    Optional<User> findByUsername(String username);
}
