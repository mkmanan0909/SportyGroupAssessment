package org.practice.sporty.mock;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;


@RestController
public class MockScoreFeedController {

    @GetMapping("/scores/{eventId}")
    public Map<String, String> score(@PathVariable String eventId) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("eventId", eventId);
        body.put("currentScore", "0:0");
        return body;
    }
}
