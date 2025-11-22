package ua.org.kse.domain.order;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class Order {
    private String id;
    private List<String> productIds = new ArrayList<>();
    private OrderStatus status = OrderStatus.CREATED;
}