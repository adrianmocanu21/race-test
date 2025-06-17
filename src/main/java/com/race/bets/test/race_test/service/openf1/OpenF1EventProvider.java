package com.race.bets.test.race_test.service.openf1;

import com.race.bets.test.race_test.client.openf1.OpenF1Client;
import com.race.bets.test.race_test.client.openf1.dto.OpenF1DriverDTO;
import com.race.bets.test.race_test.client.openf1.dto.OpenF1SessionDTO;
import com.race.bets.test.race_test.dto.DriverMarketDTO;
import com.race.bets.test.race_test.dto.EventResponseDTO;
import com.race.bets.test.race_test.exception.EventNotFoundException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import com.race.bets.test.race_test.service.DriverDataService;
import com.race.bets.test.race_test.service.EventProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpenF1EventProvider implements EventProvider {

    private final OpenF1Client openF1Client;
    private final DriverDataService driverDataService;

    @Override
    public List<EventResponseDTO> getEvents(Integer year, String country, String sessionType) {
        List<OpenF1SessionDTO> sessions = openF1Client.getSessions(year, country, sessionType);
        return sessions.stream()
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EventResponseDTO getEventBySessionKey(Integer sessionKey) {
        OpenF1SessionDTO session = openF1Client.getSessionsBySessionKey(sessionKey).stream().findAny().orElseThrow(() -> new EventNotFoundException("Event with id %s not found".formatted(sessionKey)));
        return mapToEventResponse(session);
    }

    private EventResponseDTO mapToEventResponse(OpenF1SessionDTO session) {
    List<OpenF1DriverDTO> drivers = driverDataService.getDriversBySession(session.sessionKey());

    List<DriverMarketDTO> driverMarkets =
        drivers.stream()
            .map(
                driver ->
                    new DriverMarketDTO(driver.fullName(), driver.driverNumber(), getRandomOdds()))
            .collect(Collectors.toList());

    return new EventResponseDTO(
        session.sessionKey(),
        session.country(),
        session.year(),
        session.sessionType(),
        driverMarkets);
  }

    public static int getRandomOdds() {
        return new int[]{2, 3, 4} [ThreadLocalRandom.current().nextInt(3)];
    }


}
