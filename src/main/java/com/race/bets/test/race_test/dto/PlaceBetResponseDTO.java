package com.race.bets.test.race_test.dto;

public record PlaceBetResponseDTO(
        Long betId,
        String userId,
        Integer sessionKey,
        Integer driverNumber,
        Integer amount,
        String status
) {}