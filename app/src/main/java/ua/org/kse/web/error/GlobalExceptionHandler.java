package ua.org.kse.web.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ua.org.kse.error.CosmicTagNotAllowedException;
import ua.org.kse.error.ProductNotFoundException;
import ua.org.kse.external.TagServiceException;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String PROBLEM_BASE_URI = "https://example.com/problems/";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest req) {
        BindingResult br = ex.getBindingResult();

        List<Map<String, String>> errors = br.getFieldErrors().stream()
            .map(fe -> Map.of(
                "field", fe.getField(),
                "message", Objects.toString(fe.getDefaultMessage(), "Validation error")
            ))
            .toList();

        ProblemDetail body = createProblemDetail(
            HttpStatus.BAD_REQUEST,
            "Validation failed",
            "validation-error",
            req
        );
        body.setProperty("errors", errors);

        return buildResponse(HttpStatus.BAD_REQUEST, body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex,
                                                                   HttpServletRequest req) {
        List<Map<String, String>> errors = ex.getConstraintViolations().stream()
            .map(cv -> Map.of(
                "field", cv.getPropertyPath().toString(),
                "message", Objects.toString(cv.getMessage(), "Validation error")
            ))
            .toList();

        ProblemDetail body = createProblemDetail(
            HttpStatus.BAD_REQUEST,
            "Validation failed",
            "validation-error",
            req
        );
        body.setProperty("errors", errors);

        return buildResponse(HttpStatus.BAD_REQUEST, body);
    }

    @ExceptionHandler(CosmicTagNotAllowedException.class)
    public ResponseEntity<ProblemDetail> handleCosmicTagNotAllowed(CosmicTagNotAllowedException ex,
                                                                   HttpServletRequest req) {
        ProblemDetail body = createProblemDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage(),
            "cosmic-tag-not-allowed",
            req
        );

        return buildResponse(HttpStatus.BAD_REQUEST, body);
    }

    @ExceptionHandler(TagServiceException.class)
    public ResponseEntity<ProblemDetail> handleTagService(TagServiceException ex, HttpServletRequest req) {
        ProblemDetail body = createProblemDetail(
            HttpStatus.SERVICE_UNAVAILABLE,
            ex.getMessage(),
            "tag-service-error",
            req
        );

        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, body);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleProductNotFound(ProductNotFoundException ex, HttpServletRequest req) {
        ProblemDetail body = createProblemDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage(),
            "product-not-found",
            req
        );

        return buildResponse(HttpStatus.NOT_FOUND, body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(Exception ex, HttpServletRequest req) {
        ProblemDetail body = createProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            String.format("Unexpected error: %s", ex.getClass().getSimpleName()),
            "internal-error",
            req
        );

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, body);
    }

    private ProblemDetail createProblemDetail(HttpStatus status,
                                              String detail,
                                              String typeSuffix,
                                              HttpServletRequest req) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setTitle(status.getReasonPhrase());
        body.setType(URI.create(PROBLEM_BASE_URI + typeSuffix));
        body.setInstance(URI.create(req.getRequestURI()));
        return body;
    }

    private ResponseEntity<ProblemDetail> buildResponse(HttpStatus status, ProblemDetail body) {
        return ResponseEntity.status(status)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(body);
    }
}