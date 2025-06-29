package org.springpractice.moneytransferapi.service;

import org.springpractice.moneytransferapi.entity.User;
import org.springpractice.moneytransferapi.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepo userRepo;

    @Autowired
    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public User getUserById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    public User createUser(User user) {
        user.setBalance(user.getBalance() == null ? java.math.BigDecimal.ZERO : user.getBalance());
        return userRepo.save(user);
    }

    public User updateUser(Long id, User updatedUser) {
        User existingUser = getUserById(id);

        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setBalance(updatedUser.getBalance());

        return userRepo.save(existingUser);
    }

    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepo.delete(user);
    }
}
