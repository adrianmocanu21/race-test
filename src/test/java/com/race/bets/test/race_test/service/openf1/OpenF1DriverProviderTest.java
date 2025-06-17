package com.race.bets.test.race_test.service.openf1;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.race.bets.test.race_test.client.openf1.OpenF1Client;
import com.race.bets.test.race_test.client.openf1.dto.OpenF1DriverDTO;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OpenF1DriverProviderTest {

    @Mock
    private OpenF1Client openF1Client;

    @InjectMocks
    private OpenF1DriverProvider openF1DriverProvider;

    @Test
    void getAllDrivers_WhenDriversExist_ShouldReturnDriverList() {
        // Arrange
        List<OpenF1DriverDTO> expectedDrivers = Arrays.asList(
            new OpenF1DriverDTO(44, "Lewis Hamilton", 123),
            new OpenF1DriverDTO(33, "Max Verstappen", 123)
        );
        when(openF1Client.getAllDrivers()).thenReturn(expectedDrivers);

        // Act
        List<OpenF1DriverDTO> result = openF1DriverProvider.getAllDrivers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedDrivers, result);
        verify(openF1Client).getAllDrivers();
    }

    @Test
    void getAllDrivers_WhenNoDriversExist_ShouldReturnEmptyList() {
        // Arrange
        when(openF1Client.getAllDrivers()).thenReturn(Collections.emptyList());

        // Act
        List<OpenF1DriverDTO> result = openF1DriverProvider.getAllDrivers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(openF1Client).getAllDrivers();
    }

    @Test
    void getAllDrivers_WhenClientThrowsException_ShouldPropagateException() {
        // Arrange
        when(openF1Client.getAllDrivers()).thenThrow(new RuntimeException("API Error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> openF1DriverProvider.getAllDrivers());
        verify(openF1Client).getAllDrivers();
    }
}
