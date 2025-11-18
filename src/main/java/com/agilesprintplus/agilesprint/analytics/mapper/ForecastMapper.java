package com.agilesprintplus.agilesprint.analytics.mapper;

import com.agilesprintplus.agilesprint.analytics.dto.AnalyticsDtos;
import com.agilesprintplus.agilesprint.analytics.entity.ForecastEntity;
import com.agilesprintplus.agilesprint.analytics.utils.JsonUtils;
import org.springframework.stereotype.Component;

@Component
public class ForecastMapper {

    public ForecastEntity toEntity(AnalyticsDtos.ForecastResponse dto) {
        ForecastEntity e = new ForecastEntity();
        e.setMethod(dto.method());
        e.setForecast(dto.forecast());
        e.setParamsJson(JsonUtils.toJson(dto.params()));
        e.setDiagnosticsJson(JsonUtils.toJson(dto.diagnostics()));
        return e;
    }

    public AnalyticsDtos.ForecastResponse toDto(ForecastEntity e) {
        return new AnalyticsDtos.ForecastResponse(
                e.getForecast(),
                e.getMethod(),
                JsonUtils.fromJson(e.getParamsJson()),
                JsonUtils.fromJson(e.getDiagnosticsJson())
        );
    }
}
