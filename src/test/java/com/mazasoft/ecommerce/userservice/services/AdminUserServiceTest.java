package com.mazasoft.ecommerce.userservice.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mazasoft.ecommerce.userservice.dto.CreateUserAdmin;
import com.mazasoft.ecommerce.userservice.dto.UpdateUserAdmin;
import com.mazasoft.ecommerce.userservice.dto.UserAdminResponse;
import com.mazasoft.ecommerce.userservice.entities.User;
import com.mazasoft.ecommerce.userservice.mappers.UserMapper;
import com.mazasoft.ecommerce.userservice.ports.IdentityAdminPort;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private IdentityAdminPort adminClient;

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AdminUserService adminUserService;

    @Test
    void create_assignsRoleAndUpdatesUser() {
        CreateUserAdmin request = new CreateUserAdmin(
                "jdoe",
                "jdoe@example.com",
                null,
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "123456789",
                "ADMIN"
        );
        User user = new User();
        IdentityAdminPort.RoleRepresentation role = new IdentityAdminPort.RoleRepresentation("1", "ADMIN");
        UUID kcId = UUID.randomUUID();

        when(userMapper.toEntity(request)).thenReturn(user);
        doAnswer(inv -> {
            user.setId(UUID.randomUUID());
            return user;
        }).when(userService).create(user);
        when(adminClient.getRealmRoleByName("ADMIN")).thenReturn(role);
        when(adminClient.createUser(any())).thenReturn(kcId.toString());
        when(userService.update(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(new UserAdminResponse(
                user.getId(),
                request.userName(),
                request.email(),
                request.avatar(),
                request.firstName(),
                request.lastName(),
                request.birthDate(),
                request.phoneNumber(),
                request.role(),
                kcId
        ));

        UserAdminResponse response = adminUserService.create(request);

        assertThat(response.kcId()).isEqualTo(kcId);
        verify(adminClient).assignRealmRoles(eq(kcId.toString()), eq(List.of(role)));
        verify(userService).update(user);
    }

    @Test
    void update_replacesRoleWhenChanged() {
        UUID userId = UUID.randomUUID();
        UUID kcId = UUID.randomUUID();
        User existing = new User();
        existing.setId(userId);
        existing.setKcId(kcId);
        existing.setRole("USER");

        UpdateUserAdmin request = new UpdateUserAdmin(
                "jdoe",
                "jdoe@example.com",
                null,
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "123456789",
                "ADMIN"
        );
        User updated = new User();
        IdentityAdminPort.RoleRepresentation role = new IdentityAdminPort.RoleRepresentation("1", "ADMIN");

        when(userService.getById(userId)).thenReturn(existing);
        when(adminClient.getRealmRoleByName("ADMIN")).thenReturn(role);
        when(userMapper.toEntity(request)).thenReturn(updated);
        when(userService.update(updated)).thenReturn(updated);
        when(userMapper.toResponse(updated)).thenReturn(new UserAdminResponse(
                userId,
                request.userName(),
                request.email(),
                request.avatar(),
                request.firstName(),
                request.lastName(),
                request.birthDate(),
                request.phoneNumber(),
                request.role(),
                kcId
        ));

        adminUserService.update(userId, request);

        verify(adminClient).updateUser(eq(kcId), any());
        verify(adminClient).replaceRealmRoles(eq(kcId), eq(List.of(role)));
    }
}
