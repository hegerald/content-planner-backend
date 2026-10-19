package at.contentplanner.backend.post;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import at.contentplanner.backend.channel.Channel;
import at.contentplanner.backend.common.BusinessRuleException;

/**
 * Alle fachlichen Regeln für Posts an einer Stelle.
 * <p>
 * Bewusst ohne Datenbankzugriff, damit die Regeln als schnelle Unit-Tests
 * (ohne Quarkus-Start) geprüft werden können.
 */
@ApplicationScoped
public class PostRules {

    private static final Map<PostStatus, List<PostStatus>> TRANSITIONS = Map.of(
            PostStatus.DRAFT, List.of(PostStatus.SCHEDULED),
            PostStatus.SCHEDULED, List.of(PostStatus.DRAFT, PostStatus.PUBLISHED),
            PostStatus.PUBLISHED, List.of());

    private final Clock clock;

    public PostRules(Clock clock) {
        this.clock = clock;
    }

    public Instant now() {
        return clock.instant();
    }

    /** Prüft Inhalt eines Posts gegen die Regeln seines Kanals. */
    public void checkContent(String text, Channel channel) {
        if (text.length() > channel.maxLength()) {
            throw new BusinessRuleException("TEXT_TOO_LONG",
                    "Der Text ist für %s zu lang (%d von max. %d Zeichen)."
                            .formatted(channel.displayName(), text.length(), channel.maxLength()));
        }
    }

    /** Prüft, ob ein Post im aktuellen Status geändert werden darf und der Termin passt. */
    public void checkUpdate(PostStatus currentStatus, Instant newScheduledAt) {
        if (!isEditable(currentStatus)) {
            throw new BusinessRuleException("POST_NOT_EDITABLE",
                    "Ein veröffentlichter Post kann nicht mehr geändert werden.");
        }
        if (currentStatus == PostStatus.SCHEDULED) {
            checkScheduledAtInFuture(newScheduledAt);
        }
    }

    /** Prüft einen Statuswechsel. */
    public void checkTransition(PostStatus from, PostStatus to, Instant scheduledAt) {
        if (!allowedTransitions(from).contains(to)) {
            throw new BusinessRuleException("INVALID_STATUS_TRANSITION",
                    "Statuswechsel von %s nach %s ist nicht erlaubt.".formatted(from, to));
        }
        if (to == PostStatus.SCHEDULED) {
            checkScheduledAtInFuture(scheduledAt);
        }
    }

    public void checkDelete(PostStatus status) {
        if (!isDeletable(status)) {
            throw new BusinessRuleException("POST_NOT_DELETABLE",
                    "Ein veröffentlichter Post kann nicht gelöscht werden.");
        }
    }

    public boolean isEditable(PostStatus status) {
        return status != PostStatus.PUBLISHED;
    }

    public boolean isDeletable(PostStatus status) {
        return status != PostStatus.PUBLISHED;
    }

    public List<PostStatus> allowedTransitions(PostStatus status) {
        return TRANSITIONS.get(status);
    }

    private void checkScheduledAtInFuture(Instant scheduledAt) {
        if (scheduledAt == null) {
            throw new BusinessRuleException("SCHEDULED_AT_REQUIRED",
                    "Für einen eingeplanten Post muss ein Veröffentlichungszeitpunkt gesetzt sein.");
        }
        if (!scheduledAt.isAfter(now())) {
            throw new BusinessRuleException("SCHEDULED_AT_IN_PAST",
                    "Der Veröffentlichungszeitpunkt muss in der Zukunft liegen.");
        }
    }
}
