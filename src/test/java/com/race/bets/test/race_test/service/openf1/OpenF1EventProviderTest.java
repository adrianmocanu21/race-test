package com.race.bets.test.race_test.service.openf1;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.race.bets.test.race_test.client.openf1.OpenF1Client;
import com.race.bets.test.race_test.client.openf1.dto.OpenF1DriverDTO;
import com.race.bets.test.race_test.client.openf1.dto.OpenF1SessionDTO;
import com.race.bets.test.race_test.dto.DriverMarketDTO;
import com.race.bets.test.race_test.dto.EventResponseDTO;
import com.race.bets.test.race_test.exception.EventNotFoundException;
import com.race.bets.test.race_test.service.DriverDataService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OpenF1EventProviderTest {

    @Mock
    private OpenF1Client openF1Client;

    @Mock
    private DriverDataService driverDataService;

    @InjectMocks
    private OpenF1EventProvider openF1EventProvider;

    private static final int SESSION_KEY = 123;
    private static final int MEETING_KEY = 1234;
    private static final String COUNTRY = "Romania";
    private static final int YEAR = 2023;
    private static final String SESSION_TYPE = "Race";
    private static final String LOCATION = "Bucharest";


    private OpenF1SessionDTO testSession;
    private OpenF1DriverDTO testDriver1;
    private OpenF1DriverDTO testDriver2;

    @BeforeEach
    void setUp() {
        testSession = new OpenF1SessionDTO(
            SESSION_KEY,
            MEETING_KEY,
            COUNTRY,
            YEAR,
            SESSION_TYPE,
            LOCATION
        );

        testDriver1 = new OpenF1DriverDTO(44, "Lewis Hamilton", SESSION_KEY);
        testDriver2 = new OpenF1DriverDTO(33, "Max Verstappen", SESSION_KEY);
    }

    @Test
    void getEvents_WhenNoFilters_ShouldReturnAllEvents() {
        // Arrange
        List<OpenF1SessionDTO> sessions = List.of(testSession);
        when(openF1Client.getSessions(null, null, null)).thenReturn(sessions);
        when(driverDataService.getDriversBySession(SESSION_KEY))
            .thenReturn(List.of(testDriver1, testDriver2));

        // Act
        List<EventResponseDTO> result = openF1EventProvider.getEvents(null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        EventResponseDTO event = result.get(0);
        assertEquals(SESSION_KEY, event.sessionKey());
        assertEquals(COUNTRY, event.country());
        assertEquals(YEAR, event.year());
        assertEquals(SESSION_TYPE, event.sessionType());
        assertEquals(2, event.drivers().size());
        
        verify(openF1Client).getSessions(null, null, null);
        verify(driverDataService).getDriversBySession(SESSION_KEY);
    }

    @Test
    void getEvents_WithFilters_ShouldReturnFilteredEvents() {
        // Arrange
        List<OpenF1SessionDTO> sessions = List.of(testSession);
        when(openF1Client.getSessions(YEAR, COUNTRY, SESSION_TYPE)).thenReturn(sessions);
        when(driverDataService.getDriversBySession(SESSION_KEY))
            .thenReturn(List.of(testDriver1, testDriver2));

        // Act
        List<EventResponseDTO> result = openF1EventProvider.getEvents(YEAR, COUNTRY, SESSION_TYPE);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(openF1Client).getSessions(YEAR, COUNTRY, SESSION_TYPE);
    }

    @Test
    void getEvents_WhenNoSessionsFound_ShouldReturnEmptyList() {
        // Arrange
        when(openF1Client.getSessions(any(), any(), any())).thenReturn(Collections.emptyList());

        // Act
        List<EventResponseDTO> result = openF1EventProvider.getEvents(null, null, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(openF1Client).getSessions(null, null, null);
        verifyNoInteractions(driverDataService);
    }

    @Test
    void getEventBySessionKey_WhenSessionExists_ShouldReturnEvent() {
        // Arrange
        List<OpenF1SessionDTO> sessions = List.of(testSession);
        when(openF1Client.getSessionsBySessionKey(SESSION_KEY)).thenReturn(sessions);
        when(driverDataService.getDriversBySession(SESSION_KEY))
            .thenReturn(List.of(testDriver1, testDriver2));

        // Act
        EventResponseDTO result = openF1EventProvider.getEventBySessionKey(SESSION_KEY);

        // Assert
        assertNotNull(result);
        assertEquals(SESSION_KEY, result.sessionKey());
        assertEquals(COUNTRY, result.country());
        assertEquals(YEAR, result.year());
        assertEquals(SESSION_TYPE, result.sessionType());
        assertEquals(2, result.drivers().size());
        
        // Verify driver markets
        List<DriverMarketDTO> driverMarkets = result.drivers();
        assertTrue(driverMarkets.stream().anyMatch(d -> d.driverNumber() == 44));
        assertTrue(driverMarkets.stream().anyMatch(d -> d.driverNumber() == 33));
        
        verify(openF1Client).getSessionsBySessionKey(SESSION_KEY);
        verify(driverDataService).getDriversBySession(SESSION_KEY);
    }

    @Test
    void getEventBySessionKey_WhenSessionNotFound_ShouldThrowEventNotFoundException() {
        // Arrange
        when(openF1Client.getSessionsBySessionKey(SESSION_KEY)).thenReturn(Collections.emptyList());

        // Act & Assert
        EventNotFoundException exception = assertThrows(
            EventNotFoundException.class,
            () -> openF1EventProvider.getEventBySessionKey(SESSION_KEY)
        );

        assertEquals("Event with id " + SESSION_KEY + " not found", exception.getMessage());
        verify(openF1Client).getSessionsBySessionKey(SESSION_KEY);
        verifyNoInteractions(driverDataService);
    }

    @Test
    void getRandomOdds_ShouldReturnValidOdds() {
        // Act & Assert - Test multiple times to ensure random distribution
        for (int i = 0; i < 10; i++) {
            int odds = OpenF1EventProvider.getRandomOdds();
            assertTrue(odds >= 2 && odds <= 4, "Odds should be between 2 and 4");
        }
    }
}
