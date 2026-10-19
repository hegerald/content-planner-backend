package at.contentplanner.backend.common;

/** Die angefragte Ressource existiert nicht. Wird als HTTP 404 gemeldet. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
