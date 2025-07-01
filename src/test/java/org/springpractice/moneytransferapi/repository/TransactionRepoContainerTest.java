package org.springpractice.moneytransferapi.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springpractice.moneytransferapi.entity.Transaction;
import org.springpractice.moneytransferapi.entity.User;
import org.springpractice.moneytransferapi.enums.TransactionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Testcontainers
public class TransactionRepoContainerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.3")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private UserRepo userRepo;

    @Test
    void testFindByReceiverId_withContainer() {
        User sender = new User();
        sender.setFirstName("Sender");
        sender.setLastName("Test");
        sender.setEmail("sender@example.com");
        sender.setBalance(BigDecimal.valueOf(1000));
        sender = userRepo.save(sender);

        User receiver = new User();
        receiver.setFirstName("Receiver");
        receiver.setLastName("Test");
        receiver.setEmail("receiver@example.com");
        receiver.setBalance(BigDecimal.valueOf(1000));
        receiver = userRepo.save(receiver);

        Transaction tx = new Transaction();
        tx.setSender(sender);
        tx.setReceiver(receiver);
        tx.setAmount(BigDecimal.valueOf(150));
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepo.save(tx);

        List<Transaction> result = transactionRepo.findByReceiverId(receiver.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo("150");
    }
}
