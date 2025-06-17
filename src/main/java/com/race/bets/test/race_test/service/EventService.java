package com.race.bets.test.race_test.service;

import com.race.bets.test.race_test.client.openf1.dto.OpenF1DriverDTO;
import com.race.bets.test.race_test.dto.EventResponseDTO;
import com.race.bets.test.race_test.dto.EventResultDTO;
import com.race.bets.test.race_test.exception.BetNotFoundException;
import com.race.bets.test.race_test.exception.DriverNotFoundException;
import com.race.bets.test.race_test.repository.BetRepository;
import com.race.bets.test.race_test.repository.UserRepository;
import com.race.bets.test.race_test.repository.entity.Bet;
import com.race.bets.test.race_test.repository.entity.User;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventProvider eventProvider;

    private final BetRepository betRepository;

    private final DriverDataService driverDataService;

    private final UserRepository userRepository;

    public List<EventResponseDTO> getFilteredEvents(Integer year, String country, String sessionType) {
        return eventProvider.getEvents(year, country, sessionType);
    }

    public boolean eventExists(Integer sessionKey) {
        return eventProvider.getEventBySessionKey(sessionKey) != null;
    }

    @Transactional
    public EventResultDTO settleEventOutcome(int sessionKey) {
        List<Bet> bets = betRepository.findAllBySessionKey(sessionKey);
        if (bets.isEmpty()) {
            throw new BetNotFoundException("No bets found for sessionKey " + sessionKey);
        }

        List<OpenF1DriverDTO> drivers = driverDataService.getDriversBySession(sessionKey);
        if (drivers.isEmpty()) {
            throw new DriverNotFoundException("No drivers found for sessionKey " + sessionKey);
        }

        OpenF1DriverDTO winner = drivers.get(ThreadLocalRandom.current().nextInt(drivers.size()));
        int winningDriver = winner.driverNumber();

        for (Bet bet : bets) {
            if (isSettledEvent(bet)) {
                continue;
            }

            boolean isWin = bet.getDriverNumber() == winningDriver;
            double payout = isWin ? bet.getAmount() * getOdds(bet.getDriverNumber(), drivers) : 0.0;

            bet.setIsWin(isWin);
            bet.setPayout(payout);

            User user = bet.getUser();
            if (isWin) {
                user.setBalance(user.getBalance() + (int) payout);
            }
        }

        betRepository.saveAll(bets);
        userRepository.saveAll(bets.stream().map(Bet::getUser).distinct().toList());

        return new EventResultDTO(sessionKey, winningDriver);
    }

    private static boolean isSettledEvent(Bet bet) {
        return bet.getIsWin() != null;
    }

    private int getOdds(int driverNumber, List<OpenF1DriverDTO> drivers) {
        return List.of(2, 3, 4).get(ThreadLocalRandom.current().nextInt(3));
    }
}
