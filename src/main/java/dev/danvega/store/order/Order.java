package dev.danvega.store.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Order(
        String orderId,
        String customerId,
        BigDecimal amount,
        OrderStatus status,
        LocalDateTime timestamp
) {

    public enum OrderStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
}
