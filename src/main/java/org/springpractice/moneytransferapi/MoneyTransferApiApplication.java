package org.springpractice.moneytransferapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MoneyTransferApiApplication {

    public static void main(String[] args) {
        System.out.println("DB URL = " + System.getenv("DB_URL"));
        SpringApplication.run(MoneyTransferApiApplication.class, args);
    }

}
