package com.upsjb.ms4.kafka.probe;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.kafka.probe")
public class KafkaProbeProperties {

    private boolean enabled = true;
    private boolean runOnStartup = true;

    @Min(0)
    private long initialDelayMs = 12_000L;

    @Min(1_000)
    private long retryDelayMs = 10_000L;

    @Min(1)
    private int maxAttempts = 6;

    private boolean failOnTimeout = false;

    @Min(1_000)
    private long sendTimeoutMs = 10_000L;

    @NotBlank
    private String consumerGroup = "ms4-probe-consumer";

    @NotBlank
    private String serviceName = "ms-ventas-facturacion";

    @NotBlank
    private String targetMs2 = "ms-personas-clientes-empleados";

    @NotBlank
    private String targetMs3 = "ms-catalogo-inventario";

    @Valid
    private Topics topics = new Topics();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRunOnStartup() {
        return runOnStartup;
    }

    public void setRunOnStartup(boolean runOnStartup) {
        this.runOnStartup = runOnStartup;
    }

    public long getInitialDelayMs() {
        return initialDelayMs;
    }

    public void setInitialDelayMs(long initialDelayMs) {
        this.initialDelayMs = initialDelayMs;
    }

    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    public void setRetryDelayMs(long retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public boolean isFailOnTimeout() {
        return failOnTimeout;
    }

    public void setFailOnTimeout(boolean failOnTimeout) {
        this.failOnTimeout = failOnTimeout;
    }

    public long getSendTimeoutMs() {
        return sendTimeoutMs;
    }

    public void setSendTimeoutMs(long sendTimeoutMs) {
        this.sendTimeoutMs = sendTimeoutMs;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getTargetMs2() {
        return targetMs2;
    }

    public void setTargetMs2(String targetMs2) {
        this.targetMs2 = targetMs2;
    }

    public String getTargetMs3() {
        return targetMs3;
    }

    public void setTargetMs3(String targetMs3) {
        this.targetMs3 = targetMs3;
    }

    public Topics getTopics() {
        return topics;
    }

    public void setTopics(Topics topics) {
        this.topics = topics == null ? new Topics() : topics;
    }

    public long safeInitialDelayMs() {
        return Math.max(initialDelayMs, 0L);
    }

    public long safeRetryDelayMs() {
        return Math.max(retryDelayMs, 1_000L);
    }

    public int safeMaxAttempts() {
        return Math.max(maxAttempts, 1);
    }

    public long safeSendTimeoutMs() {
        return Math.max(sendTimeoutMs, 1_000L);
    }

    public String ms2ToMs4Topic() {
        return topics.normalize(topics.ms2ToMs4, "dev.ms2.ms4.probe.v1");
    }

    public String ms4ToMs2AckTopic() {
        return topics.normalize(topics.ms4ToMs2Ack, "dev.ms4.ms2.probe-ack.v1");
    }

    public String ms3ToMs4Topic() {
        return topics.normalize(topics.ms3ToMs4, "dev.ms3.ms4.probe.v1");
    }

    public String ms4ToMs3AckTopic() {
        return topics.normalize(topics.ms4ToMs3Ack, "dev.ms4.ms3.probe-ack.v1");
    }

    public String ms4ToMs3Topic() {
        return topics.normalize(topics.ms4ToMs3, "dev.ms4.ms3.probe.v1");
    }

    public String ms3ToMs4AckTopic() {
        return topics.normalize(topics.ms3ToMs4Ack, "dev.ms3.ms4.probe-ack.v1");
    }

    public static class Topics {

        @NotBlank
        private String ms2ToMs4 = "dev.ms2.ms4.probe.v1";

        @NotBlank
        private String ms4ToMs2Ack = "dev.ms4.ms2.probe-ack.v1";

        @NotBlank
        private String ms3ToMs4 = "dev.ms3.ms4.probe.v1";

        @NotBlank
        private String ms4ToMs3Ack = "dev.ms4.ms3.probe-ack.v1";

        @NotBlank
        private String ms4ToMs3 = "dev.ms4.ms3.probe.v1";

        @NotBlank
        private String ms3ToMs4Ack = "dev.ms3.ms4.probe-ack.v1";

        public String getMs2ToMs4() {
            return ms2ToMs4;
        }

        public void setMs2ToMs4(String ms2ToMs4) {
            this.ms2ToMs4 = ms2ToMs4;
        }

        public String getMs4ToMs2Ack() {
            return ms4ToMs2Ack;
        }

        public void setMs4ToMs2Ack(String ms4ToMs2Ack) {
            this.ms4ToMs2Ack = ms4ToMs2Ack;
        }

        public String getMs3ToMs4() {
            return ms3ToMs4;
        }

        public void setMs3ToMs4(String ms3ToMs4) {
            this.ms3ToMs4 = ms3ToMs4;
        }

        public String getMs4ToMs3Ack() {
            return ms4ToMs3Ack;
        }

        public void setMs4ToMs3Ack(String ms4ToMs3Ack) {
            this.ms4ToMs3Ack = ms4ToMs3Ack;
        }

        public String getMs4ToMs3() {
            return ms4ToMs3;
        }

        public void setMs4ToMs3(String ms4ToMs3) {
            this.ms4ToMs3 = ms4ToMs3;
        }

        public String getMs3ToMs4Ack() {
            return ms3ToMs4Ack;
        }

        public void setMs3ToMs4Ack(String ms3ToMs4Ack) {
            this.ms3ToMs4Ack = ms3ToMs4Ack;
        }

        private String normalize(String value, String fallback) {
            if (value == null || value.isBlank()) {
                return fallback;
            }

            return value.trim();
        }
    }
}