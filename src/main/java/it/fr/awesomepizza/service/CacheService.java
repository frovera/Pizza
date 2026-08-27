package it.fr.awesomepizza.service;

import it.fr.awesomepizza.model.Pizza;
import it.fr.awesomepizza.repository.PizzaRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CacheService {

    private final PizzaRepository pizzaRepository;

    private final Map<Long, Pizza> pizzaById = new ConcurrentHashMap<>();

    public CacheService(PizzaRepository pizzaRepository) {
        this.pizzaRepository = pizzaRepository;
    }

    public Optional<Pizza> findPizzaById(Long id) {

        // TODO se una pizza viene impostata come non disponibile, va rimossa da pizzaById (evict),
        //  altrimenti resta un dirty value

        Pizza cached = pizzaById.get(id);
        if (cached != null) {
            return Optional.of(cached);
        }

        Optional<Pizza> pizzaOpt = pizzaRepository.findById(id).stream().findFirst();
        if (pizzaOpt.isEmpty()) {
            return Optional.empty();
        }

        pizzaById.put(id, pizzaOpt.get());
        return pizzaOpt;
    }
}
