package ua.org.kse.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProductListDto {
    private List<ProductDto> items;
    private int page;
    private int size;
    private long totalItems;
    private int totalPages;
}