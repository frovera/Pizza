package it.fr.awesomepizza.exception;

public class PizzaNotFoundException extends RuntimeException {

    public PizzaNotFoundException(Long pizzaId) {
        super("Nessuna pizza trovata con id: " + pizzaId);
    }
}
