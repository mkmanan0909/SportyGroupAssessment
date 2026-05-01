package org.assessment.sporty.service;

import org.assessment.sporty.dto.ScoreMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.concurrent.ExecutionException;

@Service
public class ScoreMessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(ScoreMessagePublisher.class);

    private final KafkaTemplate<String, ScoreMessage> kafka;
    private final String topic;

    public ScoreMessagePublisher(KafkaTemplate<String, ScoreMessage> kafka, @Value("${sporty.kafka.topic}") String topic) {
        this.kafka = kafka;
        this.topic = topic;
    }

    /** Mostly for send().get() flaking; not trying to retry programmer errors. */
    @Retryable(
            value = ExecutionException.class,
            maxAttemptsExpression = "${sporty.kafka.publish.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${sporty.kafka.publish.backoff-ms:400}"))
    public void publish(ScoreMessage message) throws Exception {
        ListenableFuture<SendResult<String, ScoreMessage>> future = kafka.send(topic, message.getEventId(), message);
        SendResult<String, ScoreMessage> result = future.get();
        RecordMetadata md = result.getRecordMetadata();
        if (md != null) {
            log.info(
                    "published score eventId={} topic={} partition={} offset={}",
                    message.getEventId(),
                    md.topic(),
                    md.partition(),
                    md.offset());
        } else {
            log.info("published score eventId={} topic={}", message.getEventId(), topic);
        }
    }

    @Recover
    void gaveUp(ExecutionException ex, ScoreMessage message) {
        log.error("kafka gave up on {}: {}", message.getEventId(), ex.getMessage());
    }
}
