package ua.org.kse.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ua.org.kse.domain.order.Order;
import ua.org.kse.domain.order.OrderItem;
import ua.org.kse.domain.order.OrderStatus;
import ua.org.kse.domain.product.Category;
import ua.org.kse.domain.product.CosmicTag;
import ua.org.kse.domain.product.Product;
import ua.org.kse.domain.user.User;
import ua.org.kse.repository.projection.ProductSalesReportProjection;
import ua.org.kse.support.AbstractPostgresIT;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderItemRepositoryIT extends AbstractPostgresIT {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    void findTopSellingProducts_aggregatesQuantitiesPerProduct() {
        User user = userRepository.save(new User(null, "report@example.com", "Report User"));
        Category snacks = categoryRepository.save(new Category(null, "Snacks"));

        Product chips = productRepository.save(
            new Product(null, "Comet Chips", "Spicy comet tail", snacks, BigDecimal.valueOf(3.50),
                new CosmicTag("star-snack")));
        Product nuts = productRepository.save(
            new Product(null, "Nebula Nuts", "Crunchy", snacks, BigDecimal.valueOf(4.00),
                new CosmicTag("galaxy-snack")));

        Order order1 = new Order();
        order1.setUser(user);
        order1.setOrderNumber("ORD-1");
        order1.setStatus(OrderStatus.CREATED);
        order1.setCreatedAt(Instant.now());
        order1 = orderRepository.save(order1);

        Order order2 = new Order();
        order2.setUser(user);
        order2.setOrderNumber("ORD-2");
        order2.setStatus(OrderStatus.CREATED);
        order2.setCreatedAt(Instant.now());
        order2 = orderRepository.save(order2);

        orderItemRepository.save(new OrderItem(null, order1, chips, 2, chips.getPrice()));
        orderItemRepository.save(new OrderItem(null, order1, nuts, 1, nuts.getPrice()));
        orderItemRepository.save(new OrderItem(null, order2, chips, 3, chips.getPrice()));

        List<ProductSalesReportProjection> report = orderItemRepository.findTopSellingProducts();

        assertThat(report).hasSize(2);

        ProductSalesReportProjection top = report.getFirst();
        assertThat(top.getProductId()).isEqualTo(chips.getId());
        assertThat(top.getProductName()).isEqualTo("Comet Chips");
        assertThat(top.getTotalQuantity()).isEqualTo(5L);
    }
}