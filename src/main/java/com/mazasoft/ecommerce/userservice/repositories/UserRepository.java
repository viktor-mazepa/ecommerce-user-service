package com.mazasoft.ecommerce.userservice.repositories;

import com.mazasoft.ecommerce.userservice.entities.User;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}
