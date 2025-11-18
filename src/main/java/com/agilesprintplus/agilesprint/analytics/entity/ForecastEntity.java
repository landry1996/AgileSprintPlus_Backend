package com.agilesprintplus.agilesprint.analytics.entity;

import com.agilesprintplus.agilesprint.analytics.utils.JsonUtils;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "velocity_forecasts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Méthode utilisée : NAIVE, SMA, EMA, LINEAR */
    @Column(nullable = false, length = 20)
    private String method;

    /** Dernière vélocité observée ou moyenne calculée */
    @Column(nullable = false)
    private double forecast;

    /** Paramètres utilisés (alpha, window...) — stockés en JSON */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String paramsJson;

    /** Diagnostics (n, mean, stddev...) — stockés en JSON */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String diagnosticsJson;

    /** Date de génération de la prévision */
    @CreationTimestamp
    private Instant createdAt;

    /** Optionnel : si tu veux relier la prévision à un Sprint */
    @Column(name = "sprint_id")
    private UUID sprintId;

    /** Optionnel : identifiant de l’utilisateur ayant généré la prévision */
    @Column(name = "user_id")
    private UUID userId;

    // Méthodes utilitaires pour (dé)sérialiser les champs JSON
    @Transient
    public Map<String, Object> getParams() {
        return JsonUtils.fromJson(paramsJson);
    }

    @Transient
    public Map<String, Object> getDiagnostics() {
        return JsonUtils.fromJson(diagnosticsJson);
    }

    public void setParams(Map<String, Object> params) {
        this.paramsJson = JsonUtils.toJson(params);
    }

    public void setDiagnostics(Map<String, Object> diagnostics) {
        this.diagnosticsJson = JsonUtils.toJson(diagnostics);
    }
}
