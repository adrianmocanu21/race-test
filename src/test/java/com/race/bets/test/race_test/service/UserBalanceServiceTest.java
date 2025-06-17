package com.race.bets.test.race_test.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.race.bets.test.race_test.exception.InsufficientBalanceException;
import com.race.bets.test.race_test.exception.UserNotFoundException;
import com.race.bets.test.race_test.repository.UserRepository;
import com.race.bets.test.race_test.repository.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserBalanceServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserBalanceService userBalanceService;

    private static final String USER_ID = "testUser";
    private static final int INITIAL_BALANCE = 100;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(USER_ID, INITIAL_BALANCE);
    }

    @Test
    void withdraw_WithSufficientBalance_ShouldUpdateBalance() {
        // Arrange
        int amount = 50;
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userBalanceService.withdraw(USER_ID, amount);

        // Assert
        assertNotNull(result);
        assertEquals(INITIAL_BALANCE - amount, result.getBalance());
        verify(userRepository).findById(USER_ID);
        verify(userRepository).save(testUser);
    }

    @Test
    void withdraw_WithInsufficientBalance_ShouldThrowException() {
        // Arrange
        int amount = 150; // More than initial balance
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(InsufficientBalanceException.class, 
            () -> userBalanceService.withdraw(USER_ID, amount));
        verify(userRepository).findById(USER_ID);
        verify(userRepository, never()).save(any());
    }

    @Test
    void withdraw_NonExistentUser_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, 
            () -> userBalanceService.withdraw(USER_ID, 50));
        verify(userRepository).findById(USER_ID);
        verify(userRepository, never()).save(any());
    }

    @Test
    void withdraw_WithExactBalance_ShouldSucceed() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userBalanceService.withdraw(USER_ID, INITIAL_BALANCE);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getBalance());
        verify(userRepository).findById(USER_ID);
        verify(userRepository).save(testUser);
    }
}
