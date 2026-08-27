package it.fr.awesomepizza.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String orderCode) {
        super("Nessun ordine trovato con codice: " + orderCode);
    }
}
