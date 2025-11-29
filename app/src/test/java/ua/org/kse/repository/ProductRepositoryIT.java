package ua.org.kse.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ua.org.kse.domain.product.Category;
import ua.org.kse.domain.product.CosmicTag;
import ua.org.kse.domain.product.Product;
import ua.org.kse.repository.projection.ProductSummaryProjection;
import ua.org.kse.support.AbstractPostgresIT;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProductRepositoryIT extends AbstractPostgresIT {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void saveAndFindById_persistsAndLoadsEntity() {
        Category category = categoryRepository.save(new Category(null, "Food"));

        Product product = new Product();
        product.setName("Moon Cheese");
        product.setDescription("Cheese from the dark side of the moon");
        product.setCategory(category);
        product.setPrice(BigDecimal.valueOf(19.99));
        product.setCosmicTag(new CosmicTag("star-delicacy"));

        Product saved = productRepository.save(product);

        assertThat(saved.getId()).isNotNull();

        Product found = productRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getName()).isEqualTo("Moon Cheese");
        assertThat(found.getCategory().getName()).isEqualTo("Food");
        assertThat(found.getCosmicTag().value()).isEqualTo("star-delicacy");
    }

    @Test
    void findSummariesByCategory_filtersByCategoryAndSortsByPriceDesc() {
        Category food = categoryRepository.save(new Category(null, "Food"));
        Category gadgets = categoryRepository.save(new Category(null, "Gadgets"));

        productRepository.save(new Product(null, "Cheap Snack", "Budget", food, BigDecimal.valueOf(5.00),
            new CosmicTag("star-snack")));
        productRepository.save(new Product(null, "Premium Snack", "Premium", food, BigDecimal.valueOf(25.00),
            new CosmicTag("star-premium")));
        productRepository.save(new Product(null, "Laser Pointer", "Toy", gadgets, BigDecimal.valueOf(15.00),
            new CosmicTag("galaxy-gadget")));

        List<ProductSummaryProjection> foodSummaries = productRepository.findSummariesByCategory("Food");

        assertThat(foodSummaries).hasSize(2);
        assertThat(foodSummaries.get(0).getName()).isEqualTo("Premium Snack");
        assertThat(foodSummaries.get(0).getPrice()).isEqualByComparingTo("25.00");
        assertThat(foodSummaries.get(1).getName()).isEqualTo("Cheap Snack");
        assertThat(foodSummaries.get(1).getPrice()).isEqualByComparingTo("5.00");

        List<ProductSummaryProjection> allSummaries = productRepository.findSummariesByCategory(null);
        assertThat(allSummaries).hasSize(3);
    }
}