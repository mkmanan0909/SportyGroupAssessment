package org.assessment.sporty.service;

import org.assessment.sporty.dto.EventStatus;

import java.util.Set;

public interface EventStateService {

    void update(String eventId, EventStatus status);

    boolean isLive(String eventId);

    Set<String> liveIdsSnapshot();
}
