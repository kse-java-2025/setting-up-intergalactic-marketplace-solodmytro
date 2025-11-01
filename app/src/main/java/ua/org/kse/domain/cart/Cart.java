package ua.org.kse.domain.cart;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class Cart {
    private String id;
    private List<String> productIds = new ArrayList<>();
}