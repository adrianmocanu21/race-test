package com.race.bets.test.race_test.dto;

import java.util.List;

public record EventResponseDTO(int sessionKey,
                               String country,
                               int year,
                               String sessionType,
                               List<DriverMarketDTO> drivers) {}
