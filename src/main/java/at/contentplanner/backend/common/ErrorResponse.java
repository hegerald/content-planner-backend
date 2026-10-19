package at.contentplanner.backend.common;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "Error", description = "Einheitliches Fehlerformat aller Endpunkte")
public record ErrorResponse(
        @Schema(description = "HTTP-Statuscode", examples = "422") int status,
        @Schema(description = "Stabiler, maschinenlesbarer Fehlercode", examples = "TEXT_TOO_LONG") String code,
        @Schema(description = "Lesbare Beschreibung", examples = "Der Text ist für X zu lang (max. 280 Zeichen).") String message) {
}
