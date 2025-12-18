package ua.org.kse.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ua.org.kse.validation.CosmicWordCheck;

import java.math.BigDecimal;

public record ProductCreateDto(@NotNull @Size(max = 255) String name, String description, String category,
                               @NotNull @DecimalMin("0.01") BigDecimal price,
                               @CosmicWordCheck @NotNull String cosmicTag) {
}