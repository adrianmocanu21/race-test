package com.race.bets.test.race_test.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.race.bets.test.race_test.client.openf1.dto.OpenF1DriverDTO;
import com.race.bets.test.race_test.dto.EventResponseDTO;
import com.race.bets.test.race_test.dto.EventResultDTO;
import com.race.bets.test.race_test.exception.BetNotFoundException;
import com.race.bets.test.race_test.exception.DriverNotFoundException;
import com.race.bets.test.race_test.repository.BetRepository;
import com.race.bets.test.race_test.repository.UserRepository;
import com.race.bets.test.race_test.repository.entity.Bet;
import com.race.bets.test.race_test.repository.entity.User;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventProvider eventProvider;

    @Mock
    private BetRepository betRepository;

    @Mock
    private DriverDataService driverDataService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EventService eventService;

    private static final int SESSION_KEY = 123;
    private static final int WINNING_DRIVER = 44;
    private static final String USER_ID = "user1";
    private static final int BET_AMOUNT = 100;
    private static final int INITIAL_BALANCE = 1000;

    private User testUser;
    private Bet testBet;
    private List<OpenF1DriverDTO> testDrivers;

    @BeforeEach
    void setUp() {
        testUser = new User(USER_ID, INITIAL_BALANCE);
        testBet = Bet.builder()
                .id(1L)
                .user(testUser)
                .sessionKey(SESSION_KEY)
                .driverNumber(WINNING_DRIVER)
                .amount(BET_AMOUNT)
                .isWin(null)
                .payout(null)
                .build();

        testDrivers = List.of(
                new OpenF1DriverDTO(WINNING_DRIVER, "Lewis Hamilton", SESSION_KEY),
                new OpenF1DriverDTO(33, "Max Verstappen" , SESSION_KEY)
        );
    }

    @Test
    void getFilteredEvents_ShouldReturnEventsFromProvider() {
        // Arrange
        Integer year = 2023;
        String country = "Monaco";
        String sessionType = "Race";
        List<EventResponseDTO> expectedEvents = List.of(
                new EventResponseDTO(SESSION_KEY, "Monaco", 2023, "Race", Collections.emptyList())
        );
        when(eventProvider.getEvents(year, country, sessionType)).thenReturn(expectedEvents);

        // Act
        List<EventResponseDTO> result = eventService.getFilteredEvents(year, country, sessionType);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(SESSION_KEY, result.get(0).sessionKey());
        verify(eventProvider).getEvents(year, country, sessionType);
    }

    @Test
    void eventExists_WhenEventFound_ShouldReturnTrue() {
        // Arrange
        when(eventProvider.getEventBySessionKey(SESSION_KEY)).thenReturn(
                new EventResponseDTO(SESSION_KEY, "Monaco", 2023, "Race", Collections.emptyList())
        );

        // Act
        boolean exists = eventService.eventExists(SESSION_KEY);


        // Assert
        assertTrue(exists);
        verify(eventProvider).getEventBySessionKey(SESSION_KEY);
    }

    @Test
    void eventExists_WhenEventNotFound_ShouldReturnFalse() {
        // Arrange
        when(eventProvider.getEventBySessionKey(SESSION_KEY)).thenReturn(null);

        // Act
        boolean exists = eventService.eventExists(SESSION_KEY);

        // Assert
        assertFalse(exists);
        verify(eventProvider).getEventBySessionKey(SESSION_KEY);
    }

    @Test
    void settleEventOutcome_WithNoBets_ShouldThrowException() {
        // Arrange
        when(betRepository.findAllBySessionKey(SESSION_KEY)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(BetNotFoundException.class, () -> eventService.settleEventOutcome(SESSION_KEY));
        verify(betRepository).findAllBySessionKey(SESSION_KEY);
        verifyNoInteractions(driverDataService, userRepository);
    }

    @Test
    void settleEventOutcome_WithNoDrivers_ShouldThrowException() {
        // Arrange
        when(betRepository.findAllBySessionKey(SESSION_KEY)).thenReturn(List.of(testBet));
        when(driverDataService.getDriversBySession(SESSION_KEY)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(DriverNotFoundException.class, () -> eventService.settleEventOutcome(SESSION_KEY));
        verify(betRepository).findAllBySessionKey(SESSION_KEY);
        verify(driverDataService).getDriversBySession(SESSION_KEY);
        verifyNoInteractions(userRepository);
    }

    @Test
    void settleEventOutcome_WithWinningBet_ShouldUpdateBetAndUserBalance() {
        // Arrange
        List<Bet> bets = List.of(testBet);
        when(betRepository.findAllBySessionKey(SESSION_KEY)).thenReturn(bets);
        when(driverDataService.getDriversBySession(SESSION_KEY)).thenReturn(testDrivers);

        // Act
        EventResultDTO result = eventService.settleEventOutcome(SESSION_KEY);

        // Assert
        assertNotNull(result);
        assertEquals(SESSION_KEY, result.sessionKey());
        assertTrue(testDrivers.stream().anyMatch(d -> d.driverNumber() == result.winningDriverNumber()));
        
        // Get the actual bet that was saved
        Bet savedBet = bets.get(0);
        
        // Verify bet was updated
        assertNotNull(savedBet.getIsWin());
        assertNotNull(savedBet.getPayout());
        
        // Verify user balance was updated if bet was a win
        if (savedBet.getIsWin()) {
            assertEquals(INITIAL_BALANCE + savedBet.getPayout(), testUser.getBalance());
        } else {
            assertEquals(INITIAL_BALANCE, testUser.getBalance());
        }
        
        verify(betRepository).saveAll(anyList());
        verify(userRepository).saveAll(anyList());
    }

    @Test
    void settleEventOutcome_WithSettledBet_ShouldSkipProcessing() {
        // Arrange
        testBet.setIsWin(true);
        testBet.setPayout(200.0);
        when(betRepository.findAllBySessionKey(SESSION_KEY)).thenReturn(List.of(testBet));
        when(driverDataService.getDriversBySession(SESSION_KEY)).thenReturn(testDrivers);

        // Act
        EventResultDTO result = eventService.settleEventOutcome(SESSION_KEY);

        // Assert
        assertNotNull(result);
        // Verify no additional changes were made to the bet
        assertTrue(testBet.getIsWin());
        assertEquals(200.0, testBet.getPayout());
        assertEquals(INITIAL_BALANCE, testUser.getBalance()); // Balance unchanged
        
        verify(betRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }
}
