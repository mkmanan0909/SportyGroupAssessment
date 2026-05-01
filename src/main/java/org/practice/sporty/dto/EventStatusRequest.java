package org.practice.sporty.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EventStatusRequest {

    @JsonProperty("eventId")
    private Object eventId;

    private EventStatus status;

    public String getEventId() {
        if (eventId == null) {
            return null;
        }
        return String.valueOf(eventId).trim();
    }

    public void setEventId(Object eventId) {
        this.eventId = eventId;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }
}
