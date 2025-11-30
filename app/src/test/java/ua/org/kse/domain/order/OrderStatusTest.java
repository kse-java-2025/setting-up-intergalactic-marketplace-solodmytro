package ua.org.kse.domain.order;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {
    @Test
    void orderStatus_valuesAreAvailable() {
        OrderStatus[] values = OrderStatus.values();

        assertThat(values).contains(
            OrderStatus.CREATED,
            OrderStatus.PAID,
            OrderStatus.SHIPPED,
            OrderStatus.CANCELLED
        );
    }
}