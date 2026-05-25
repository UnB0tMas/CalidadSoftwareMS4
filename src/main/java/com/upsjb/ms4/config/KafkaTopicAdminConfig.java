package com.upsjb.ms4.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicAdminConfig {

    private static final int LOCAL_PARTITIONS = 3;
    private static final int LOCAL_REPLICAS = 1;

    @Bean
    public NewTopic ms2ClienteSnapshotTopic(KafkaTopicProperties properties) {
        return topic(properties.clienteSnapshotTopic());
    }

    @Bean
    public NewTopic ms2EmpleadoSnapshotTopic(KafkaTopicProperties properties) {
        return topic(properties.empleadoSnapshotTopic());
    }

    @Bean
    public NewTopic ms3ProductoSnapshotTopic(KafkaTopicProperties properties) {
        return topic(properties.productoSnapshotTopic());
    }

    @Bean
    public NewTopic ms3PrecioSnapshotTopic(KafkaTopicProperties properties) {
        return topic(properties.precioSnapshotTopic());
    }

    @Bean
    public NewTopic ms3PromocionSnapshotTopic(KafkaTopicProperties properties) {
        return topic(properties.promocionSnapshotTopic());
    }

    @Bean
    public NewTopic ms3StockSnapshotTopic(KafkaTopicProperties properties) {
        return topic(properties.stockSnapshotTopic());
    }

    @Bean
    public NewTopic ms3MovimientoInventarioTopic(KafkaTopicProperties properties) {
        return topic(properties.movimientoInventarioTopic());
    }

    @Bean
    public NewTopic ms4StockCommandTopic(KafkaTopicProperties properties) {
        return topic(properties.stockCommandTopic());
    }

    @Bean
    public NewTopic ms4StockReconciliationTopic(KafkaTopicProperties properties) {
        return topic(properties.stockReconciliationTopic());
    }

    @Bean
    public NewTopic ms4DeadLetterTopic(KafkaTopicProperties properties) {
        return topic(properties.deadLetterTopic());
    }

    private NewTopic topic(String name) {
        return TopicBuilder
                .name(name)
                .partitions(LOCAL_PARTITIONS)
                .replicas(LOCAL_REPLICAS)
                .build();
    }
}