package org.assessment.sporty.service;

import org.assessment.sporty.dto.ExternalScoreResponse;
import org.assessment.sporty.dto.ScoreMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class LiveEventPoller {

    private static final Logger log = LoggerFactory.getLogger(LiveEventPoller.class);

    private final EventStateService events;
    private final ExternalScoreClient scoreClient;
    private final ScoreMessagePublisher publisher;

    public LiveEventPoller(
            EventStateService events,
            ExternalScoreClient scoreClient,
            ScoreMessagePublisher publisher) {
        this.events = events;
        this.scoreClient = scoreClient;
        this.publisher = publisher;
    }

    @Scheduled(fixedRateString = "${sporty.poll-interval-ms:10000}")
    public void pollLiveEvents() {
        var ids = events.liveIdsSnapshot();
        if (ids.isEmpty()) {
            return;
        }
        for (String id : ids) {
            processOne(id);
        }
    }

    void processOne(String eventId) {
        if (!events.isLive(eventId)) {
            return;
        }
        try {
            ExternalScoreResponse score = scoreClient.fetchScore(eventId);
            if (!events.isLive(eventId)) {
                return;
            }
            String id = score.getEventId() != null ? score.getEventId() : eventId;
            publisher.publish(new ScoreMessage(id, score.getCurrentScore(), Instant.now()));
        } catch (Exception e) {
            log.error("poll failed for {}", eventId, e);
        }
    }
}
