package it.fr.awesomepizza.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        String pizzaName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
