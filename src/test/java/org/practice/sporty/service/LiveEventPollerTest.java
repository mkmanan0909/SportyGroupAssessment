package org.practice.sporty.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.practice.sporty.dto.EventStatus;
import org.practice.sporty.dto.ExternalScoreResponse;
import org.practice.sporty.dto.ScoreMessage;
import org.practice.sporty.service.impl.EventStateServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiveEventPollerTest {

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
    void doesNothingWhenNoLiveEvents() throws Exception {
        poller.pollLiveEvents();
        verify(externalScoreClient, never()).fetchScore(any());
        verify(scoreMessagePublisher, never()).publish(any());
    }

    @Test
    void fetchesScoreAndPublishesForLiveEvent() throws Exception {
        eventStateService.update("e1", EventStatus.LIVE);
        ExternalScoreResponse api = new ExternalScoreResponse();
        api.setEventId("e1");
        api.setCurrentScore("2:1");
        when(externalScoreClient.fetchScore("e1")).thenReturn(api);

        poller.processOne("e1");

        ArgumentCaptor<ScoreMessage> captor = ArgumentCaptor.forClass(ScoreMessage.class);
        verify(scoreMessagePublisher).publish(captor.capture());
        ScoreMessage sent = captor.getValue();
        assertThat(sent.getEventId()).isEqualTo("e1");
        assertThat(sent.getCurrentScore()).isEqualTo("2:1");
        assertThat(sent.getPolledAt()).isNotNull();
    }

    @Test
    void skipsPublishWhenEventTurnedOffBeforePublish() throws Exception {
        eventStateService.update("e1", EventStatus.LIVE);
        ExternalScoreResponse api = new ExternalScoreResponse();
        api.setEventId("e1");
        api.setCurrentScore("0:0");
        when(externalScoreClient.fetchScore("e1")).thenAnswer(inv -> {
            eventStateService.update("e1", EventStatus.NOT_LIVE);
            return api;
        });

        poller.processOne("e1");

        verify(scoreMessagePublisher, never()).publish(any());
    }

    @Test
    void usesRequestEventIdWhenApiOmitsIt() throws Exception {
        eventStateService.update("e1", EventStatus.LIVE);
        ExternalScoreResponse api = new ExternalScoreResponse();
        api.setCurrentScore("1:0");
        when(externalScoreClient.fetchScore("e1")).thenReturn(api);

        poller.processOne("e1");

        ArgumentCaptor<ScoreMessage> captor = ArgumentCaptor.forClass(ScoreMessage.class);
        verify(scoreMessagePublisher).publish(captor.capture());
        assertThat(captor.getValue().getEventId()).isEqualTo("e1");
    }
}
