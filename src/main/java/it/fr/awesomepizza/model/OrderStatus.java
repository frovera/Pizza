package it.fr.awesomepizza.model;

public enum OrderStatus {
    RECEIVED,
    IN_PROGRESS,
    READY;

    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case RECEIVED -> target == IN_PROGRESS;
            case IN_PROGRESS -> target == READY;
            case READY -> false;
        };
    }
}
