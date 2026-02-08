package com.mazasoft.ecommerce.userservice.services;

import com.mazasoft.ecommerce.userservice.dto.CreateUserAdmin;
import com.mazasoft.ecommerce.userservice.dto.UpdateUserAdmin;
import com.mazasoft.ecommerce.userservice.dto.UserAdminResponse;
import com.mazasoft.ecommerce.userservice.entities.User;
import com.mazasoft.ecommerce.userservice.exceptions.ApiException;
import com.mazasoft.ecommerce.userservice.mappers.UserMapper;
import com.mazasoft.ecommerce.userservice.ports.IdentityAdminPort;
import io.micrometer.common.util.StringUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AdminUserService {

    private final IdentityAdminPort adminClient;
    private final UserService userService;
    private final UserMapper userMapper;

    public AdminUserService(IdentityAdminPort adminClient, UserService userService, UserMapper userMapper) {
        this.adminClient = adminClient;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    public UserAdminResponse create(CreateUserAdmin createUserAdmin) {
        IdentityAdminPort.RoleRepresentation rRole = validateRoleChange(null, createUserAdmin.role());
        User newUser = userMapper.toEntity(createUserAdmin);
        userService.create(newUser);
        String kcId = adminClient.createUser(new IdentityAdminPort.CreateUserCommand(
                createUserAdmin.userName(),
                createUserAdmin.email(),
                createUserAdmin.avatar(),
                createUserAdmin.firstName(),
                createUserAdmin.lastName(),
                createUserAdmin.birthDate(),
                createUserAdmin.phoneNumber()
        ));
        newUser.setKcId(UUID.fromString(kcId));
        if (StringUtils.isNotEmpty(createUserAdmin.role())) {
            adminClient.assignRealmRoles(kcId, List.of(rRole));
        }
        userService.update(newUser);
        return userMapper.toResponse(newUser);
    }

    public UserAdminResponse update(UUID id, UpdateUserAdmin updateUserAdmin) {
        User user = userService.getById(id);
        IdentityAdminPort.RoleRepresentation rRole = validateRoleChange(user.getRole(), updateUserAdmin.role());
        adminClient.updateUser(user.getKcId(), new IdentityAdminPort.UpdateUserCommand(
                updateUserAdmin.email(),
                updateUserAdmin.firstName(),
                updateUserAdmin.lastName()
        ));
        if (StringUtils.isNotEmpty(updateUserAdmin.role()) && !user.getRole().equals(updateUserAdmin.role())) {
            adminClient.replaceRealmRoles(user.getKcId(), List.of(rRole));
        }
        User uUser = userMapper.toEntity(updateUserAdmin);
        uUser.setId(id);
        uUser.setUpdatedAt(LocalDateTime.now());
        User updated = userService.update(uUser);
        return userMapper.toResponse(updated);
    }

    public void enable(UUID id) {
        User user = userService.getById(id);
        adminClient.setEnabled(user.getKcId(), true);
    }

    public void disable(UUID id) {
        User user = userService.getById(id);
        adminClient.setEnabled(user.getKcId(), false);
    }

    public void delete(UUID id) {
        User user = userService.getById(id);
        adminClient.deleteUser(user.getKcId());
        userService.delete(id);
    }

    private IdentityAdminPort.RoleRepresentation validateRoleChange(String currentRole, String newRole) {
        if (StringUtils.isBlank(newRole)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Role is required");
        }

        IdentityAdminPort.RoleRepresentation role = adminClient.getRealmRoleByName(newRole);
        if (role == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Unknown role: " + newRole);
        }

        if (currentRole != null && !isAllowedTransition(currentRole, newRole)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Role change not allowed");
        }
        return role;
    }

    private boolean isAllowedTransition(String from, String to) {
        return !from.equals("ADMIN") || !to.equals("USER");
    }
}
