package ua.org.kse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ua.org.kse.domain.order.OrderItem;
import ua.org.kse.repository.projection.ProductSalesReportProjection;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("""
        select oi.product.id as productId,
               oi.product.name as productName,
               sum(oi.quantity) as totalQuantity
        from OrderItem oi
        group by oi.product.id, oi.product.name
        order by totalQuantity desc
        """)
    List<ProductSalesReportProjection> findTopSellingProducts();
}