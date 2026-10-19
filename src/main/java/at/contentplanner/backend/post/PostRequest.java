package at.contentplanner.backend.post;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import at.contentplanner.backend.channel.Channel;

@Schema(name = "PostRequest", description = "Daten zum Anlegen oder Ändern eines Posts. Der Status wird nicht hier, sondern über den Status-Endpunkt geändert.")
public record PostRequest(
        @NotBlank @Size(max = 100)
        @Schema(description = "Interner Titel", examples = "Herbst-Kampagne Teaser") String title,

        @NotBlank
        @Schema(description = "Post-Text. Maximale Länge hängt vom Kanal ab, siehe /api/channels.", examples = "Bald ist es so weit …") String text,

        @NotNull
        @Schema(description = "Ziel-Kanal", examples = "LINKEDIN") Channel channel,

        @Schema(description = "Geplanter Veröffentlichungszeitpunkt (ISO-8601, UTC). Optional für Entwürfe.", examples = "2026-11-01T09:00:00Z") Instant scheduledAt) {
}
