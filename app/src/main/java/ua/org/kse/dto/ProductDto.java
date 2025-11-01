package ua.org.kse.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDto {
    private String id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private String cosmicTag;
}