package at.contentplanner.backend.post;

import java.time.Instant;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import at.contentplanner.backend.channel.Channel;

/**
 * Antwort für einen Post.
 * <p>
 * {@code editable}, {@code deletable} und {@code allowedTransitions} werden vom Backend
 * berechnet. Jede Oberfläche – auch eine selbst gebaute – zeigt damit nur an, was erlaubt
 * ist, ohne die Regeln selbst kennen zu müssen.
 */
@Schema(name = "Post", description = "Ein geplanter Social-Media-Post inklusive der erlaubten Aktionen")
public record PostResponse(
        @Schema(description = "ID", examples = "1") Long id,
        @Schema(description = "Interner Titel") String title,
        @Schema(description = "Post-Text") String text,
        @Schema(description = "Ziel-Kanal") Channel channel,
        @Schema(description = "Aktueller Status") PostStatus status,
        @Schema(description = "Geplanter Veröffentlichungszeitpunkt (UTC)") Instant scheduledAt,
        @Schema(description = "Tatsächlicher Veröffentlichungszeitpunkt (UTC)") Instant publishedAt,
        @Schema(description = "Angelegt am (UTC)") Instant createdAt,
        @Schema(description = "Zuletzt geändert am (UTC)") Instant updatedAt,
        @Schema(description = "Darf der Post aktuell bearbeitet werden?") boolean editable,
        @Schema(description = "Darf der Post aktuell gelöscht werden?") boolean deletable,
        @Schema(description = "Status, in die aktuell gewechselt werden darf") List<PostStatus> allowedTransitions) {

    static PostResponse from(Post post, PostRules rules) {
        return new PostResponse(
                post.id,
                post.title,
                post.text,
                post.channel,
                post.status,
                post.scheduledAt,
                post.publishedAt,
                post.createdAt,
                post.updatedAt,
                rules.isEditable(post.status),
                rules.isDeletable(post.status),
                rules.allowedTransitions(post.status));
    }
}
