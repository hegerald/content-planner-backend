package at.contentplanner.backend.post;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import at.contentplanner.backend.channel.Channel;
import at.contentplanner.backend.common.ErrorResponse;

@Path("/api/posts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Posts", description = "Geplante Social-Media-Posts verwalten")
public class PostResource {

    private final PostService service;

    public PostResource(PostService service) {
        this.service = service;
    }

    @GET
    @Operation(operationId = "listPosts", summary = "Posts auflisten, optional gefiltert",
            description = "Sortiert nach geplantem Zeitpunkt; Posts ohne Termin stehen am Ende.")
    public List<PostResponse> list(
            @QueryParam("status") @Parameter(description = "Nur Posts mit diesem Status") PostStatus status,
            @QueryParam("channel") @Parameter(description = "Nur Posts dieses Kanals") Channel channel) {
        return service.list(status, channel);
    }

    @GET
    @Path("/{id}")
    @Operation(operationId = "getPost", summary = "Einzelnen Post laden")
    @APIResponse(responseCode = "200", description = "Gefunden",
            content = @Content(schema = @Schema(implementation = PostResponse.class)))
    @APIResponse(responseCode = "404", description = "Nicht gefunden",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PostResponse get(@PathParam("id") long id) {
        return service.get(id);
    }

    @POST
    @Operation(operationId = "createPost", summary = "Neuen Post als Entwurf anlegen")
    @APIResponse(responseCode = "201", description = "Angelegt",
            content = @Content(schema = @Schema(implementation = PostResponse.class)))
    @APIResponse(responseCode = "400", description = "Eingabe ungültig",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "422", description = "Fachliche Regel verletzt (z. B. Text zu lang)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public Response create(@Valid @NotNull PostRequest request) {
        PostResponse created = service.create(request);
        return Response.created(URI.create("/api/posts/" + created.id())).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(operationId = "updatePost", summary = "Inhalt und Termin eines Posts ändern")
    @APIResponse(responseCode = "200", description = "Geändert",
            content = @Content(schema = @Schema(implementation = PostResponse.class)))
    @APIResponse(responseCode = "400", description = "Eingabe ungültig",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "404", description = "Nicht gefunden",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "422", description = "Fachliche Regel verletzt",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PostResponse update(@PathParam("id") long id, @Valid @NotNull PostRequest request) {
        return service.update(id, request);
    }

    @POST
    @Path("/{id}/status")
    @Operation(operationId = "changePostStatus", summary = "Status eines Posts wechseln",
            description = "Erlaubt: DRAFT → SCHEDULED, SCHEDULED → DRAFT, SCHEDULED → PUBLISHED.")
    @APIResponse(responseCode = "200", description = "Status gewechselt",
            content = @Content(schema = @Schema(implementation = PostResponse.class)))
    @APIResponse(responseCode = "404", description = "Nicht gefunden",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "422", description = "Statuswechsel nicht erlaubt",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PostResponse changeStatus(@PathParam("id") long id, @Valid @NotNull StatusChangeRequest request) {
        return service.changeStatus(id, request.targetStatus());
    }

    @DELETE
    @Path("/{id}")
    @Operation(operationId = "deletePost", summary = "Post löschen")
    @APIResponse(responseCode = "204", description = "Gelöscht")
    @APIResponse(responseCode = "404", description = "Nicht gefunden",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "422", description = "Veröffentlichte Posts können nicht gelöscht werden",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public void delete(@PathParam("id") long id) {
        service.delete(id);
    }
}
