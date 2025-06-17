package com.race.bets.test.race_test.service;

import com.race.bets.test.race_test.dto.EventResponseDTO;

import java.util.List;

public interface EventProvider {

    List<EventResponseDTO> getEvents(Integer year, String country, String sessionType);

    EventResponseDTO getEventBySessionKey(Integer sessionKey);

}
