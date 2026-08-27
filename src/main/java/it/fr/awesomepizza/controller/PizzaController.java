package it.fr.awesomepizza.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.fr.awesomepizza.dto.response.PizzaResponse;
import it.fr.awesomepizza.service.PizzaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pizzas")
@Tag(name = "Catalogo", description = "Consultazione del catalogo pizze disponibili")
public class PizzaController {

    private final PizzaService pizzaService;

    public PizzaController(PizzaService pizzaService) {
        this.pizzaService = pizzaService;
    }

    @GetMapping
    @Operation(summary = "Elenca le pizze disponibili nel catalogo")
    public ResponseEntity<List<PizzaResponse>> getAvailablePizzas() {
        return ResponseEntity.ok(pizzaService.getAvailablePizzas());
    }
}
