package org.springpractice.moneytransferapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springpractice.moneytransferapi.entity.User;
import org.springpractice.moneytransferapi.repository.UserRepo;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setEmail("john@example.com");
        mockUser.setBalance(BigDecimal.valueOf(1000));
    }

    @Test
    void testGetUserById_found() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(mockUser));
        User result = userService.getUserById(1L);
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    void testGetUserById_notFound() {
        when(userRepo.findById(2L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(2L));
    }

    @Test
    void testCreateUser_withNullBalance_setsZero() {
        mockUser.setBalance(null);
        when(userRepo.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            assertEquals(BigDecimal.ZERO, u.getBalance());
            return u;
        });
        userService.createUser(mockUser);
    }

    @Test
    void testUpdateUser() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User update = new User();
        update.setFirstName("Jane");
        update.setLastName("Smith");
        update.setEmail("jane@example.com");
        update.setBalance(BigDecimal.valueOf(2000));

        User updated = userService.updateUser(1L, update);

        assertEquals("Jane", updated.getFirstName());
        assertEquals("jane@example.com", updated.getEmail());
    }
}
