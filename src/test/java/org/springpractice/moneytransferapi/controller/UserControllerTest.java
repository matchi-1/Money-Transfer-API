package org.springpractice.moneytransferapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springpractice.moneytransferapi.entity.User;
import org.springpractice.moneytransferapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(UserControllerTest.MockedServiceConfig.class)
class UserControllerTest {

    @TestConfiguration
    static class MockedServiceConfig {
        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testGetAllUsers() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Ana");
        user.setLastName("Ng");
        user.setEmail("ana@example.com");
        user.setBalance(BigDecimal.TEN);

        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Ana"));
    }

    @Test
    void testGetUserById() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Bob");
        user.setEmail("bob@example.com");

        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("bob@example.com"));
    }

    @Test
    void testCreateUser() throws Exception {
        User user = new User();
        user.setId(2L);
        user.setFirstName("Eve");
        user.setLastName("Lopez");
        user.setEmail("eve@example.com");
        user.setBalance(BigDecimal.valueOf(300));

        when(userService.createUser(Mockito.any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("eve@example.com"));
    }

    @Test
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }
}
