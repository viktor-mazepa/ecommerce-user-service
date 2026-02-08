package com.mazasoft.ecommerce.userservice.services;

import com.mazasoft.ecommerce.userservice.entities.User;
import com.mazasoft.ecommerce.userservice.repositories.UserRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User create(User user) {
        return userRepository.save(user);
    }



    @Transactional
    public User update(User user) {
        UUID id = user.getId();
        if (id == null) {
            throw new IllegalArgumentException("User id is required for update");
        }
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));

        existing.setAvatar(user.getAvatar());
        existing.setFistName(user.getFistName());
        existing.setLastName(user.getLastName());
        existing.setUserName(user.getUserName());
        existing.setEmail(user.getEmail());
        existing.setBirthDate(user.getBirthDate());
        existing.setPhoneNumber(user.getPhoneNumber());
        if (user.getKcId() != null) {
            existing.setKcId(user.getKcId());
        }
        if (StringUtils.isNotEmpty(user.getRole())){
            existing.setRole(user.getRole());
        }
        existing.setUpdatedAt(user.getUpdatedAt());
        return userRepository.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public User getById(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }

    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("User not found by email: " + email));
    }
}
