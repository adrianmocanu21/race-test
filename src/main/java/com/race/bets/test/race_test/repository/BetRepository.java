package com.race.bets.test.race_test.repository;

import com.race.bets.test.race_test.repository.entity.Bet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BetRepository extends JpaRepository<Bet, Long> {

    boolean existsByUserIdAndSessionKey(String userId, Integer sessionKey);

    List<Bet> findAllBySessionKey(Integer sessionKey);
}
