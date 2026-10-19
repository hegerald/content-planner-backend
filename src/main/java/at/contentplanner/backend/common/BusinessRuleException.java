package at.contentplanner.backend.common;

/**
 * Eine fachliche Regel wurde verletzt. Wird als HTTP 422 an den Client gemeldet.
 * Der {@code code} ist stabil und maschinenlesbar, damit auch fremde (z. B. selbst
 * gebaute) Oberflächen gezielt darauf reagieren können.
 */
public class BusinessRuleException extends RuntimeException {

    private final String code;

    public BusinessRuleException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
