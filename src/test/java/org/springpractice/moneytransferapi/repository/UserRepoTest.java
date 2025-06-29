package org.springpractice.moneytransferapi.repository;

import org.junit.jupiter.api.Test;
import org.springpractice.moneytransferapi.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepoTest {

    @Autowired
    private UserRepo userRepo;

    @Test
    void testSaveAndFindUser() {
        User user = new User();
        user.setFirstName("Ana");
        user.setLastName("Ng");
        user.setEmail("ana@example.com");
        user.setBalance(BigDecimal.valueOf(500));

        User saved = userRepo.save(user);
        Optional<User> retrieved = userRepo.findById(saved.getId());

        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getEmail()).isEqualTo("ana@example.com");
    }
}
