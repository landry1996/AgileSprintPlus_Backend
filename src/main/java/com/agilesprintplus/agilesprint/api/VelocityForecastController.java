package com.agilesprintplus.agilesprint.api;


import com.agilesprintplus.agilesprint.analytics.dto.AnalyticsDtos.Method;
import com.agilesprintplus.agilesprint.analytics.dto.AnalyticsDtos.ForecastRequest;
import com.agilesprintplus.agilesprint.analytics.dto.AnalyticsDtos.ForecastResponse;
import com.agilesprintplus.agilesprint.analytics.service.VelocityForecastService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics/velocity")
@RequiredArgsConstructor
@Validated
public class VelocityForecastController {

    private final VelocityForecastService velocityForecastService;

    /** Endpoint principal (POST JSON) */
    @PreAuthorize("isAuthenticated()")
    @PostMapping(
            path = "/forecast",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ForecastResponse> forecast(@Valid @RequestBody ForecastRequest request) {
        return ResponseEntity.ok(velocityForecastService.forecast(request));
    }

    /** Endpoint simplifié NAIVE (POST JSON) */
    @PreAuthorize("isAuthenticated()")
    @PostMapping(
            path = "/forecast/naive",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ForecastResponse> naiveForecast(@Valid @RequestBody SimpleNaiveRequest request) {
        ForecastRequest mapped = new ForecastRequest(
                request.history(),
                Method.NAIVE,
                null,
                null
        );
        return ResponseEntity.ok(velocityForecastService.forecast(mapped));
    }

    /**
     * Endpoint GET “rapide”
     * Exemples:
     *  - /api/analytics/velocity/forecast?history=5,6,7&method=SMA&window=2
     *  - /api/analytics/velocity/forecast?history=10&method=NAIVE
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/forecast", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ForecastResponse> forecastWithParams(
            @RequestParam("history") String history,
            @RequestParam("method") String method,
            @RequestParam(value = "window", required = false) Integer window,
            @RequestParam(value = "alpha",  required = false) Double alpha) {

        List<Double> historyList = parseHistory(history);
        Method methodEnum = Method.valueOf(method.toUpperCase());

        ForecastRequest request = new ForecastRequest(historyList, methodEnum, window, alpha);
        return ResponseEntity.ok(velocityForecastService.forecast(request));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/methods", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String[]>> getAvailableMethods() {
        String[] names = Arrays.stream(Method.values()).map(Enum::name).toArray(String[]::new);
        return ResponseEntity.ok(Map.of("availableMethods", names));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "OK", "service", "VelocityForecast"));
    }

    public record SimpleNaiveRequest(@NotEmpty List<Double> history) {}

    /** Convertit "1,2,3" → List<Double> en tolérant espaces */
    private static List<Double> parseHistory(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("history must be provided (e.g. history=5,6,7)");
        }
        try {
            return Arrays.stream(raw.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Double::parseDouble)
                    .toList();
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("history must be a comma-separated list of numbers");
        }
    }
}
