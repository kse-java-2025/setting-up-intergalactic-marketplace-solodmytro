package ua.org.kse.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ua.org.kse.domain.order.Order;
import ua.org.kse.domain.order.OrderStatus;
import ua.org.kse.domain.user.User;
import ua.org.kse.support.AbstractPostgresIT;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderRepositoryIT extends AbstractPostgresIT {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByOrderNumber_returnsPersistedOrder() {
        User user = userRepository.save(new User(null, "order@example.com", "Order User"));

        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber("ORD-42");
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(Instant.now());

        Order saved = orderRepository.save(order);

        Order found = orderRepository.findByOrderNumber("ORD-42").orElseThrow();

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getOrderNumber()).isEqualTo("ORD-42");
        assertThat(found.getUser().getEmail()).isEqualTo("order@example.com");
    }
}