package ua.org.kse.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ua.org.kse.validation.CosmicWordCheck;
import java.math.BigDecimal;

@Data
public class ProductUpdateDto {
    @Size(max = 255)
    private String name;

    private String description;
    private String category;

    @DecimalMin("0.01")
    private BigDecimal price;

    @CosmicWordCheck
    private String cosmicTag;
}