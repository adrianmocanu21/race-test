package com.race.bets.test.race_test.service.openf1;

import com.race.bets.test.race_test.client.openf1.OpenF1Client;
import com.race.bets.test.race_test.client.openf1.dto.OpenF1DriverDTO;
import java.util.List;

import com.race.bets.test.race_test.service.DriverProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpenF1DriverProvider implements DriverProvider {

    private final OpenF1Client openF1Client;

    @Override
    public List<OpenF1DriverDTO> getAllDrivers() {
        return openF1Client.getAllDrivers();
    }

}