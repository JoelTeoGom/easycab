package org.example.ec_de.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Properties;

/**
 * Service class for waiting for the start signal from a Kafka topic.
 */
@Slf4j
@Service
public class KafkaWaitForStart {

    /**
     * The unique identifier of the taxi, injected from application properties.
     */
    @Value("${taxi.id}")
    private String taxiId;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    /**
     * KafkaConsumer for consuming messages from Kafka topics.
     */
    private KafkaConsumer<String, String> consumer;

    /**
     * Constructor for KafkaWaitForStart.
     * Initializes the Kafka consumer with the necessary properties and subscribes to the topic.
     */
    @PostConstruct
    public void initializeConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        consumer = new KafkaConsumer<>(props);
        log.info("Consumer created with bootstrap servers: {}", bootstrapServers);
        log.info("Subscribed to topic: taxi-start-service-{}", taxiId);
        consumer.subscribe(Collections.singletonList("taxi-start-service-" + taxiId));
    }

}