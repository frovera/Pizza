package it.fr.awesomepizza.config;

import it.fr.awesomepizza.model.Pizza;
import it.fr.awesomepizza.repository.PizzaRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class PizzaCatalogInitializer implements ApplicationRunner {

    private final PizzaRepository pizzaRepository;

    public PizzaCatalogInitializer(PizzaRepository pizzaRepository) {
        this.pizzaRepository = pizzaRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (pizzaRepository.count() == 0) {
            pizzaRepository.saveAll(List.of(
                    Pizza.of("Margherita", "Pomodoro, mozzarella, basilico", new BigDecimal("6.00")),
                    Pizza.of("Marinara", "Pomodoro, aglio, origano", new BigDecimal("5.00")),
                    Pizza.of("Diavola", "Pomodoro, mozzarella, salame piccante", new BigDecimal("7.50")),
                    Pizza.of("Capricciosa", "Pomodoro, mozzarella, funghi, carciofini, prosciutto, olive", new BigDecimal("8.50")),
                    Pizza.of("Quattro Stagioni", "Pomodoro, mozzarella, funghi, carciofini, prosciutto, olive", new BigDecimal("8.50")),
                    Pizza.of("Quattro Formaggi", "Mozzarella, gorgonzola, fontina, parmigiano", new BigDecimal("8.00"))
            ));
        }
    }
}
