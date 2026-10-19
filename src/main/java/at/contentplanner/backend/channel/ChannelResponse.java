package at.contentplanner.backend.channel;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "Channel", description = "Ein Social-Media-Kanal mit seinem Zeichenlimit")
public record ChannelResponse(
        @Schema(description = "Technischer Schlüssel", examples = "LINKEDIN") Channel channel,
        @Schema(description = "Anzeigename", examples = "LinkedIn") String displayName,
        @Schema(description = "Maximale Zeichenanzahl des Post-Texts", examples = "3000") int maxLength) {

    public static ChannelResponse from(Channel channel) {
        return new ChannelResponse(channel, channel.displayName(), channel.maxLength());
    }
}
