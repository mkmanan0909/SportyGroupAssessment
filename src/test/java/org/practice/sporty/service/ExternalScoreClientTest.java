package org.practice.sporty.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.practice.sporty.dto.ExternalScoreResponse;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestClientException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExternalScoreClientTest {

    private WireMockServer wireMock;
    private ExternalScoreClient client;

    @BeforeEach
    void startServer() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();
        WireMock.configureFor(wireMock.port());
        String template = "http://localhost:" + wireMock.port() + "/scores/{eventId}";
        client = new ExternalScoreClient(new RestTemplateBuilder(), template);
    }

    @AfterEach
    void stopServer() {
        if (wireMock != null) {
            wireMock.stop();
        }
    }

    @Test
    void parsesJsonResponse() {
        wireMock.stubFor(get(urlEqualTo("/scores/e1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"eventId\":\"e1\",\"currentScore\":\"3:3\"}")));

        ExternalScoreResponse r = client.fetchScore("e1");
        assertThat(r.getEventId()).isEqualTo("e1");
        assertThat(r.getCurrentScore()).isEqualTo("3:3");
    }

    @Test
    void propagatesHttpErrors() {
        wireMock.stubFor(get(urlEqualTo("/scores/missing"))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> client.fetchScore("missing"))
                .isInstanceOf(RestClientException.class);
    }
}
