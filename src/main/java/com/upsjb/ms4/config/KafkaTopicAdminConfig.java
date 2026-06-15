package com.upsjb.ms4.config;

import java.time.Duration;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicAdminConfig {

    private static final int LOCAL_PARTITIONS =
            3;

    private static final int LOCAL_REPLICAS =
            1;

    private static final String FOURTEEN_DAYS_MS =
            String.valueOf(
                    Duration.ofDays(14)
                            .toMillis()
            );

    private static final String THIRTY_DAYS_MS =
            String.valueOf(
                    Duration.ofDays(30)
                            .toMillis()
            );

    /*
     * MS4 crea únicamente los topics de los que es productor.
     *
     * Los topics ms2.* y ms3.* deben ser creados y configurados
     * por sus respectivos microservicios propietarios.
     */

    @Bean
    public NewTopic ms4StockCommandTopic(
            KafkaTopicProperties properties
    ) {
        return retainedTopic(
                properties.stockCommandTopic(),
                FOURTEEN_DAYS_MS
        );
    }

    @Bean
    public NewTopic ms4StockReconciliationTopic(
            KafkaTopicProperties properties
    ) {
        return retainedTopic(
                properties.stockReconciliationTopic(),
                THIRTY_DAYS_MS
        );
    }

    @Bean
    public NewTopic ms4DeadLetterTopic(
            KafkaTopicProperties properties
    ) {
        return retainedTopic(
                properties.deadLetterTopic(),
                THIRTY_DAYS_MS
        );
    }

    private NewTopic retainedTopic(
            String name,
            String retentionMs
    ) {
        return TopicBuilder
                .name(name)
                .partitions(
                        LOCAL_PARTITIONS
                )
                .replicas(
                        LOCAL_REPLICAS
                )
                .config(
                        TopicConfig.CLEANUP_POLICY_CONFIG,
                        TopicConfig.CLEANUP_POLICY_DELETE
                )
                .config(
                        TopicConfig.RETENTION_MS_CONFIG,
                        retentionMs
                )
                .build();
    }
}