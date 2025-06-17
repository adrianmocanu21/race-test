package com.race.bets.test.race_test.service;

import com.race.bets.test.race_test.client.openf1.dto.OpenF1DriverDTO;

import java.util.List;

public interface DriverProvider {

    List<OpenF1DriverDTO> getAllDrivers();

}
