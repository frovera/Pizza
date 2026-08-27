package it.fr.awesomepizza.repository;

import it.fr.awesomepizza.model.Pizza;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PizzaRepository extends JpaRepository<Pizza, Long> {

    List<Pizza> findByAvailableTrue();
}
