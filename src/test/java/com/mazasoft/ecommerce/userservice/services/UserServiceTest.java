package com.mazasoft.ecommerce.userservice.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mazasoft.ecommerce.userservice.entities.User;
import com.mazasoft.ecommerce.userservice.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void update_throwsWhenIdMissing() {
        User user = new User();

        assertThatThrownBy(() -> userService.update(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User id is required");
    }

    @Test
    void update_throwsWhenNotFound() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(user))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void update_updatesPersistedFields() {
        UUID id = UUID.randomUUID();
        User existing = new User();
        existing.setId(id);
        existing.setRole("USER");

        User update = new User();
        update.setId(id);
        update.setUserName("jdoe");
        update.setEmail("jdoe@example.com");
        update.setAvatar("avatar.png");
        update.setFistName("John");
        update.setLastName("Doe");
        update.setBirthDate(LocalDate.of(1990, 1, 1));
        update.setPhoneNumber("123456789");
        update.setRole("ADMIN");

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.update(update);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.getUserName()).isEqualTo("jdoe");
        assertThat(saved.getEmail()).isEqualTo("jdoe@example.com");
        assertThat(saved.getAvatar()).isEqualTo("avatar.png");
        assertThat(saved.getFistName()).isEqualTo("John");
        assertThat(saved.getLastName()).isEqualTo("Doe");
        assertThat(saved.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(saved.getPhoneNumber()).isEqualTo("123456789");
        assertThat(saved.getRole()).isEqualTo("ADMIN");
        assertThat(result).isEqualTo(saved);
    }
}
