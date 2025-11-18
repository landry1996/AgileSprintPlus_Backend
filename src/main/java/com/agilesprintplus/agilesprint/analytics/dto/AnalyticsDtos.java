package com.agilesprintplus.agilesprint.analytics.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public class AnalyticsDtos {

  public enum Method {
    NAIVE,
    SMA,
    EMA,
    LINEAR
  }
  public record ForecastRequest(
          @NotEmpty List<Double> history,
          @NotNull Method method,
          @Min(1) Integer window,
          Double alpha
  ) {}
  public record ForecastResponse(
          double forecast,
          String method,
          Map<String, Object> params,
          Map<String, Object> diagnostics
  ) {}
}