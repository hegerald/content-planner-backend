package at.contentplanner.backend.post;

import jakarta.validation.constraints.NotNull;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "StatusChangeRequest", description = "Gewünschter Zielstatus eines Posts")
public record StatusChangeRequest(
        @NotNull @Schema(description = "Zielstatus", examples = "SCHEDULED") PostStatus targetStatus) {
}
