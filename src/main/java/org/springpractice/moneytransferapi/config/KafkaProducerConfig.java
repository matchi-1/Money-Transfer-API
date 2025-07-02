package org.springpractice.moneytransferapi.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springpractice.moneytransferapi.dto.TransactionEventDTO;
import org.springpractice.moneytransferapi.dto.TransactionRequestEvent;
import org.springpractice.moneytransferapi.dto.TransactionResponseEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    private Map<String, Object> producerConfigs() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return config;
    }

    @Bean
    public ProducerFactory<String, TransactionRequestEvent> requestProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, TransactionRequestEvent> transactionRequestKafkaTemplate() {
        return new KafkaTemplate<>(requestProducerFactory());
    }

    @Bean
    public ProducerFactory<String, TransactionResponseEvent> responseProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, TransactionResponseEvent> transactionResponseKafkaTemplate() {
        return new KafkaTemplate<>(responseProducerFactory());
    }

    @Bean
    public ProducerFactory<String, TransactionEventDTO> eventProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, TransactionEventDTO> transactionEventKafkaTemplate() {
        return new KafkaTemplate<>(eventProducerFactory());
    }
}

