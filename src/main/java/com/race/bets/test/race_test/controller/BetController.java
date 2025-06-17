package com.race.bets.test.race_test.controller;

import com.race.bets.test.race_test.dto.PlaceBetRequestDTO;
import com.race.bets.test.race_test.dto.PlaceBetResponseDTO;
import com.race.bets.test.race_test.service.BetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bets")
@RequiredArgsConstructor
public class BetController {

    private final BetService betService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaceBetResponseDTO placeBet(@RequestBody PlaceBetRequestDTO request) {
        return betService.placeBet(request);
    }
}
