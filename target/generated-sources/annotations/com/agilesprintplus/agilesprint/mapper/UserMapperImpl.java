package com.agilesprintplus.agilesprint.mapper;

import com.agilesprintplus.agilesprint.api.dto.UserDtos;
import com.agilesprintplus.agilesprint.domain.Role;
import com.agilesprintplus.agilesprint.domain.User;
import com.agilesprintplus.agilesprint.repo.TaskRepository;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-07T10:05:56+0400",
    comments = "version: 1.6.2, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDtos.Response toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        boolean passwordChangeRequired = false;
        UUID id = null;
        String username = null;
        String email = null;
        String firstName = null;
        String lastName = null;
        Set<Role> roles = null;
        boolean enabled = false;
        Instant createdAt = null;
        Instant updatedAt = null;

        passwordChangeRequired = user.isPasswordChangeRequired();
        if ( user.getId() != null ) {
            id = user.getId();
        }
        if ( user.getUsername() != null ) {
            username = user.getUsername();
        }
        if ( user.getEmail() != null ) {
            email = user.getEmail();
        }
        if ( user.getFirstName() != null ) {
            firstName = user.getFirstName();
        }
        if ( user.getLastName() != null ) {
            lastName = user.getLastName();
        }
        Set<Role> set = user.getRoles();
        if ( set != null ) {
            roles = new LinkedHashSet<Role>( set );
        }
        enabled = user.isEnabled();
        if ( user.getCreatedAt() != null ) {
            createdAt = user.getCreatedAt();
        }
        if ( user.getUpdatedAt() != null ) {
            updatedAt = user.getUpdatedAt();
        }

        Set<UUID> taskIds = null;

        UserDtos.Response response = new UserDtos.Response( id, username, email, firstName, lastName, roles, enabled, passwordChangeRequired, createdAt, updatedAt, taskIds );

        return response;
    }

    @Override
    public User toEntity(UserDtos.Create dto, PasswordEncoder encoder) {
        if ( dto == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        if ( dto.username() != null ) {
            user.username( dto.username() );
        }
        if ( dto.email() != null ) {
            user.email( dto.email() );
        }
        if ( dto.firstName() != null ) {
            user.firstName( dto.firstName() );
        }
        if ( dto.lastName() != null ) {
            user.lastName( dto.lastName() );
        }

        user.enabled( true );
        user.passwordChangeRequired( true );

        User userResult = user.build();

        afterCreate( dto, userResult, encoder );

        return userResult;
    }

    @Override
    public User toEntityWithDefaultPassword(UserDtos.CreateWithDefaultPassword dto, PasswordEncoder encoder) {
        if ( dto == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        if ( dto.username() != null ) {
            user.username( dto.username() );
        }
        if ( dto.email() != null ) {
            user.email( dto.email() );
        }
        if ( dto.firstName() != null ) {
            user.firstName( dto.firstName() );
        }
        if ( dto.lastName() != null ) {
            user.lastName( dto.lastName() );
        }

        user.enabled( true );
        user.passwordChangeRequired( true );

        User userResult = user.build();

        afterCreateWithDefaultPassword( dto, userResult, encoder );

        return userResult;
    }

    @Override
    public void updateEntity(User user, UserDtos.Update dto, TaskRepository taskRepo) {
        if ( dto == null ) {
            return;
        }

        if ( dto.email() != null ) {
            user.setEmail( dto.email() );
        }
        if ( dto.firstName() != null ) {
            user.setFirstName( dto.firstName() );
        }
        if ( dto.lastName() != null ) {
            user.setLastName( dto.lastName() );
        }
        if ( user.getRoles() != null ) {
            Set<Role> set = dto.roles();
            if ( set != null ) {
                user.getRoles().clear();
                user.getRoles().addAll( set );
            }
        }
        else {
            Set<Role> set = dto.roles();
            if ( set != null ) {
                user.setRoles( new LinkedHashSet<Role>( set ) );
            }
        }
        if ( dto.enabled() != null ) {
            user.setEnabled( dto.enabled() );
        }

        afterUpdate( dto, user, taskRepo );
    }
}
