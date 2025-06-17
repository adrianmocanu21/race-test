package com.race.bets.test.race_test.client.openf1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenF1SessionDTO(
        @JsonProperty("session_key") int sessionKey,
        @JsonProperty("meeting_key") int meetingKey,
        @JsonProperty("country_name") String country,
        @JsonProperty("year") int year,
        @JsonProperty("session_type") String sessionType,
        @JsonProperty("location") String location
) {}