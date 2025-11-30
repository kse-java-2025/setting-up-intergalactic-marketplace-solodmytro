package ua.org.kse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.org.kse.domain.product.Category;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
}