package com.agilesprintplus.notification;

import java.util.List;

public interface EmailService {
    void sendSimple(String to, String subject, String text);
    void sendHtml(String to, String subject, String html, String textFallback);
    void sendBulkHtml(List<String> to, String subject, String html, String textFallback);
}