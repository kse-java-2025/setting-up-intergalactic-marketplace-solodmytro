package ua.org.kse.web.error;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ProblemDetails {
    private String type;
    private String title;
    private Integer status;
    private String detail;
    private String instance;
    private List<FieldError> errors;

    @Data
    @Builder
    public static class FieldError {
        private String field;
        private String message;
    }
}