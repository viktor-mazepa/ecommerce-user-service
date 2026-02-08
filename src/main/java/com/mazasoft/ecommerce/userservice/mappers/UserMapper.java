package com.mazasoft.ecommerce.userservice.mappers;

import com.mazasoft.ecommerce.userservice.dto.CreateUserAdmin;
import com.mazasoft.ecommerce.userservice.dto.UpdateUserAdmin;
import com.mazasoft.ecommerce.userservice.dto.UserAdminResponse;
import com.mazasoft.ecommerce.userservice.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(CreateUserAdmin request) {
        if (request == null) {
            return null;
        }
        User user = new User();
        user.setUserName(request.userName());
        user.setEmail(request.email());
        user.setAvatar(request.avatar());
        user.setFistName(request.firstName());
        user.setLastName(request.lastName());
        user.setBirthDate(request.birthDate());
        user.setPhoneNumber(request.phoneNumber());
        user.setRole(request.role());
        return user;
    }

    public UserAdminResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        return new UserAdminResponse(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getAvatar(),
                user.getFistName(),
                user.getLastName(),
                user.getBirthDate(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getKcId()
        );
    }

    public User toEntity(UpdateUserAdmin request) {
        if (request == null) {
            return null;
        }
        User user = new User();
        user.setUserName(request.userName());
        user.setEmail(request.email());
        user.setAvatar(request.avatar());
        user.setFistName(request.firstName());
        user.setLastName(request.lastName());
        user.setBirthDate(request.birthDate());
        user.setPhoneNumber(request.phoneNumber());
        user.setRole(request.role());
        return user;
    }
}
