package dev.jcri.mdde.registry.configuration.benchmark;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonPropertyOrder({
        StatsConfig.KAFKA_BROKER_HOST
})
public class StatsConfig {
    public static final String KAFKA_BROKER_HOST = "kafka_broker";

    private String kafkaBroker;

    @JsonGetter(KAFKA_BROKER_HOST)
    public String getKafkaBroker() {
        return kafkaBroker;
    }
    @JsonSetter(KAFKA_BROKER_HOST)
    public void setKafkaBroker(String kafkaBroker) {
        this.kafkaBroker = kafkaBroker;
    }
}
