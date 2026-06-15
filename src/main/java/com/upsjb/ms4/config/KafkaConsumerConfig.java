package com.upsjb.ms4.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.upsjb.ms4.shared.exception.KafkaPublishException;
import com.upsjb.ms4.shared.exception.ValidationException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    private static final String DEFAULT_GROUP_ID =
            "ms4-snapshot-consumer";

    private static final long RETRY_INTERVAL_MILLIS =
            1_000L;

    private static final long RETRY_ATTEMPTS =
            2L;

    private static final int BUSINESS_MAX_POLL_RECORDS =
            25;

    private static final int PROBE_MAX_POLL_RECORDS =
            10;

    private final KafkaProperties kafkaProperties;
    private final KafkaTopicProperties kafkaTopicProperties;

    public KafkaConsumerConfig(
            KafkaProperties kafkaProperties,
            KafkaTopicProperties kafkaTopicProperties
    ) {
        this.kafkaProperties =
                kafkaProperties;

        this.kafkaTopicProperties =
                kafkaTopicProperties;
    }

    @Bean
    public ConsumerFactory<String, String>
    consumerFactory() {
        Map<String, Object> properties =
                buildBaseConsumerProperties();

        properties.putIfAbsent(
                ConsumerConfig.GROUP_ID_CONFIG,
                DEFAULT_GROUP_ID
        );

        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        properties.put(
                ConsumerConfig.MAX_POLL_RECORDS_CONFIG,
                BUSINESS_MAX_POLL_RECORDS
        );

        return new DefaultKafkaConsumerFactory<>(
                properties
        );
    }

    /*
     * Factory funcional para los consumers que reciben
     * Acknowledgment y confirman manualmente el registro.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
    kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            DefaultErrorHandler kafkaErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String>
                factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                consumerFactory
        );

        factory.setCommonErrorHandler(
                kafkaErrorHandler
        );

        factory.setConcurrency(1);

        factory.getContainerProperties()
                .setAckMode(
                        ContainerProperties.AckMode.MANUAL
                );

        return factory;
    }

    @Bean
    public DeadLetterPublishingRecoverer
    deadLetterPublishingRecoverer(
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) ->
                                new TopicPartition(
                                        kafkaTopicProperties
                                                .deadLetterTopic(),
                                        -1
                                )
                );

        recoverer.setFailIfSendResultIsError(
                true
        );

        recoverer.setWaitForSendResultTimeout(
                Duration.ofSeconds(10)
        );

        recoverer.setAppendOriginalHeaders(
                true
        );

        recoverer.setStripPreviousExceptionHeaders(
                true
        );

        recoverer.setLogRecoveryRecord(
                true
        );

        return recoverer;
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            DeadLetterPublishingRecoverer
                    deadLetterPublishingRecoverer
    ) {
        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(
                        deadLetterPublishingRecoverer,
                        new FixedBackOff(
                                RETRY_INTERVAL_MILLIS,
                                RETRY_ATTEMPTS
                        )
                );

        errorHandler.addNotRetryableExceptions(
                ValidationException.class,
                KafkaPublishException.class,
                IllegalArgumentException.class,
                JsonProcessingException.class
        );

        errorHandler.setAckAfterHandle(
                true
        );

        errorHandler.setResetStateOnExceptionChange(
                true
        );

        errorHandler.setResetStateOnRecoveryFailure(
                true
        );

        return errorHandler;
    }

    /*
     * Factory para snapshots MS3 cuyo listener no recibe
     * Acknowledgment.
     */
    @Bean(name = "ms3KafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String>
    ms3KafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            DefaultErrorHandler kafkaErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String>
                factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                consumerFactory
        );

        factory.setCommonErrorHandler(
                kafkaErrorHandler
        );

        factory.setConcurrency(1);

        factory.getContainerProperties()
                .setAckMode(
                        ContainerProperties.AckMode.RECORD
                );

        return factory;
    }

    /*
     * Factory exclusiva para Kafka Probe.
     *
     * Los listeners del probe no declaran Acknowledgment,
     * por lo que usan confirmación por registro.
     */
    @Bean(name = "kafkaProbeListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String>
    kafkaProbeListenerContainerFactory(
            DefaultErrorHandler kafkaErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String>
                factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                buildProbeConsumerFactory()
        );

        factory.setCommonErrorHandler(
                kafkaErrorHandler
        );

        factory.setConcurrency(1);

        factory.getContainerProperties()
                .setAckMode(
                        ContainerProperties.AckMode.RECORD
                );

        return factory;
    }

    private ConsumerFactory<String, String>
    buildProbeConsumerFactory() {
        Map<String, Object> properties =
                buildBaseConsumerProperties();

        /*
         * Cada listener declara su propio groupId y clientIdPrefix.
         */
        properties.remove(
                ConsumerConfig.GROUP_ID_CONFIG
        );

        properties.remove(
                ConsumerConfig.CLIENT_ID_CONFIG
        );

        /*
         * Igual que MS2, se usa earliest para no perder un probe
         * publicado antes de que MS4 haya terminado de iniciar.
         */
        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        properties.put(
                ConsumerConfig.MAX_POLL_RECORDS_CONFIG,
                PROBE_MAX_POLL_RECORDS
        );

        return new DefaultKafkaConsumerFactory<>(
                properties
        );
    }

    private Map<String, Object>
    buildBaseConsumerProperties() {
        Map<String, Object> properties =
                new HashMap<>(
                        kafkaProperties
                                .buildConsumerProperties()
                );

        /*
         * Evita que los siete listeners funcionales de MS4
         * reutilicen ms4-snapshot-consumer-client-0.
         */
        properties.remove(
                ConsumerConfig.CLIENT_ID_CONFIG
        );

        properties.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        properties.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        properties.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false
        );

        properties.put(
                ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG,
                false
        );

        return properties;
    }
}