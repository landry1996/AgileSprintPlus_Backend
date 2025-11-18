package com.agilesprintplus.agilesprint.mapper;

import com.agilesprintplus.agilesprint.api.dto.SprintDtos;
import com.agilesprintplus.agilesprint.domain.Sprint;
import java.time.LocalDate;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-03T21:53:31+0400",
    comments = "version: 1.6.2, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class SprintMapperImpl implements SprintMapper {

    @Override
    public Sprint toEntity(SprintDtos.Create dto) {
        validateCreate( dto );

        if ( dto == null ) {
            return null;
        }

        Sprint.SprintBuilder sprint = Sprint.builder();

        if ( dto.name() != null ) {
            sprint.name( dto.name() );
        }
        if ( dto.startDate() != null ) {
            sprint.startDate( dto.startDate() );
        }
        if ( dto.endDate() != null ) {
            sprint.endDate( dto.endDate() );
        }
        if ( dto.goal() != null ) {
            sprint.goal( dto.goal() );
        }

        sprint.enabled( true );

        Sprint sprintResult = sprint.build();

        afterCreate( dto, sprintResult );

        return sprintResult;
    }

    @Override
    public void updateEntity(Sprint sprint, SprintDtos.Update dto) {
        if ( dto == null ) {
            return;
        }

        validateUpdate( dto, sprint );

        if ( dto.name() != null ) {
            sprint.setName( dto.name() );
        }
        if ( dto.startDate() != null ) {
            sprint.setStartDate( dto.startDate() );
        }
        if ( dto.endDate() != null ) {
            sprint.setEndDate( dto.endDate() );
        }
        if ( dto.goal() != null ) {
            sprint.setGoal( dto.goal() );
        }

        afterUpdate( dto, sprint );
    }

    @Override
    public SprintDtos.Response toResponse(Sprint sprint) {
        if ( sprint == null ) {
            return null;
        }

        UUID id = null;
        String name = null;
        LocalDate startDate = null;
        LocalDate endDate = null;
        Integer durationDays = null;
        String goal = null;

        if ( sprint.getId() != null ) {
            id = sprint.getId();
        }
        if ( sprint.getName() != null ) {
            name = sprint.getName();
        }
        if ( sprint.getStartDate() != null ) {
            startDate = sprint.getStartDate();
        }
        if ( sprint.getEndDate() != null ) {
            endDate = sprint.getEndDate();
        }
        if ( sprint.getDurationDays() != null ) {
            durationDays = sprint.getDurationDays();
        }
        if ( sprint.getGoal() != null ) {
            goal = sprint.getGoal();
        }

        long tasksCount = sprint.getTasks() != null ? sprint.getTasks().size() : 0L;

        SprintDtos.Response response = new SprintDtos.Response( id, name, startDate, endDate, durationDays, goal, tasksCount );

        return response;
    }
}
