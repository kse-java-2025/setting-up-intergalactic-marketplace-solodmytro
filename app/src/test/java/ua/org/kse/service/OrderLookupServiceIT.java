package ua.org.kse.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ua.org.kse.domain.order.Order;
import ua.org.kse.domain.order.OrderStatus;
import ua.org.kse.domain.user.User;
import ua.org.kse.error.NotFoundException;
import ua.org.kse.repository.OrderRepository;
import ua.org.kse.repository.UserRepository;
import ua.org.kse.support.AbstractPostgresIT;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class OrderLookupServiceIT extends AbstractPostgresIT {
    @Autowired
    private OrderLookupService orderLookupService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByOrderNumberOrThrow_returnsPersistedOrder() {
        User user = userRepository.save(new User(null, "lookup@example.com", "Lookup User"));

        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber("ORD-99");
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(Instant.now());

        Order saved = orderRepository.save(order);

        Order found = orderLookupService.findByOrderNumberOrThrow("ORD-99");

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getOrderNumber()).isEqualTo("ORD-99");
        assertThat(found.getUser().getEmail()).isEqualTo("lookup@example.com");
    }

    @Test
    void findByOrderNumberOrThrow_whenNotExisting_throwsNotFound() {
        assertThrows(NotFoundException.class,
            () -> orderLookupService.findByOrderNumberOrThrow("NON-EXISTING"));
    }
}