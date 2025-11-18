package com.agilesprintplus.notification.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mail")
public record EmailProperties(
        String from,
        String replyTo,
        String displayName
) {}