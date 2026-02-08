package com.mazasoft.ecommerce.userservice.contollers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mazasoft.ecommerce.userservice.controllers.AdminUserController;
import com.mazasoft.ecommerce.userservice.dto.CreateUserAdmin;
import com.mazasoft.ecommerce.userservice.dto.UserAdminResponse;
import com.mazasoft.ecommerce.userservice.services.AdminUserService;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminUserService adminUserService;

    @Test
    void create_returnsOkForValidRequest() throws Exception {
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
        when(adminUserService.create(any())).thenReturn(new UserAdminResponse(
                UUID.randomUUID(),
                request.userName(),
                request.email(),
                request.avatar(),
                request.firstName(),
                request.lastName(),
                request.birthDate(),
                request.phoneNumber(),
                request.role(),
                UUID.randomUUID()
        ));

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void create_returnsBadRequestWhenMissingRequiredFields() throws Exception {
        CreateUserAdmin request = new CreateUserAdmin(
                "",
                "",
                null,
                "",
                "",
                null,
                null,
                ""
        );

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
