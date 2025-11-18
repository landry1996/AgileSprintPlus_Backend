package com.agilesprintplus.agilesprint.service.taskcount;

import java.util.UUID;

public interface SprintTaskCount {

    UUID sprintId();
    Long completedTaskCount();
}