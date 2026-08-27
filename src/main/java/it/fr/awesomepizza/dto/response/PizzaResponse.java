package it.fr.awesomepizza.dto.response;

import java.math.BigDecimal;

public record PizzaResponse(
        Long id,
        String name,
        String description,
        BigDecimal price
) {
}
