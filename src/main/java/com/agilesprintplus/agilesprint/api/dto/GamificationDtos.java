package com.agilesprintplus.agilesprint.api.dto;
import java.util.*;
public class GamificationDtos {

    public record Response(
            UUID userId,
            int xp,
            int badges,
            int tasksDone,
            int sprintsCompleted
    ) {}
}