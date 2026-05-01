package org.assessment.sporty.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.assessment.sporty.dto.EventStatus;
import org.assessment.sporty.dto.ScoreMessage;
import org.assessment.sporty.service.impl.EventStateServiceImpl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiveEventPollerFailureTest {

    @Mock
    private ExternalScoreClient externalScoreClient;

    @Mock
    private ScoreMessagePublisher scoreMessagePublisher;

    private EventStateService eventStateService;
    private LiveEventPoller poller;

    @BeforeEach
    void setUp() {
        eventStateService = new EventStateServiceImpl();
        poller = new LiveEventPoller(eventStateService, externalScoreClient, scoreMessagePublisher);
    }

    @Test
    void doesNotPublishWhenExternalApiFails() throws Exception {
        eventStateService.update("e1", EventStatus.LIVE);
        when(externalScoreClient.fetchScore("e1")).thenThrow(new org.springframework.web.client.RestClientException("down"));

        poller.processOne("e1");

        verify(scoreMessagePublisher, never()).publish(any(ScoreMessage.class));
    }
}
