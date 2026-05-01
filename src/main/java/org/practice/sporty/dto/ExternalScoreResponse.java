package org.practice.sporty.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalScoreResponse {

    private String eventId;
    private String currentScore;

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
}
