package at.contentplanner.backend.post;

/**
 * Lebenszyklus eines Posts.
 *
 * <pre>
 * DRAFT ⇄ SCHEDULED → PUBLISHED
 * </pre>
 */
public enum PostStatus {
    /** Entwurf: frei bearbeitbar, Termin optional. */
    DRAFT,
    /** Eingeplant: Termin muss gesetzt sein und in der Zukunft liegen. */
    SCHEDULED,
    /** Veröffentlicht: Endzustand, nicht mehr änderbar oder löschbar. */
    PUBLISHED
}
