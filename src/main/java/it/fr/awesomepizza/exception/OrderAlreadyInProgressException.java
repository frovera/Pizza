package it.fr.awesomepizza.exception;

public class OrderAlreadyInProgressException extends RuntimeException {

    public OrderAlreadyInProgressException() {
        super("Esiste già un ordine in lavorazione: completarlo prima di prenderne in carico un altro");
    }
}
