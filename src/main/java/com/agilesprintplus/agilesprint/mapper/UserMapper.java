package com.agilesprintplus.agilesprint.mapper;

import com.agilesprintplus.agilesprint.api.dto.UserDtos;
import com.agilesprintplus.agilesprint.domain.Role;
import com.agilesprintplus.agilesprint.domain.Task;
import com.agilesprintplus.agilesprint.domain.User;
import com.agilesprintplus.agilesprint.repo.TaskRepository;
import org.springframework.data.domain.Page;
import org.mapstruct.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

  @Mapping(target = "taskIds", ignore = true)
  @Mapping(target = "passwordChangeRequired", source = "passwordChangeRequired")
  UserDtos.Response toResponse(User user);

  default Page<UserDtos.Response> toResponsePage(Page<User> page) {
    return page.map(this::toResponse);
  }

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "passwordHash", ignore = true)
  @Mapping(target = "enabled", constant = "true")
  @Mapping(target = "passwordChangeRequired", constant = "true")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "tasks", ignore = true)
  User toEntity(UserDtos.Create dto, @Context PasswordEncoder encoder);
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "passwordHash", ignore = true)
  @Mapping(target = "enabled", constant = "true")
  @Mapping(target = "passwordChangeRequired", constant = "true")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "tasks", ignore = true)
  @Mapping(target = "tokenList", ignore = true)
  User toEntityWithDefaultPassword(UserDtos.CreateWithDefaultPassword dto, @Context PasswordEncoder encoder);

  @BeanMapping(ignoreByDefault = true, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mappings({
          @Mapping(target = "email", source = "email"),
          @Mapping(target = "firstName", source = "firstName"),
          @Mapping(target = "lastName", source = "lastName"),
          @Mapping(target = "roles", source = "roles"),
          @Mapping(target = "enabled", source = "enabled")
  })
  void updateEntity(@MappingTarget User user, UserDtos.Update dto, @Context TaskRepository taskRepo);

  @AfterMapping
  default void afterCreate(UserDtos.Create dto,
                           @MappingTarget User user,
                           @Context PasswordEncoder encoder) {
    normalizeUser(user);
    if (user.getRoles() == null || user.getRoles().isEmpty()) {
      user.setRoles(Set.of(Role.DEVELOPER));
      user.setTokenList(new ArrayList<>());
    }
  }
  @AfterMapping
  default void afterCreateWithDefaultPassword(UserDtos.CreateWithDefaultPassword dto,
                                              @MappingTarget User user,
                                              @Context PasswordEncoder encoder) {
    user.setPasswordHash(encoder.encode("kamer237"));
    normalizeUser(user);
    user.setRoles(Set.of(dto.role()));
  }
  @AfterMapping
  default void afterUpdate(UserDtos.Update dto,
                           @MappingTarget User user,
                           @Context TaskRepository taskRepo) {
    normalizeUser(user);
    if (dto.taskIds() != null) {
      Set<Task> newTasks = new HashSet<>(taskRepo.findAllById(dto.taskIds()));
      user.getTasks().clear();
      user.getTasks().addAll(newTasks);
    }
  }
  private static void normalizeUser(User user) {
    if (user.getEmail() != null) user.setEmail(user.getEmail().trim().toLowerCase());
    user.setFirstName(org.apache.commons.lang3.StringUtils.trimToNull(user.getFirstName()));
    user.setLastName(org.apache.commons.lang3.StringUtils.trimToNull(user.getLastName()));
  }
}
