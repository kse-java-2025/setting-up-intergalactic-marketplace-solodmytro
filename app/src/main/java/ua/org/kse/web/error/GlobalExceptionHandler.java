package ua.org.kse.web.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ua.org.kse.external.TagServiceException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetails> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        BindingResult br = ex.getBindingResult();
        var errors = br.getFieldErrors().stream()
            .map(fe -> ProblemDetails.FieldError.builder()
                .field(fe.getField())
                .message(fe.getDefaultMessage())
                .build())
            .collect(Collectors.toList());

        var body = ProblemDetails.builder()
            .type("https://example.com/problems/validation-error")
            .title("Bad Request")
            .status(HttpStatus.BAD_REQUEST.value())
            .detail("Validation failed")
            .instance(req.getRequestURI())
            .errors(errors)
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetails> handleConstraintViolation(ConstraintViolationException ex,
                                                                    HttpServletRequest req) {
        var errors = ex.getConstraintViolations().stream()
            .map(cv -> ProblemDetails.FieldError.builder()
                .field(cv.getPropertyPath().toString())
                .message(cv.getMessage())
                .build())
            .collect(Collectors.toList());

        var body = ProblemDetails.builder()
            .type("https://example.com/problems/validation-error")
            .title("Bad Request")
            .status(HttpStatus.BAD_REQUEST.value())
            .detail("Validation failed")
            .instance(req.getRequestURI())
            .errors(errors)
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(body);
    }

    @ExceptionHandler(TagServiceException.class)
    public ResponseEntity<ProblemDetails> handleTagService(TagServiceException ex, HttpServletRequest req) {
        var body = ProblemDetails.builder()
            .type("https://example.com/problems/tag-service-error")
            .title("Cosmic Tag Service Error")
            .status(HttpStatus.SERVICE_UNAVAILABLE.value())
            .detail(ex.getMessage())
            .instance(req.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(body);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetails> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        var body = ProblemDetails.builder()
            .type("https://example.com/problems/not-found")
            .title("Not Found")
            .status(HttpStatus.NOT_FOUND.value())
            .detail(ex.getMessage())
            .instance(req.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(body);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ProblemDetails> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        var body = ProblemDetails.builder()
            .type("https://example.com/problems/bad-request")
            .title("Bad Request")
            .status(HttpStatus.BAD_REQUEST.value())
            .detail(ex.getMessage())
            .instance(req.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetails> handleGeneric(Exception ex, HttpServletRequest req) {
        var body = ProblemDetails.builder()
            .type("https://example.com/problems/internal-error")
            .title("Internal Server Error")
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .detail("Unexpected error")
            .instance(req.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(body);
    }
}