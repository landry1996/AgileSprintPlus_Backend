package com.agilesprintplus.agilesprint.service.impl;

import com.agilesprintplus.agilesprint.api.dto.UserDtos;
import com.agilesprintplus.agilesprint.domain.User;
import com.agilesprintplus.agilesprint.exception.BadRequestException;
import com.agilesprintplus.agilesprint.exception.ConflictException;
import com.agilesprintplus.agilesprint.exception.NotFoundException;
import com.agilesprintplus.agilesprint.mapper.UserMapper;
import com.agilesprintplus.agilesprint.repo.TaskRepository;
import com.agilesprintplus.agilesprint.repo.UserRepository;
import com.agilesprintplus.agilesprint.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repo;
    private final UserMapper mapper;
    private final PasswordEncoder encoder;
    private final TaskRepository taskRepo;

    @Override
    public UserDtos.Response create(UserDtos.Create dto) {
        final String username = dto.username().trim();
        final String email = dto.email().trim().toLowerCase(Locale.ROOT);

        if (repo.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("Username already exists: " + username);
        }
        if (repo.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email already used: " + email);
        }

        User user = mapper.toEntity(dto, encoder);

        return mapper.toResponse(repo.save(user));
    }
    @Override
    @Transactional(readOnly = true)
    public UserDtos.Response getById(UUID id) {
        return mapper.toResponse(findOr404(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDtos.Response> list(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    public UserDtos.Response update(UUID id, UserDtos.Update dto) {
        User user = findOr404(id);

        if (dto.email() != null) {
            String newEmail = dto.email().trim().toLowerCase(Locale.ROOT);
            if (!newEmail.equals(user.getEmail()) && repo.existsByEmailIgnoreCase(newEmail)) {
                throw new ConflictException("Email already used: " + newEmail);
            }
        }

        mapper.updateEntity(user, dto,taskRepo);
        return mapper.toResponse(user);
    }
    @Override
    public void delete(UUID id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("User not found: " + id);
        }
        repo.deleteById(id);
    }
    @Override
    public void changePassword(UUID id, UserDtos.ChangePassword dto) {
        User user = findOr404(id);
        if (!encoder.matches(dto.oldPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Old password is incorrect");
        }
        user.setPasswordHash(encoder.encode(dto.newPassword()));
    }
    @Override
    public UserDtos.Response toggleActive(UUID id, boolean enabled) {
        User user = findOr404(id);
        user.setEnabled(enabled);
        return mapper.toResponse(user);
    }
    @Override
    @Transactional(readOnly = true)
    public UserDtos.Response getByUsername(String username) {
        User user = repo.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
        return mapper.toResponse(user);
    }
    private User findOr404(UUID id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("User not found: " + id));
    }
}
