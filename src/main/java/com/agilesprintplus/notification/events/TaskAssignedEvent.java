package com.agilesprintplus.notification.events;



import com.agilesprintplus.agilesprint.domain.Task;
import com.agilesprintplus.agilesprint.domain.User;

import java.util.Set;

public record TaskAssignedEvent(Task task, Set<User> assignees) {}