package com.race.bets.test.race_test.repository;

import com.race.bets.test.race_test.repository.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

}
