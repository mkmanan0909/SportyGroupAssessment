package org.practice.sporty.service;

import org.practice.sporty.dto.EventStatus;

import java.util.Set;

public interface EventStateService {

    void update(String eventId, EventStatus status);

    boolean isLive(String eventId);

    Set<String> liveIdsSnapshot();
}
