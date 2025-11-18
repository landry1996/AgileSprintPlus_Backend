package com.agilesprintplus.notification.listeners;

import com.agilesprintplus.notification.EmailService;
import com.agilesprintplus.notification.TaskMailBuilder;
import com.agilesprintplus.notification.events.TaskAssignedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskAssignedListener {

    private final EmailService emailService;
    private final TaskMailBuilder mailBuilder;

    @Async
    @EventListener
    public void onTaskAssigned(TaskAssignedEvent evt) {
        var task = evt.task();
        evt.assignees().forEach(u -> {
            if (u.getEmail() == null || u.getEmail().isBlank()) {
                log.warn("Skip email: user {} has no email", u.getId());
                return;
            }
            String subject = mailBuilder.subjectTaskAssigned(task);
            String html = mailBuilder.htmlTaskAssigned(task, displayName(u));
            String text = mailBuilder.textTaskAssigned(task, displayName(u));
            emailService.sendHtml(u.getEmail(), subject, html, text);
        });
    }

    private String displayName(com.agilesprintplus.agilesprint.domain.User u) {
        if (u.getFirstName() != null || u.getLastName() != null) {
            return ((u.getFirstName() == null ? "" : u.getFirstName()) + " " +
                    (u.getLastName() == null ? "" : u.getLastName())).trim();
        }
        return u.getUsername();
    }
}
