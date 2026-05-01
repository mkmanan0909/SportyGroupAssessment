package org.assessment.sporty.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.assessment.sporty.dto.ScoreMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.time.Instant;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {ScoreMessagePublisher.class, ScoreMessagePublisherTest.RetryConfig.class})
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "sporty.kafka.topic=test-scores-topic",
        "sporty.kafka.publish.max-attempts=3",
        "sporty.kafka.publish.backoff-ms=50"
})
class ScoreMessagePublisherTest {

    @Configuration
    @EnableRetry(proxyTargetClass = true)
    static class RetryConfig {
    }

    @MockBean
    private KafkaTemplate<String, ScoreMessage> kafkaTemplate;

    @Autowired
    private ScoreMessagePublisher publisher;

    @Test
    void publishSendsToConfiguredTopicWithEventIdAsKey() throws Exception {
        SettableListenableFuture<SendResult<String, ScoreMessage>> future = new SettableListenableFuture<>();
        future.set(mock(SendResult.class));
        when(kafkaTemplate.send(eq("test-scores-topic"), eq("e99"), any(ScoreMessage.class))).thenReturn(future);

        Instant polledAt = Instant.parse("2026-04-26T12:00:00Z");
        publisher.publish(new ScoreMessage("e99", "1:0", polledAt));

        ArgumentCaptor<ScoreMessage> captor = ArgumentCaptor.forClass(ScoreMessage.class);
        verify(kafkaTemplate).send(eq("test-scores-topic"), eq("e99"), captor.capture());
        assertThat(captor.getValue().getEventId()).isEqualTo("e99");
        assertThat(captor.getValue().getCurrentScore()).isEqualTo("1:0");
        assertThat(captor.getValue().getPolledAt()).isEqualTo(polledAt);
    }

    @Test
    void publishRetriesThenSucceedsWhenSendFutureFailsOnce() throws Exception {
        SettableListenableFuture<SendResult<String, ScoreMessage>> failing = new SettableListenableFuture<>();
        failing.setException(new ExecutionException("wrapped", new RuntimeException("transient")));

        SettableListenableFuture<SendResult<String, ScoreMessage>> ok = new SettableListenableFuture<>();
        ok.set(mock(SendResult.class));

        when(kafkaTemplate.send(eq("test-scores-topic"), eq("r1"), any(ScoreMessage.class)))
                .thenReturn(failing, ok);

        publisher.publish(new ScoreMessage("r1", "0:0", Instant.now()));

        verify(kafkaTemplate, times(2)).send(eq("test-scores-topic"), eq("r1"), any(ScoreMessage.class));
    }
}
