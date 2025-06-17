package com.race.bets.test.race_test.client.openf1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenF1DriverDTO(
        @JsonProperty("driver_number") int driverNumber,
        @JsonProperty("full_name") String fullName,
        @JsonProperty("session_key") int sessionKey
) {}
