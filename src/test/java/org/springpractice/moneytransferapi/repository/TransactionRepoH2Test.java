package org.springpractice.moneytransferapi.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springpractice.moneytransferapi.entity.Transaction;
import org.springpractice.moneytransferapi.entity.User;
import org.springpractice.moneytransferapi.enums.TransactionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class TransactionRepoH2Test {

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private UserRepo userRepo;

    @Test
    void testFindBySenderId_withH2() {
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
        tx.setAmount(BigDecimal.valueOf(100));
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepo.save(tx);

        List<Transaction> result = transactionRepo.findBySenderId(sender.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo("100");
    }
}
