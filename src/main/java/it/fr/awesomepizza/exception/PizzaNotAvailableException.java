package it.fr.awesomepizza.exception;

public class PizzaNotAvailableException extends RuntimeException {

    public PizzaNotAvailableException(String pizzaName) {
        super("La pizza '" + pizzaName + "' non è al momento disponibile");
    }
}
