package com.agilesprintplus.notification.impl;

import com.agilesprintplus.notification.EmailService;
import com.agilesprintplus.notification.configs.EmailProperties;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailProperties props;

    @Override
    @Async
    public void sendSimple(String to, String subject, String text) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(formattedFrom());
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            mailSender.send(msg);
            log.info("Email (text) sent to={}, subject='{}'", to, subject);
        } catch (MailException e) {
            log.error("Failed to send simple email to={} subject='{}': {}", to, subject, e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void sendHtml(String to, String subject, String html, @Nullable String textFallback) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(props.from(), props.displayName()));
            if (props.replyTo() != null) helper.setReplyTo(props.replyTo());
            helper.setTo(to);
            helper.setSubject(subject);
            // true => HTML ; textFallback (optionnel) pour clients sans HTML
            helper.setText(textFallback != null ? textFallback : stripHtml(html), html);
            mailSender.send(mime);
            log.info("Email (html) sent to={}, subject='{}'", to, subject);
        } catch (Exception e) {
            log.error("Failed to send html email to={} subject='{}': {}", to, subject, e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void sendBulkHtml(List<String> to, String subject, String html, @Nullable String textFallback) {
        if (to == null || to.isEmpty()) return;
        to.forEach(email -> sendHtml(email, subject, html, textFallback));
    }

    private String formattedFrom() {
        return props.displayName() != null
                ? String.format("%s <%s>", props.displayName(), props.from())
                : props.from();
    }

    private String stripHtml(String html) {
        return html == null ? "" : html.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
    }
}
