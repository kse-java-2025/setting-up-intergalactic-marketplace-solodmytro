package ua.org.kse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ua.org.kse.domain.product.Product;
import ua.org.kse.repository.projection.ProductSummaryProjection;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("""
        select p.id as id,
               p.name as name,
               p.category.name as categoryName,
               p.price as price
        from Product p
        where (:categoryName is null or p.category.name = :categoryName)
        order by p.price desc
        """)
    List<ProductSummaryProjection> findSummariesByCategory(String categoryName);
}