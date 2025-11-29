package ua.org.kse.repository.projection;

import java.math.BigDecimal;

public interface ProductSummaryProjection {
    Long getId();

    String getName();

    String getCategoryName();

    BigDecimal getPrice();
}