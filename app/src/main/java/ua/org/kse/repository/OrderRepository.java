package ua.org.kse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.org.kse.domain.order.Order;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
}