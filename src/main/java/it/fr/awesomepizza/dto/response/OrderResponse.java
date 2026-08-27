package it.fr.awesomepizza.dto.response;

import it.fr.awesomepizza.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        String orderCode,
        OrderStatus status,
        Instant createdAt,
        Instant statusUpdatedAt,
        List<OrderItemResponse> items,
        BigDecimal totalPrice
) {
}
