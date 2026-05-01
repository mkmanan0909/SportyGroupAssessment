package org.assessment.sporty.dto;

import java.time.Instant;

public class ScoreMessage {

    private String eventId;
    private String currentScore;
    private Instant polledAt;

    public ScoreMessage() {
    }

    public ScoreMessage(String eventId, String currentScore, Instant polledAt) {
        this.eventId = eventId;
        this.currentScore = currentScore;
        this.polledAt = polledAt;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(String currentScore) {
        this.currentScore = currentScore;
    }

    public Instant getPolledAt() {
        return polledAt;
    }

    public void setPolledAt(Instant polledAt) {
        this.polledAt = polledAt;
    }
}
