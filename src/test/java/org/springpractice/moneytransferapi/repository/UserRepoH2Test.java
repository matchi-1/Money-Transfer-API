package org.springpractice.moneytransferapi.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springpractice.moneytransferapi.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class UserRepoH2Test {

    @Autowired
    private UserRepo userRepo;

    @Test
    void testCreateUser() {
        User user = new User();
        user.setFirstName("Alice");
        user.setLastName("Smith");
        user.setEmail("alice@example.com");
        user.setBalance(BigDecimal.valueOf(1000));

        User saved = userRepo.save(user);
        assertThat(saved.getId()).isNotNull();
    }
}