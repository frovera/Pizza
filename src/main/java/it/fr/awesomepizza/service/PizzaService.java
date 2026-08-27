package it.fr.awesomepizza.service;

import it.fr.awesomepizza.dto.response.PizzaResponse;
import it.fr.awesomepizza.model.Pizza;
import it.fr.awesomepizza.repository.PizzaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PizzaService {

    private final PizzaRepository pizzaRepository;

    public PizzaService(PizzaRepository pizzaRepository) {
        this.pizzaRepository = pizzaRepository;
    }

    public List<PizzaResponse> getAvailablePizzas() {
        return pizzaRepository.findByAvailableTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    private PizzaResponse toResponse(Pizza pizza) {
        return new PizzaResponse(pizza.getId(), pizza.getName(), pizza.getDescription(), pizza.getPrice());
    }
}
