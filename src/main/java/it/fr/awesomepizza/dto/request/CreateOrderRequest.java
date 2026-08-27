package it.fr.awesomepizza.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotEmpty(message = "L'ordine deve contenere almeno una pizza")
        @Valid
        List<OrderItemRequest> items
) {
}
