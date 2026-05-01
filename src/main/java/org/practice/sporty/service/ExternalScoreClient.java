package org.practice.sporty.service;

import org.practice.sporty.dto.ExternalScoreResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class ExternalScoreClient {

    private static final Logger log = LoggerFactory.getLogger(ExternalScoreClient.class);

    private final RestTemplate http;
    private final String urlPattern;

    public ExternalScoreClient(RestTemplateBuilder builder, @Value("${sporty.external-score.url-template}") String urlPattern) {
        this.http = builder.build();
        this.urlPattern = urlPattern;
    }

    public ExternalScoreResponse fetchScore(String eventId) {
        String url = UriComponentsBuilder.fromUriString(urlPattern).buildAndExpand(eventId).toUriString();
        try {
            ExternalScoreResponse body = http.getForObject(url, ExternalScoreResponse.class);
            if (body == null) {
                throw new RestClientException("empty body for " + eventId);
            }
            return body;
        } catch (RestClientException e) {
            log.warn("GET {} -> {}", url, e.toString());
            throw e;
        }
    }
}
