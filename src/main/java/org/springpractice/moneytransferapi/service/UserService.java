package org.springpractice.moneytransferapi.service;

import org.springpractice.moneytransferapi.entity.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();

    User getUserById(Long id);

    User createUser(User user);

    User updateUser(Long id, User updatedUser);

    void deleteUser(Long id);
}
