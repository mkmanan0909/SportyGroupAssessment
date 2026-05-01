package org.assessment.sporty.service;

import org.junit.jupiter.api.Test;
import org.assessment.sporty.dto.EventStatus;
import org.assessment.sporty.service.impl.EventStateServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventStateServiceTest {

    private final EventStateService service = new EventStateServiceImpl();

    @Test
    void marksEventLiveAndNotLive() {
        service.update("e1", EventStatus.LIVE);
        assertThat(service.isLive("e1")).isTrue();
        assertThat(service.liveIdsSnapshot()).containsExactly("e1");

        service.update("e1", EventStatus.NOT_LIVE);
        assertThat(service.isLive("e1")).isFalse();
        assertThat(service.liveIdsSnapshot()).isEmpty();
    }

    @Test
    void supportsMultipleLiveEvents() {
        service.update("a", EventStatus.LIVE);
        service.update("b", EventStatus.LIVE);
        assertThat(service.liveIdsSnapshot()).containsExactlyInAnyOrder("a", "b");
    }

    @Test
    void rejectsBlankEventId() {
        assertThatThrownBy(() -> service.update("  ", EventStatus.LIVE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }
}
