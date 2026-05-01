package org.practice.sporty.service.impl;

import org.practice.sporty.dto.EventStatus;
import org.practice.sporty.service.EventStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EventStateServiceImpl implements EventStateService {

    private static final Logger log = LoggerFactory.getLogger(EventStateServiceImpl.class);

    private final Set<String> liveIds = ConcurrentHashMap.newKeySet();

    @Override
    public void update(String eventId, EventStatus status) {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("eventId must not be blank");
        }
        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }
        if (status == EventStatus.LIVE) {
            if (liveIds.add(eventId)) {
                log.info("live: {}", eventId);
            }
        } else {
            if (liveIds.remove(eventId)) {
                log.info("off air: {}", eventId);
            }
        }
    }

    @Override
    public boolean isLive(String eventId) {
        return liveIds.contains(eventId);
    }

    /** Copy so the scheduler doesn't iterate a structurally mutating set. */
    @Override
    public Set<String> liveIdsSnapshot() {
        return Set.copyOf(liveIds);
    }
}
