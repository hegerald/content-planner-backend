package at.contentplanner.backend.common;

import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

/**
 * Übersetzt Exceptions in das einheitliche {@link ErrorResponse}-Format.
 */
public class ExceptionMappers {

    @ServerExceptionMapper
    public Response businessRule(BusinessRuleException e) {
        return error(422, e.code(), e.getMessage());
    }

    @ServerExceptionMapper
    public Response notFound(NotFoundException e) {
        return error(404, "NOT_FOUND", e.getMessage());
    }

    /**
     * Ersetzt den eingebauten Validierungs-Report von Quarkus durch unser einheitliches Format.
     */
    @ServerExceptionMapper
    public Response validation(ValidationException e) {
        String message = e.getMessage();
        if (e instanceof ConstraintViolationException cve) {
            message = cve.getConstraintViolations().stream()
                    .map(v -> lastSegment(v.getPropertyPath().toString()) + ": " + v.getMessage())
                    .sorted()
                    .collect(Collectors.joining("; "));
        }
        return error(400, "VALIDATION_FAILED", message);
    }

    private static String lastSegment(String path) {
        int dot = path.lastIndexOf('.');
        return dot >= 0 ? path.substring(dot + 1) : path;
    }

    private static Response error(int status, String code, String message) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(status, code, message))
                .build();
    }
}
