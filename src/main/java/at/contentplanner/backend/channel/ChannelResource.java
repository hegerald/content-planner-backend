package at.contentplanner.backend.channel;

import java.util.Arrays;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/channels")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Channels", description = "Verfügbare Social-Media-Kanäle")
public class ChannelResource {

    @GET
    @Operation(operationId = "listChannels", summary = "Alle Kanäle inklusive Zeichenlimit")
    public List<ChannelResponse> list() {
        return Arrays.stream(Channel.values()).map(ChannelResponse::from).toList();
    }
}
