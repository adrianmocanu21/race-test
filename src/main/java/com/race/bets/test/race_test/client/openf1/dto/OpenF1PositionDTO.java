package com.race.bets.test.race_test.client.openf1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record OpenF1PositionDTO(
        @JsonProperty("session_key") int sessionKey,
        @JsonProperty("driver_number") int driverNumber,
        @JsonProperty("position") int position,
        @JsonProperty("date") Instant date
) {}