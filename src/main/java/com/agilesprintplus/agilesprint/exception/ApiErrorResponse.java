package com.agilesprintplus.agilesprint.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Schema(description = "Structure d'une réponse d'erreur standard renvoyée par l'API")
public class ApiErrorResponse {

    @Schema(description = "Horodatage de l'erreur", example = "2025-10-30T13:30:12.123Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;

    @Schema(description = "Code HTTP de l'erreur", example = "404")
    private int status;

    @Schema(description = "Type d'erreur HTTP", example = "Not Found")
    private String error;

    @Schema(description = "Message descriptif de l'erreur", example = "User not found: <uuid>")
    private String message;

    @Schema(description = "Chemin de la requête", example = "/api/users/<uuid>")
    private String path;

    public ApiErrorResponse() { }

    public ApiErrorResponse(HttpStatus status, String message, String path) {
        this.timestamp = Instant.now();
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = message;
        this.path = path;
    }

    public Instant getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }

    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public void setStatus(int status) { this.status = status; }
    public void setError(String error) { this.error = error; }
    public void setMessage(String message) { this.message = message; }
    public void setPath(String path) { this.path = path; }
}
