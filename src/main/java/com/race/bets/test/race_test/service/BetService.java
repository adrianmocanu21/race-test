package com.race.bets.test.race_test.service;


import com.race.bets.test.race_test.dto.PlaceBetRequestDTO;
import com.race.bets.test.race_test.dto.PlaceBetResponseDTO;
import com.race.bets.test.race_test.exception.DriverMismatchException;
import com.race.bets.test.race_test.exception.DuplicateBetException;
import com.race.bets.test.race_test.exception.EventNotFoundException;
import com.race.bets.test.race_test.repository.BetRepository;
import com.race.bets.test.race_test.repository.entity.Bet;
import com.race.bets.test.race_test.repository.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BetService {

    private final BetRepository betRepository;
    private final UserBalanceService userBalanceService;
    private final EventService eventService;
    private final DriverDataService driverDataService;

    @Transactional
    public PlaceBetResponseDTO placeBet(PlaceBetRequestDTO request) {
        validateSingleUserBetPerEvent(request);
        validateEventExists(request);
        validateIsEventDriver(request);

        User user = userBalanceService.withdraw(request.userId(), request.amount());


        Bet bet = Bet.builder()
                .user(user)
                .sessionKey(request.sessionKey())
                .driverNumber(request.driverNumber())
                .amount(request.amount())
                .isWin(null)
                .payout(null)
                .build();

        Bet saved = betRepository.save(bet);

        return new PlaceBetResponseDTO(
                saved.getId(),
                request.userId(),
                saved.getSessionKey(),
                saved.getDriverNumber(),
                saved.getAmount(),
                "PLACED"
        );
    }

    private void validateIsEventDriver(PlaceBetRequestDTO request) {
        if (!driverDataService.isSessionDriver(request.sessionKey(), request.driverNumber())) {
            throw new DriverMismatchException("Driver with number %s not found in session %s".formatted(request.driverNumber(), request.sessionKey()));
        }
    }

    private void validateEventExists(PlaceBetRequestDTO request) {
        if (!eventService.eventExists(request.sessionKey())) {
            throw new EventNotFoundException("Event with id %s not found".formatted(request.sessionKey()));
        }
    }

    private void validateSingleUserBetPerEvent(PlaceBetRequestDTO request) {
        if (betRepository.existsByUserIdAndSessionKey(request.userId(), request.sessionKey())) {
            throw new DuplicateBetException("User already placed a bet %s for session %s".formatted(request.userId(), request.sessionKey()));
        }
    }
}
