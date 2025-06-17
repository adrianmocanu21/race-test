package com.race.bets.test.race_test.service;

import com.race.bets.test.race_test.client.openf1.dto.OpenF1DriverDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DriverDataService {

    private final DriverProvider driverProvider;
    private List<OpenF1DriverDTO> allDrivers = new ArrayList<>();

    public DriverDataService(DriverProvider driverProvider) {
        this.driverProvider = driverProvider;
        loadDriverData();
    }

    public void loadDriverData() {
        this.allDrivers = driverProvider.getAllDrivers();
    }

    public List<OpenF1DriverDTO> getDriversBySession(int sessionKey) {
        return allDrivers.stream()
                .filter(driver -> driver.sessionKey() == sessionKey)
                .toList();
    }

    public boolean isSessionDriver(int sessionKey, int driverNumber) {
        return allDrivers.stream()
                .anyMatch(driver -> driver.sessionKey() == sessionKey && driver.driverNumber() == driverNumber);
    }
}
