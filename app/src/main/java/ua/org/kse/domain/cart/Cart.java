package ua.org.kse.domain.cart;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Cart {
    private String id;
    private List<String> productIds;
}