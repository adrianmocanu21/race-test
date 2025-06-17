package com.race.bets.test.race_test.config;

import com.race.bets.test.race_test.repository.UserRepository;
import com.race.bets.test.race_test.repository.entity.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseSeeder {

    @Bean
    CommandLineRunner seedDatabase(UserRepository userRepository) {
        return args -> {
            if (!userRepository.existsById("user1")) {
                userRepository.save(User.builder().id("user1").balance(100).build());
            }
            if (!userRepository.existsById("user2")) {
                userRepository.save(User.builder().id("user2").balance(100).build());
            }
            if (!userRepository.existsById("user3")) {
                userRepository.save(User.builder().id("user3").balance(100).build());
            }
        };
    }
}
