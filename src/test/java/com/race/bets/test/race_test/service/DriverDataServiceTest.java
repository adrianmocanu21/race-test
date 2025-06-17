package com.race.bets.test.race_test.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.race.bets.test.race_test.client.openf1.dto.OpenF1DriverDTO;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DriverDataServiceTest {

    @Mock
    private DriverProvider driverProvider;

    @InjectMocks
    private DriverDataService driverDataService;

    private static final int SESSION_KEY_1 = 123;
    private static final int SESSION_KEY_2 = 456;
    private static final int DRIVER_NUMBER_1 = 44;
    private static final int DRIVER_NUMBER_2 = 33;
    private static final String DRIVER_NAME_1 = "Lewis Hamilton";
    private static final String DRIVER_NAME_2 = "Max Verstappen";

    private OpenF1DriverDTO driver1;
    private OpenF1DriverDTO driver2;
    private OpenF1DriverDTO driver3;

    @BeforeEach
    void setUp() {
        driver1 = new OpenF1DriverDTO(DRIVER_NUMBER_1, DRIVER_NAME_1, SESSION_KEY_1);
        driver2 = new OpenF1DriverDTO(DRIVER_NUMBER_2, DRIVER_NAME_2, SESSION_KEY_1);
        driver3 = new OpenF1DriverDTO(5, "Sebastian Vettel", SESSION_KEY_2);
    }

    @Test
    void loadDriverData_ShouldLoadAllDriversFromProvider() {
        // Arrange - Reset the mock to clear the constructor call
        reset(driverProvider);
        List<OpenF1DriverDTO> expectedDrivers = Arrays.asList(driver1, driver2, driver3);
        when(driverProvider.getAllDrivers()).thenReturn(expectedDrivers);

        // Act
        driverDataService.loadDriverData();

        // Assert
        verify(driverProvider, times(1)).getAllDrivers();
        // Verify the data was loaded by checking one of the service methods
        assertFalse(driverDataService.getDriversBySession(SESSION_KEY_1).isEmpty());
    }

    @Test
    void getDriversBySession_WhenSessionExists_ShouldReturnFilteredDrivers() {
        // Arrange
        List<OpenF1DriverDTO> allDrivers = Arrays.asList(driver1, driver2, driver3);
        when(driverProvider.getAllDrivers()).thenReturn(allDrivers);
        driverDataService.loadDriverData();

        // Act
        List<OpenF1DriverDTO> result = driverDataService.getDriversBySession(SESSION_KEY_1);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(driver -> driver.sessionKey() == SESSION_KEY_1));
        assertTrue(result.contains(driver1));
        assertTrue(result.contains(driver2));
        assertFalse(result.contains(driver3));
    }

    @Test
    void getDriversBySession_WhenSessionDoesNotExist_ShouldReturnEmptyList() {
        // Arrange
        List<OpenF1DriverDTO> allDrivers = Arrays.asList(driver1, driver2);
        when(driverProvider.getAllDrivers()).thenReturn(allDrivers);
        driverDataService.loadDriverData();

        // Act
        List<OpenF1DriverDTO> result = driverDataService.getDriversBySession(999);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void isSessionDriver_WhenDriverExistsInSession_ShouldReturnTrue() {
        // Arrange
        List<OpenF1DriverDTO> allDrivers = Arrays.asList(driver1, driver2, driver3);
        when(driverProvider.getAllDrivers()).thenReturn(allDrivers);
        driverDataService.loadDriverData();

        // Act
        boolean result = driverDataService.isSessionDriver(SESSION_KEY_1, DRIVER_NUMBER_1);

        // Assert
        assertTrue(result);
    }

    @Test
    void isSessionDriver_WhenDriverDoesNotExistInSession_ShouldReturnFalse() {
        // Arrange
        List<OpenF1DriverDTO> allDrivers = Arrays.asList(driver1, driver2);
        when(driverProvider.getAllDrivers()).thenReturn(allDrivers);
        driverDataService.loadDriverData();

        // Act
        boolean result = driverDataService.isSessionDriver(SESSION_KEY_1, 99); // Non-existent driver number

        // Assert
        assertFalse(result);
    }

    @Test
    void isSessionDriver_WhenSessionDoesNotExist_ShouldReturnFalse() {
        // Arrange
        List<OpenF1DriverDTO> allDrivers = Arrays.asList(driver1, driver2);
        when(driverProvider.getAllDrivers()).thenReturn(allDrivers);
        driverDataService.loadDriverData();

        // Act
        boolean result = driverDataService.isSessionDriver(999, DRIVER_NUMBER_1);

        // Assert
        assertFalse(result);
    }

    @Test
    void isSessionDriver_WhenNoDriversLoaded_ShouldReturnFalse() {
        // Arrange - don't load any drivers
        when(driverProvider.getAllDrivers()).thenReturn(Collections.emptyList());
        driverDataService.loadDriverData();

        // Act
        boolean result = driverDataService.isSessionDriver(SESSION_KEY_1, DRIVER_NUMBER_1);

        // Assert
        assertFalse(result);
    }
}
