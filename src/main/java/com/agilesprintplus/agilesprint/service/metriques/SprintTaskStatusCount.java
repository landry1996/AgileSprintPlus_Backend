package com.agilesprintplus.agilesprint.service.metriques;


import com.agilesprintplus.agilesprint.domain.TaskStatus;

public interface SprintTaskStatusCount {
    TaskStatus getStatus();
    Long getTotal();
}