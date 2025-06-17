package com.race.bets.test.race_test.service;

import com.race.bets.test.race_test.exception.InsufficientBalanceException;
import com.race.bets.test.race_test.exception.UserNotFoundException;
import com.race.bets.test.race_test.repository.UserRepository;
import com.race.bets.test.race_test.repository.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserBalanceService {

    private final UserRepository userRepository;

    public User withdraw(String userId, int amount) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));
        if (user.getBalance() < amount) {
            throw new InsufficientBalanceException("Insufficient balance for user " + userId);
        }
        user.setBalance(user.getBalance() - amount);
        return userRepository.save(user);
    }
}

