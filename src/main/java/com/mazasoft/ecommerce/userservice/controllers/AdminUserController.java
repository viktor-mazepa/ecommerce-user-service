package com.mazasoft.ecommerce.userservice.controllers;

import com.mazasoft.ecommerce.userservice.dto.CreateUserAdmin;
import com.mazasoft.ecommerce.userservice.dto.UpdateUserAdmin;
import com.mazasoft.ecommerce.userservice.dto.UserAdminResponse;
import com.mazasoft.ecommerce.userservice.services.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PostMapping
    public ResponseEntity<UserAdminResponse> create(@Valid @RequestBody CreateUserAdmin createUserAdmin) {
        return ResponseEntity.ok(adminUserService.create(createUserAdmin));

    }

    @PutMapping("/{id}")
    public ResponseEntity<UserAdminResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateUserAdmin updateUserAdmin) {
        return ResponseEntity.ok(adminUserService.update(id, updateUserAdmin));
    }

    @PostMapping("/{id}/enable")
    public ResponseEntity<Void> enable(@PathVariable UUID id) {
        adminUserService.enable(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/disable")
    public ResponseEntity<Void> disable(@PathVariable UUID id) {
        adminUserService.disable(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        adminUserService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
