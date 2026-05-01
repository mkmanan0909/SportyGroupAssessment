package org.assessment.sporty.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.assessment.sporty.dto.ScoreMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfiguration {

    @Bean
    public ProducerFactory<String, ScoreMessage> scoreMessageProducerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${sporty.kafka.publish.timeout-ms:10000}") int publishTimeoutMs) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, publishTimeoutMs);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, publishTimeoutMs);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, publishTimeoutMs + 2000);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JsonSerializer<ScoreMessage> valueSerializer = new JsonSerializer<>(mapper);
        /*
         Do not put ADD_TYPE_INFO_HEADERS into props: ProducerConfig merges the map into the native
         kafka-clients Producer and logs WARN for unrecognized keys like spring.json.add.type.headers.
         */
        valueSerializer.configure(Collections.singletonMap(JsonSerializer.ADD_TYPE_INFO_HEADERS, false), false);
        return new DefaultKafkaProducerFactory<>(
                props,
                new StringSerializer(),
                valueSerializer);
    }

    @Bean
    public KafkaTemplate<String, ScoreMessage> scoreKafkaTemplate(
            ProducerFactory<String, ScoreMessage> scoreMessageProducerFactory) {
        return new KafkaTemplate<>(scoreMessageProducerFactory);
    }
}
