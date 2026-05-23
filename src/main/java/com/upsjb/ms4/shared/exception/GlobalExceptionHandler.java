// ruta: src/main/java/com/upsjb/ms4/shared/exception/GlobalExceptionHandler.java
package com.upsjb.ms4.shared.exception;

import com.upsjb.ms4.dto.shared.ErrorResponseDto;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.response.ErrorResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String DEFAULT_INTERNAL_MESSAGE =
            "Ocurrió un error interno. Intente nuevamente o contacte soporte.";

    private final ErrorResponseFactory errorResponseFactory;

    public GlobalExceptionHandler(ErrorResponseFactory errorResponseFactory) {
        this.errorResponseFactory = errorResponseFactory;
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(ValidationException ex,
                                                             HttpServletRequest request) {
        return build(
                HttpStatus.BAD_REQUEST,
                ex.getCode(),
                ex.getMessage(),
                ex.getFieldErrors(),
                ex,
                request,
                false
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDto> handleBusiness(BusinessException ex,
                                                           HttpServletRequest request) {
        boolean technical = ex.getStatus().is5xxServerError();
        return build(
                ex.getStatus(),
                ex.getCode(),
                ex.getMessage(),
                Map.of(),
                ex,
                request,
                technical
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleBeanValidation(MethodArgumentNotValidException ex,
                                                                 HttpServletRequest request) {
        return build(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.VALIDATION_ERROR,
                "La solicitud contiene campos inválidos.",
                fieldErrorsFromBinding(ex),
                ex,
                request,
                false
        );
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponseDto> handleBindException(BindException ex,
                                                                HttpServletRequest request) {
        return build(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.VALIDATION_ERROR,
                "La solicitud contiene parámetros inválidos.",
                fieldErrorsFromBinding(ex),
                ex,
                request,
                false
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolation(ConstraintViolationException ex,
                                                                      HttpServletRequest request) {
        Map<String, List<String>> errors = new LinkedHashMap<>();

        ex.getConstraintViolations().forEach(violation ->
                errors.computeIfAbsent(violation.getPropertyPath().toString(), key -> new ArrayList<>())
                        .add(violation.getMessage())
        );

        return build(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.VALIDATION_ERROR,
                "La solicitud contiene parámetros inválidos.",
                errors,
                ex,
                request,
                false
        );
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodValidation(HandlerMethodValidationException ex,
                                                                   HttpServletRequest request) {
        Map<String, List<String>> errors = new LinkedHashMap<>();

        ex.getParameterValidationResults().forEach(result -> {
            String parameterName = result.getMethodParameter().getParameterName();
            String field = parameterName == null || parameterName.isBlank() ? "request" : parameterName;

            result.getResolvableErrors().forEach(error ->
                    errors.computeIfAbsent(field, key -> new ArrayList<>())
                            .add(error.getDefaultMessage())
            );
        });

        ex.getCrossParameterValidationResults().forEach(error ->
                errors.computeIfAbsent("request", key -> new ArrayList<>())
                        .add(error.getDefaultMessage())
        );

        return build(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.VALIDATION_ERROR,
                "La solicitud contiene parámetros inválidos.",
                errors,
                ex,
                request,
                false
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                               HttpServletRequest request) {
        String field = ex.getName() == null || ex.getName().isBlank() ? "request" : ex.getName();
        String message = "El parámetro '" + field + "' tiene un formato inválido.";

        return build(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.INVALID_PARAMETER,
                message,
                Map.of(field, List.of(message)),
                ex,
                request,
                false
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingParameter(MissingServletRequestParameterException ex,
                                                                   HttpServletRequest request) {
        String field = ex.getParameterName();
        String message = "El parámetro '" + field + "' es obligatorio.";

        return build(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.INVALID_PARAMETER,
                message,
                Map.of(field, List.of(message)),
                ex,
                request,
                false
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleUnreadableBody(HttpMessageNotReadableException ex,
                                                                 HttpServletRequest request) {
        return build(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.INVALID_BODY,
                "El cuerpo de la solicitud no tiene un formato válido.",
                Map.of(),
                ex,
                request,
                false
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                     HttpServletRequest request) {
        return build(
                HttpStatus.METHOD_NOT_ALLOWED,
                ErrorCodes.METHOD_NOT_ALLOWED,
                "El método HTTP no está permitido para este recurso.",
                Map.of(),
                ex,
                request,
                false
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex,
                                                                       HttpServletRequest request) {
        return build(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                ErrorCodes.UNSUPPORTED_MEDIA_TYPE,
                "El tipo de contenido enviado no es soportado por este recurso.",
                Map.of(),
                ex,
                request,
                false
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(Exception ex,
                                                               HttpServletRequest request) {
        return build(
                HttpStatus.FORBIDDEN,
                ErrorCodes.FORBIDDEN,
                "No tiene permisos para realizar esta acción.",
                Map.of(),
                ex,
                request,
                false
        );
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUnauthorized(Exception ex,
                                                               HttpServletRequest request) {
        return build(
                HttpStatus.UNAUTHORIZED,
                ErrorCodes.UNAUTHORIZED,
                "Debe autenticarse para acceder al recurso.",
                Map.of(),
                ex,
                request,
                false
        );
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorResponseDto> handleSpringErrorResponse(ErrorResponseException ex,
                                                                      HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        ProblemDetail detail = ex.getBody();
        String message = detail.getDetail() != null ? detail.getDetail() : "Solicitud inválida.";

        return build(
                status,
                ErrorCodes.INVALID_REQUEST,
                message,
                Map.of(),
                ex,
                request,
                status.is5xxServerError()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnexpected(Exception ex,
                                                             HttpServletRequest request) {
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCodes.INTERNAL_ERROR,
                DEFAULT_INTERNAL_MESSAGE,
                Map.of(),
                ex,
                request,
                true
        );
    }

    private ResponseEntity<ErrorResponseDto> build(HttpStatus status,
                                                   String code,
                                                   String message,
                                                   Map<String, List<String>> fieldErrors,
                                                   Exception ex,
                                                   HttpServletRequest request,
                                                   boolean technicalError) {
        if (technicalError) {
            log.error("Error técnico en MS4. path={}, message={}",
                    request != null ? request.getRequestURI() : null,
                    ex.getMessage(),
                    ex
            );
        } else {
            log.warn("Error funcional en MS4. path={}, message={}",
                    request != null ? request.getRequestURI() : null,
                    ex.getMessage()
            );
        }

        ErrorResponseDto body = errorResponseFactory.create(
                code,
                message,
                null,
                fieldErrors,
                request
        );

        return ResponseEntity.status(status).body(body);
    }

    private Map<String, List<String>> fieldErrorsFromBinding(MethodArgumentNotValidException ex) {
        Map<String, List<String>> errors = new LinkedHashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.computeIfAbsent(fieldError.getField(), key -> new ArrayList<>())
                    .add(fieldError.getDefaultMessage());
        }

        return errors;
    }

    private Map<String, List<String>> fieldErrorsFromBinding(BindException ex) {
        Map<String, List<String>> errors = new LinkedHashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.computeIfAbsent(fieldError.getField(), key -> new ArrayList<>())
                    .add(fieldError.getDefaultMessage());
        }

        return errors;
    }
}