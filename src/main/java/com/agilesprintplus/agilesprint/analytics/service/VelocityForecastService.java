package com.agilesprintplus.agilesprint.analytics.service;

import com.agilesprintplus.agilesprint.analytics.dto.AnalyticsDtos.Method;
import com.agilesprintplus.agilesprint.analytics.dto.AnalyticsDtos.ForecastRequest;
import com.agilesprintplus.agilesprint.analytics.dto.AnalyticsDtos.ForecastResponse;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VelocityForecastService {

    private static final double DEFAULT_ALPHA = 0.6;
    private static final int DEFAULT_WINDOW = 3;

    private final Map<Method, ForecastingAlgorithm> algorithms;
    private final ValidationUtils validationUtils;
    private final DiagnosticCalculator diagnosticCalculator;

    public VelocityForecastService() {
        this.validationUtils = new ValidationUtils();
        this.diagnosticCalculator = new DiagnosticCalculator();
        this.algorithms = initializeAlgorithms();
    }

    /** Version naïve conservée pour compatibilité */
    public double naiveForecast(double lastVelocity) {
        double alpha = DEFAULT_ALPHA;
        return alpha * lastVelocity + (1 - alpha) * Math.max(1.0, lastVelocity - 2);
    }

    /** Point d'entrée générique */
    public ForecastResponse forecast(ForecastRequest request) {
        validationUtils.validateRequest(request);

        List<Double> history = validationUtils.validateHistory(request.history());
        Method method = request.method();

        if (isMinimalNaiveCase(history, method)) {
            return handleMinimalNaiveCase(history);
        }

        validationUtils.ensureMinimumHistorySize(history, method);

        ForecastingAlgorithm algorithm = algorithms.get(method);
        if (algorithm == null) {
            throw new IllegalArgumentException("Unsupported method: " + method);
        }

        ForecastResult result = algorithm.calculate(history, request);
        Map<String, Object> diagnostics = diagnosticCalculator.calculate(history);

        return buildResponse(result, diagnostics);
    }
    private boolean isMinimalNaiveCase(List<Double> history, Method method) {
        return history.size() == 1 && method == Method.NAIVE;
    }
    private ForecastResponse handleMinimalNaiveCase(List<Double> history) {
        double forecast = naiveForecast(history.get(0));
        Map<String, Object> params = Map.of("alpha", DEFAULT_ALPHA);
        Map<String, Object> diagnostics = diagnosticCalculator.calculate(history);
        return buildResponse(new ForecastResult(forecast, params), diagnostics);
    }
    private ForecastResponse buildResponse(ForecastResult result, Map<String, Object> diagnostics) {
        return new ForecastResponse(
                result.getForecast(),
                result.getMethod().name(),
                result.getParameters(),
                diagnostics
        );
    }
    private Map<Method, ForecastingAlgorithm> initializeAlgorithms() {
        Map<Method, ForecastingAlgorithm> algos = new EnumMap<>(Method.class);
        algos.put(Method.NAIVE, new NaiveAlgorithm());
        algos.put(Method.SMA, new SimpleMovingAverage());
        algos.put(Method.EMA, new ExponentialMovingAverage());
        algos.put(Method.LINEAR, new LinearRegressionAlgorithm());
        return algos;
    }
    private interface ForecastingAlgorithm {
        ForecastResult calculate(List<Double> history, ForecastRequest request);
        Method getMethod();
    }
    private static class ForecastResult {
        private final double forecast;
        private final Map<String, Object> parameters;
        private final Method method;

        public ForecastResult(double forecast, Map<String, Object> parameters) {
            this(forecast, parameters, null);
        }
        public ForecastResult(double forecast, Map<String, Object> parameters, Method method) {
            this.forecast = forecast;
            this.parameters = Collections.unmodifiableMap(new HashMap<>(parameters));
            this.method = method;
        }
        public double getForecast() {
            return forecast;
        }
        public Map<String, Object> getParameters() {
            return parameters;
        }
        public Method getMethod() {
            return method;
        }
    }
    private class NaiveAlgorithm implements ForecastingAlgorithm {
        @Override
        public ForecastResult calculate(List<Double> history, ForecastRequest request) {
            double forecast = naiveForecast(ListUtils.last(history));
            return new ForecastResult(forecast, Map.of("alpha", DEFAULT_ALPHA), Method.NAIVE);
        }
        @Override
        public Method getMethod() { return Method.NAIVE; }
    }
    private class SimpleMovingAverage implements ForecastingAlgorithm {
        @Override
        public ForecastResult calculate(List<Double> history, ForecastRequest request) {
            int window = calculateWindow(history, request);
            double forecast = calculateSMA(history, window);
            return new ForecastResult(forecast, Map.of("window", window), Method.SMA);
        }
        private int calculateWindow(List<Double> history, ForecastRequest request) {
            int window = (request.window() != null) ? request.window() : Math.min(DEFAULT_WINDOW, history.size());
            return Math.max(1, Math.min(window, history.size()));
        }
        @Override
        public Method getMethod() {
            return Method.SMA;
        }
    }
    private class ExponentialMovingAverage implements ForecastingAlgorithm {
        @Override
        public ForecastResult calculate(List<Double> history, ForecastRequest request) {
            double alpha = calculateAlpha(request);
            double forecast = calculateEMA(history, alpha);
            return new ForecastResult(forecast, Map.of("alpha", alpha), Method.EMA);
        }
        private double calculateAlpha(ForecastRequest request) {
            double alpha = (request.alpha() != null) ? request.alpha() : DEFAULT_ALPHA;
            return MathUtils.clamp(alpha, 0.0001, 1.0);
        }
        @Override
        public Method getMethod() { return Method.EMA; }
    }
    private class LinearRegressionAlgorithm implements ForecastingAlgorithm {
        @Override
        public ForecastResult calculate(List<Double> history, ForecastRequest request) {
            double forecast = calculateLinearRegression(history);
            return new ForecastResult(forecast, Map.of(), Method.LINEAR);
        }
        @Override
        public Method getMethod() { return Method.LINEAR; }
    }

    private static class ValidationUtils {
        public void validateRequest(ForecastRequest request) {
            Objects.requireNonNull(request, "ForecastRequest must not be null");
            Objects.requireNonNull(request.method(), "method must not be null");
        }
        public List<Double> validateHistory(List<Double> history) {
            if (history == null || history.isEmpty()) {
                throw new IllegalArgumentException("history must not be null or empty");
            }
            for (Double value : history) {
                if (value == null || value.isNaN() || value.isInfinite()) {
                    throw new IllegalArgumentException("history contains invalid number: " + value);
                }
            }
            return history;
        }
        public void ensureMinimumHistorySize(List<Double> history, Method method) {
            int minSize = getMinimumHistorySize(method);
            if (history.size() < minSize) {
                throw new IllegalArgumentException(
                        "History must contain at least " + minSize + " points for " + method);
            }
        }
        private int getMinimumHistorySize(Method method) {
            return (method == Method.LINEAR) ? 2 : 1;
        }
    }
    private static class DiagnosticCalculator {
        public Map<String, Object> calculate(List<Double> history) {
            int n = history.size();
            double mean = calculateMean(history);
            double stdDev = calculateStandardDeviation(history, mean, n);

            return Map.of(
                    "n", n,
                    "mean", MathUtils.round(mean, 4),
                    "stddev", MathUtils.round(stdDev, 4)
            );
        }
        private double calculateMean(List<Double> history) {
            return history.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        }
        private double calculateStandardDeviation(List<Double> history, double mean, int n) {
            if (n <= 1) return 0.0;

            double variance = history.stream()
                    .mapToDouble(value -> Math.pow(value - mean, 2))
                    .sum() / (n - 1);

            return Math.sqrt(variance);
        }
    }
    private static double calculateSMA(List<Double> history, int window) {
        double sum = 0.0;
        for (int i = history.size() - window; i < history.size(); i++) {
            sum += history.get(i);
        }
        return sum / window;
    }
    private static double calculateEMA(List<Double> history, double alpha) {
        double ema = ListUtils.first(history);
        for (int i = 1; i < history.size(); i++) {
            ema = alpha * history.get(i) + (1 - alpha) * ema;
        }
        return ema;
    }
    private static double calculateLinearRegression(List<Double> history) {
        int n = history.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            double x = i + 1;
            double y = history.get(i);
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double denominator = n * sumX2 - sumX * sumX;
        if (denominator == 0.0) return ListUtils.last(history);

        double slope = (n * sumXY - sumX * sumY) / denominator;
        double intercept = (sumY - slope * sumX) / n;
        double nextX = n + 1;

        return intercept + slope * nextX;
    }

    private static class MathUtils {
        public static double clamp(double value, double min, double max) {
            return Math.max(min, Math.min(max, value));
        }

        public static double round(double value, int decimals) {
            double scale = Math.pow(10, decimals);
            return Math.round(value * scale) / scale;
        }
    }
    private static class ListUtils {
        public static double first(List<Double> list) {
            return list.get(0);
        }

        public static double last(List<Double> list) {
            return list.get(list.size() - 1);
        }
    }
}