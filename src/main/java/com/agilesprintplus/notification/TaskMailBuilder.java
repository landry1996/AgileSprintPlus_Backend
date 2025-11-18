package com.agilesprintplus.notification;

import com.agilesprintplus.agilesprint.domain.Task;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class TaskMailBuilder {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public String subjectTaskAssigned(Task task) {
        return "🧩 Nouvelle tâche assignée : " + safe(task.getTitle());
    }

    public String htmlTaskAssigned(Task task, String assigneeName) {
        String sprint = (task.getSprint() != null) ? task.getSprint().getName() : "—";
        return """
            <div style="font-family:Segoe UI,Arial,sans-serif;font-size:14px;color:#222">
              <h2 style="margin:0 0 12px">Nouvelle tâche assignée</h2>
              <p>Bonjour %s,</p>
              <p>Une tâche vient de vous être assignée :</p>
              <table style="border-collapse:collapse">
                <tr><td style="padding:4px 8px;color:#555">Titre</td><td style="padding:4px 8px"><b>%s</b></td></tr>
                <tr><td style="padding:4px 8px;color:#555">Description</td><td style="padding:4px 8px">%s</td></tr>
                <tr><td style="padding:4px 8px;color:#555">Sprint</td><td style="padding:4px 8px">%s</td></tr>
                <tr><td style="padding:4px 8px;color:#555">Story Points</td><td style="padding:4px 8px">%s</td></tr>
                <tr><td style="padding:4px 8px;color:#555">Statut</td><td style="padding:4px 8px">%s</td></tr>
              </table>
              <p style="margin-top:16px">Bon travail 👊</p>
              <hr style="border:none;border-top:1px solid #eee;margin:16px 0">
              <p style="font-size:12px;color:#777">Agile Sprint+</p>
            </div>
            """.formatted(
                safe(assigneeName),
                safe(task.getTitle()),
                safe(task.getDescription()),
                safe(sprint),
                task.getStoryPoints() == null ? "—" : task.getStoryPoints(),
                task.getStatus() == null ? "—" : task.getStatus().name()
        );
    }

    public String textTaskAssigned(Task task, String assigneeName) {
        String sprint = (task.getSprint() != null) ? task.getSprint().getName() : "—";
        return """
            Bonjour %s,
            Une tâche vient de vous être assignée.

            Titre: %s
            Description: %s
            Sprint: %s
            Story Points: %s
            Statut: %s

            Bon travail,
            Agile Sprint+
            """.formatted(
                safe(assigneeName),
                safe(task.getTitle()),
                safe(task.getDescription()),
                safe(sprint),
                task.getStoryPoints() == null ? "—" : task.getStoryPoints(),
                task.getStatus() == null ? "—" : task.getStatus().name()
        );
    }

    private String safe(String v) {
        return v == null ? "—" : v;
    }
}
