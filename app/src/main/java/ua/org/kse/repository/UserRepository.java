package ua.org.kse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.org.kse.domain.user.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}