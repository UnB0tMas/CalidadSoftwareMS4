package com.upsjb.ms4.kafka.consumer;

@FunctionalInterface
public interface KafkaRecordHandler {

    void handle(
            String topic,
            String key,
            Integer partition,
            Long offset,
            String rawJson
    );
}