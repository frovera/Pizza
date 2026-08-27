package it.fr.awesomepizza.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull(message = "pizzaId è obbligatorio")
        Long pizzaId,

        @Min(value = 1, message = "quantity deve essere almeno 1")
        int quantity
) {
}
