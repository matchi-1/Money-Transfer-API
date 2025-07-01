package org.springpractice.moneytransferapi.service.impl;

import org.springpractice.moneytransferapi.entity.User;
import org.springpractice.moneytransferapi.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springpractice.moneytransferapi.service.UserService;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;

    public UserServiceImpl(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @Override public User getUserById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    @Override public User createUser(User user) {
        user.setBalance(user.getBalance() == null ? java.math.BigDecimal.ZERO : user.getBalance());
        return userRepo.save(user);
    }

    @Override public User updateUser(Long id, User updatedUser) {
        User existingUser = getUserById(id);

        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setBalance(updatedUser.getBalance());

        return userRepo.save(existingUser);
    }

    @Override public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepo.delete(user);
    }
}
