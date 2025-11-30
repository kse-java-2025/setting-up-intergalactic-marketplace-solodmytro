package ua.org.kse.service;

import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.org.kse.domain.order.Order;
import ua.org.kse.error.NotFoundException;

import jakarta.persistence.EntityManager;

@Service
@RequiredArgsConstructor
public class OrderLookupService {
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public Order findByOrderNumberOrThrow(String orderNumber) {
        Session session = entityManager.unwrap(Session.class);

        return session.byNaturalId(Order.class)
            .using("orderNumber", orderNumber)
            .loadOptional()
            .orElseThrow(() -> new NotFoundException("Order with number " + orderNumber + " not found"));
    }
}