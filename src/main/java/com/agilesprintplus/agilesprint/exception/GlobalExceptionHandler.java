package com.agilesprintplus.agilesprint.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException ex, WebRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException ex, WebRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException ex, WebRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(PasswordChangeRequiredException.class)
    public ResponseEntity<ApiErrorResponse> handlePasswordChange(PasswordChangeRequiredException ex, WebRequest req) {
        return build(HttpStatus.PRECONDITION_REQUIRED, ex.getMessage(), req);
    }

    // Validation @Valid sur @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleBeanValidation(MethodArgumentNotValidException ex, WebRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .reduce((a,b) -> a + "; " + b)
                .orElse("Validation error");
        return build(HttpStatus.BAD_REQUEST, msg, req);
    }

    // Validation sur @RequestParam / @PathVariable (ConstraintViolation)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraint(ConstraintViolationException ex, WebRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    // Erreurs fonctionnelles lancées par le service (ex: validations métier)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    // Corps JSON illisible / invalide (ex: number au lieu d'une liste, JSON mal formé)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException ex, WebRequest req) {
        String msg = "Malformed JSON request body";
        if (ex.getMostSpecificCause() != null) {
            msg += ": " + ex.getMostSpecificCause().getMessage();
        }
        return build(HttpStatus.BAD_REQUEST, msg, req);
    }

    // Paramètre de requête manquant (ex: /forecast?method=SMA sans history)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParam(MissingServletRequestParameterException ex, WebRequest req) {
        String msg = "Missing request parameter: " + ex.getParameterName();
        return build(HttpStatus.BAD_REQUEST, msg, req);
    }

    // Mauvaise conversion d’un paramètre (ex: method=unknown, history=abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest req) {
        String name = ex.getName();
        String requiredType = (ex.getRequiredType() != null) ? ex.getRequiredType().getSimpleName() : "unknown";
        String value = String.valueOf(ex.getValue());
        String msg = "Parameter '" + name + "' with value '" + value + "' is not of required type " + requiredType;
        return build(HttpStatus.BAD_REQUEST, msg, req);
    }
    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, WebRequest req) {
        String path = req.getDescription(false).replace("uri=", "");
        return ResponseEntity.status(status).body(new ApiErrorResponse(status, message, path));
    }

    //----

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiErrorResponse handleAccessDenied(AccessDeniedException ex, WebRequest req) {
        return new ApiErrorResponse(HttpStatus.FORBIDDEN, "Forbidden", req.getDescription(false).replace("uri=",""));
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleAuth(AuthenticationException ex, WebRequest req) {
        return new ApiErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", req.getDescription(false).replace("uri=",""));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(Exception ex, WebRequest req) {
        // LOGGE la vraie cause pour la voir dans la console
        ex.printStackTrace(); // ou logger.error("Unhandled", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred", req);
    }
}
