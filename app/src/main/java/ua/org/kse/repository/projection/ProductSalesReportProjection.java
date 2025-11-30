package ua.org.kse.repository.projection;

public interface ProductSalesReportProjection {
    Long getProductId();

    String getProductName();

    Long getTotalQuantity();
}