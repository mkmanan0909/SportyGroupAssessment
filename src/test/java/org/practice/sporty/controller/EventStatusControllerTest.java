package org.practice.sporty.controller;

import org.junit.jupiter.api.Test;
import org.practice.sporty.dto.EventStatus;
import org.practice.sporty.service.EventStateService;
import org.practice.sporty.utility.ApiExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EventStatusController.class)
@Import(ApiExceptionHandler.class)
class EventStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventStateService eventStateService;

    @Test
    void acceptsLiveStatusString() throws Exception {
        mockMvc.perform(post("/events/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventId\":\"42\",\"status\":\"live\"}"))
                .andExpect(status().isAccepted());

        verify(eventStateService).update(eq("42"), eq(EventStatus.LIVE));
    }

    @Test
    void acceptsNumericEventId() throws Exception {
        mockMvc.perform(post("/events/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventId\":99,\"status\":\"not live\"}"))
                .andExpect(status().isAccepted());

        verify(eventStateService).update(eq("99"), eq(EventStatus.NOT_LIVE));
    }

    @Test
    void acceptsBooleanStatus() throws Exception {
        mockMvc.perform(post("/events/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventId\":\"x\",\"status\":true}"))
                .andExpect(status().isAccepted());

        verify(eventStateService).update(eq("x"), eq(EventStatus.LIVE));
    }

}
