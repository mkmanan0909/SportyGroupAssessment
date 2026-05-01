package org.assessment.sporty.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum EventStatus {
    LIVE,
    NOT_LIVE;

    @JsonCreator
    public static EventStatus fromJson(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("status is required");
        }
        if (value instanceof Boolean) {
            return (Boolean) value ? LIVE : NOT_LIVE;
        }
        String s = String.valueOf(value).trim().toLowerCase(Locale.ROOT);
        if ("live".equals(s)) {
            return LIVE;
        }
        if ("not live".equals(s) || "not_live".equals(s)) {
            return NOT_LIVE;
        }
        throw new IllegalArgumentException("status: use \"live\", \"not live\", or a boolean");
    }

    @JsonValue
    public String toJson() {
        return this == LIVE ? "live" : "not live";
    }
}
