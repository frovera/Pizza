package it.fr.awesomepizza.exception;

import it.fr.awesomepizza.model.OrderStatus;

public class InvalidOrderStateTransitionException extends RuntimeException {

    public InvalidOrderStateTransitionException(OrderStatus current, OrderStatus target) {
        super("Transizione non valida da " + current + " a " + target);
    }
}
