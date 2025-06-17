package com.race.bets.test.race_test.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.race.bets.test.race_test.dto.PlaceBetRequestDTO;
import com.race.bets.test.race_test.dto.PlaceBetResponseDTO;
import com.race.bets.test.race_test.exception.DriverMismatchException;
import com.race.bets.test.race_test.exception.DuplicateBetException;
import com.race.bets.test.race_test.exception.EventNotFoundException;
import com.race.bets.test.race_test.exception.InsufficientBalanceException;
import com.race.bets.test.race_test.exception.UserNotFoundException;
import com.race.bets.test.race_test.repository.BetRepository;
import com.race.bets.test.race_test.repository.entity.Bet;
import com.race.bets.test.race_test.repository.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BetServiceTest {

    @Mock
    private BetRepository betRepository;

    @Mock
    private UserBalanceService userBalanceService;

    @Mock
    private EventService eventService;

    @Mock
    private DriverDataService driverDataService;

    @InjectMocks
    private BetService betService;

    private static final String USER_ID = "user123";
    private static final int SESSION_KEY = 123;
    private static final int DRIVER_NUMBER = 44;
    private static final int BET_AMOUNT = 100;
    private static final int INITIAL_BALANCE = 1000;

    private User testUser;
    private PlaceBetRequestDTO placeBetRequest;

    @BeforeEach
    void setUp() {
        testUser = new User(USER_ID, INITIAL_BALANCE);
        placeBetRequest = new PlaceBetRequestDTO(USER_ID, SESSION_KEY, DRIVER_NUMBER, BET_AMOUNT);
    }

    @Test
    void placeBet_WithValidRequest_ShouldPlaceBetSuccessfully() {
        // Arrange
        when(betRepository.existsByUserIdAndSessionKey(USER_ID, SESSION_KEY)).thenReturn(false);
        when(eventService.eventExists(SESSION_KEY)).thenReturn(true);
        when(driverDataService.isSessionDriver(SESSION_KEY, DRIVER_NUMBER)).thenReturn(true);
        when(userBalanceService.withdraw(USER_ID, BET_AMOUNT)).thenReturn(testUser);
        when(betRepository.save(any(Bet.class))).thenAnswer(invocation -> {
            Bet savedBet = invocation.getArgument(0);
            savedBet.setId(1L);
            return savedBet;
        });

        // Act
        PlaceBetResponseDTO response = betService.placeBet(placeBetRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.betId());
        assertEquals(USER_ID, response.userId());
        assertEquals(SESSION_KEY, response.sessionKey());
        assertEquals(DRIVER_NUMBER, response.driverNumber());
        assertEquals(BET_AMOUNT, response.amount());
        assertEquals("PLACED", response.status());

        verify(betRepository).existsByUserIdAndSessionKey(USER_ID, SESSION_KEY);
        verify(eventService).eventExists(SESSION_KEY);
        verify(driverDataService).isSessionDriver(SESSION_KEY, DRIVER_NUMBER);
        verify(userBalanceService).withdraw(USER_ID, BET_AMOUNT);
        verify(betRepository).save(any(Bet.class));
    }

    @Test
    void placeBet_WhenUserAlreadyPlacedBet_ShouldThrowDuplicateBetException() {
        // Arrange
        when(betRepository.existsByUserIdAndSessionKey(USER_ID, SESSION_KEY)).thenReturn(true);

        // Act & Assert
        DuplicateBetException exception = assertThrows(
            DuplicateBetException.class,
            () -> betService.placeBet(placeBetRequest)
        );

        assertEquals("User already placed a bet " + USER_ID + " for session " + SESSION_KEY, 
            exception.getMessage());
        
        verify(betRepository).existsByUserIdAndSessionKey(USER_ID, SESSION_KEY);
        verifyNoMoreInteractions(betRepository, userBalanceService, eventService, driverDataService);
    }

    @Test
    void placeBet_WhenEventDoesNotExist_ShouldThrowEventNotFoundException() {
        // Arrange
        when(betRepository.existsByUserIdAndSessionKey(USER_ID, SESSION_KEY)).thenReturn(false);
        when(eventService.eventExists(SESSION_KEY)).thenReturn(false);

        // Act & Assert
        EventNotFoundException exception = assertThrows(
            EventNotFoundException.class,
            () -> betService.placeBet(placeBetRequest)
        );

        assertEquals("Event with id " + SESSION_KEY + " not found", exception.getMessage());
        
        verify(betRepository).existsByUserIdAndSessionKey(USER_ID, SESSION_KEY);
        verify(eventService).eventExists(SESSION_KEY);
        verifyNoMoreInteractions(betRepository, userBalanceService, driverDataService);
    }

    @Test
    void placeBet_WhenDriverNotInSession_ShouldThrowDriverMismatchException() {
        // Arrange
        when(betRepository.existsByUserIdAndSessionKey(USER_ID, SESSION_KEY)).thenReturn(false);
        when(eventService.eventExists(SESSION_KEY)).thenReturn(true);
        when(driverDataService.isSessionDriver(SESSION_KEY, DRIVER_NUMBER)).thenReturn(false);

        // Act & Assert
        DriverMismatchException exception = assertThrows(
            DriverMismatchException.class,
            () -> betService.placeBet(placeBetRequest)
        );

        assertEquals("Driver with number " + DRIVER_NUMBER + " not found in session " + SESSION_KEY, 
            exception.getMessage());
        
        verify(betRepository).existsByUserIdAndSessionKey(USER_ID, SESSION_KEY);
        verify(eventService).eventExists(SESSION_KEY);
        verify(driverDataService).isSessionDriver(SESSION_KEY, DRIVER_NUMBER);
        verifyNoMoreInteractions(betRepository, userBalanceService);
    }

    @Test
    void placeBet_WhenUserNotFound_ShouldPropagateUserNotFoundException() {
        // Arrange
        when(betRepository.existsByUserIdAndSessionKey(USER_ID, SESSION_KEY)).thenReturn(false);
        when(eventService.eventExists(SESSION_KEY)).thenReturn(true);
        when(driverDataService.isSessionDriver(SESSION_KEY, DRIVER_NUMBER)).thenReturn(true);
        when(userBalanceService.withdraw(USER_ID, BET_AMOUNT))
            .thenThrow(new UserNotFoundException("User not found"));

        // Act & Assert
        assertThrows(
            UserNotFoundException.class,
            () -> betService.placeBet(placeBetRequest)
        );
        
        verify(betRepository).existsByUserIdAndSessionKey(USER_ID, SESSION_KEY);
        verify(eventService).eventExists(SESSION_KEY);
        verify(driverDataService).isSessionDriver(SESSION_KEY, DRIVER_NUMBER);
        verify(userBalanceService).withdraw(USER_ID, BET_AMOUNT);
        verifyNoMoreInteractions(betRepository);
    }

    @Test
    void placeBet_WhenInsufficientBalance_ShouldPropagateInsufficientBalanceException() {
        // Arrange
        when(betRepository.existsByUserIdAndSessionKey(USER_ID, SESSION_KEY)).thenReturn(false);
        when(eventService.eventExists(SESSION_KEY)).thenReturn(true);
        when(driverDataService.isSessionDriver(SESSION_KEY, DRIVER_NUMBER)).thenReturn(true);
        when(userBalanceService.withdraw(USER_ID, BET_AMOUNT))
            .thenThrow(new InsufficientBalanceException("Insufficient balance"));

        // Act & Assert
        assertThrows(
            InsufficientBalanceException.class,
            () -> betService.placeBet(placeBetRequest)
        );
        
        verify(betRepository).existsByUserIdAndSessionKey(USER_ID, SESSION_KEY);
        verify(eventService).eventExists(SESSION_KEY);
        verify(driverDataService).isSessionDriver(SESSION_KEY, DRIVER_NUMBER);
        verify(userBalanceService).withdraw(USER_ID, BET_AMOUNT);
        verifyNoMoreInteractions(betRepository);
    }

    @Test
    void placeBet_ShouldSaveBetWithCorrectParameters() {
        // Arrange
        when(betRepository.existsByUserIdAndSessionKey(USER_ID, SESSION_KEY)).thenReturn(false);
        when(eventService.eventExists(SESSION_KEY)).thenReturn(true);
        when(driverDataService.isSessionDriver(SESSION_KEY, DRIVER_NUMBER)).thenReturn(true);
        when(userBalanceService.withdraw(USER_ID, BET_AMOUNT)).thenReturn(testUser);
        
        ArgumentCaptor<Bet> betCaptor = ArgumentCaptor.forClass(Bet.class);
        when(betRepository.save(betCaptor.capture())).thenAnswer(invocation -> {
            Bet savedBet = invocation.getArgument(0);
            savedBet.setId(1L);
            return savedBet;
        });

        // Act
        betService.placeBet(placeBetRequest);

        // Assert
        Bet savedBet = betCaptor.getValue();
        assertNotNull(savedBet);
        assertEquals(testUser, savedBet.getUser());
        assertEquals(SESSION_KEY, savedBet.getSessionKey());
        assertEquals(DRIVER_NUMBER, savedBet.getDriverNumber());
        assertEquals(BET_AMOUNT, savedBet.getAmount());
        assertNull(savedBet.getIsWin());
        assertNull(savedBet.getPayout());
    }
}
