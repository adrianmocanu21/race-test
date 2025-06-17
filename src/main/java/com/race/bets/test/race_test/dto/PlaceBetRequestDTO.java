package com.race.bets.test.race_test.dto;

public record PlaceBetRequestDTO(
        String userId,
        Integer sessionKey,
        Integer driverNumber,
        Integer amount
) {}