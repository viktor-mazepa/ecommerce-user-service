package com.mazasoft.ecommerce.userservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mazasoft.ecommerce.userservice.dto.CreateUserAdmin;
import com.mazasoft.ecommerce.userservice.dto.UpdateUserAdmin;
import com.mazasoft.ecommerce.userservice.dto.UserAdminResponse;
import com.mazasoft.ecommerce.userservice.dto.event.UserProvisionRequestedPayload;
import com.mazasoft.ecommerce.userservice.entities.OutboxEvent;
import com.mazasoft.ecommerce.userservice.entities.User;
import com.mazasoft.ecommerce.userservice.enums.EventType;
import com.mazasoft.ecommerce.userservice.enums.OutboxEventStatus;
import com.mazasoft.ecommerce.userservice.mappers.UserMapper;
import com.mazasoft.ecommerce.userservice.ports.IdentityAdminPort;
import com.mazasoft.ecommerce.userservice.repositories.EventRepository;
import com.mazasoft.ecommerce.userservice.services.database.UserService;
import com.mazasoft.ecommerce.userservice.utils.EventPayloadGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class AdminUserService {

    private final UserService userService;
    private final UserMapper userMapper;
    private final EventPayloadGenerator payloadGenerator;
    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;

    public AdminUserService(IdentityAdminPort adminClient, UserService userService, UserMapper userMapper, EventPayloadGenerator payloadGenerator, EventRepository eventRepository, ObjectMapper objectMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.payloadGenerator = payloadGenerator;
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
    }

    public UserAdminResponse create(CreateUserAdmin createUserAdmin) {
        User newUser = userMapper.toEntity(createUserAdmin);
        newUser = userService.create(newUser);
        UserProvisionRequestedPayload payload = payloadGenerator.generateCreateUserPayload(newUser.getId(), createUserAdmin);
        OutboxEvent outboxEvent = getEvent(payload, EventType.USER_CREATE);
        eventRepository.save(outboxEvent);

       /* IdentityAdminPort.RoleRepresentation rRole = validateRoleChange(null, createUserAdmin.role());
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
        userService.update(newUser);*/
        return userMapper.toResponse(newUser);
    }

    public UserAdminResponse update(UUID id, UpdateUserAdmin updateUserAdmin) {
        //User user = userService.getById(id);
        User user = userMapper.toEntity(updateUserAdmin);
        user.setId(id);
        user.setUpdatedAt(LocalDateTime.now());
        User updated = userService.update(user);

        UserProvisionRequestedPayload payload = payloadGenerator.generateUpdateUserPayload(user.getId(), updateUserAdmin);
        OutboxEvent outboxEvent = getEvent(payload, EventType.USER_UPDATE);
        eventRepository.save(outboxEvent);

        /*IdentityAdminPort.RoleRepresentation rRole = validateRoleChange(user.getRole(), updateUserAdmin.role());
        adminClient.updateUser(user.getKcId(), new IdentityAdminPort.UpdateUserCommand(
                updateUserAdmin.email(),
                updateUserAdmin.firstName(),
                updateUserAdmin.lastName()
        ));
        if (StringUtils.isNotEmpty(updateUserAdmin.role()) && !user.getRole().equals(updateUserAdmin.role())) {
            adminClient.replaceRealmRoles(user.getKcId(), List.of(rRole));
        }*/
        return userMapper.toResponse(updated);
    }

    public void enable(UUID id) {
        User user = userService.enable(id);
        UserProvisionRequestedPayload payload = payloadGenerator.generateEnableUserPayload(user);
        OutboxEvent outboxEvent = getEvent(payload, EventType.USER_ENABLE);
        eventRepository.save(outboxEvent);
        /*adminClient.setEnabled(user.getKcId(), true);*/
    }

    public void disable(UUID id) {
        User user = userService.disable(id);
        UserProvisionRequestedPayload payload = payloadGenerator.generateDisableUserPayload(user);
        OutboxEvent outboxEvent = getEvent(payload, EventType.USER_DISABLE);
        eventRepository.save(outboxEvent);
        /* adminClient.setEnabled(user.getKcId(), false);*/
    }

    public void delete(UUID id) {
        User user = userService.getById(id);
        userService.delete(id);
        UserProvisionRequestedPayload payload = payloadGenerator.generateDeleteUserPayload(user);
        OutboxEvent outboxEvent = getEvent(payload, EventType.USER_DELETE);
        eventRepository.save(outboxEvent);

        /* adminClient.deleteUser(user.getKcId());*/

    }

    /*private IdentityAdminPort.RoleRepresentation validateRoleChange(String currentRole, String newRole) {
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
    }*/

    private boolean isAllowedTransition(String from, String to) {
        return !from.equals("ADMIN") || !to.equals("USER");
    }

    private OutboxEvent getEvent(UserProvisionRequestedPayload payload, EventType eventType) {
        OutboxEvent event = new OutboxEvent();
        event.setId(UUID.randomUUID());
        event.setAggregateType("USER");
        event.setAggregateId(payload.externalId());
        event.setEventType(eventType);
        event.setPayload(objectMapper.valueToTree(payload));
        event.setStatus(OutboxEventStatus.NEW);
        return event;

    }
}
