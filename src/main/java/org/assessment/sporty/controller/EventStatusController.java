package org.assessment.sporty.controller;

import org.assessment.sporty.dto.EventStatusRequest;
import org.assessment.sporty.service.EventStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventStatusController {

    private final EventStateService eventStateService;

    private static final Logger log = LoggerFactory.getLogger(EventStatusController.class);

    public EventStatusController(EventStateService eventStateService) {
        this.eventStateService = eventStateService;
    }

    @PostMapping("/events/status")
    public ResponseEntity<Void> updateStatus(@RequestBody EventStatusRequest request) {
        eventStateService.update(request.getEventId(), request.getStatus());
        log.info("POST /events/status eventId={} status={}", request.getEventId(), request.getStatus());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
