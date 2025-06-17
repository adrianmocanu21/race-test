package com.race.bets.test.race_test.client.openf1;

import com.race.bets.test.race_test.client.openf1.dto.OpenF1DriverDTO;
import com.race.bets.test.race_test.client.openf1.dto.OpenF1PositionDTO;
import com.race.bets.test.race_test.client.openf1.dto.OpenF1SessionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@FeignClient(name = "openF1Client", url = "${openf1.api.base-url}")
public interface OpenF1Client {

    @GetMapping("/sessions")
    List<OpenF1SessionDTO> getSessions(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "country_name", required = false) String country,
            @RequestParam(value = "session_type", required = false) String sessionType
    );

    @GetMapping("/sessions")
    List<OpenF1SessionDTO> getSessionsBySessionKey(@RequestParam(value = "session_key", required = false) Integer sessionKey);

    @GetMapping("/position")
    List<OpenF1PositionDTO> getPositions(
            @RequestParam("session_key") int sessionKey,
            @RequestParam("position") int position
    );

    @GetMapping("/drivers")
    List<OpenF1DriverDTO> getAllDrivers();

}