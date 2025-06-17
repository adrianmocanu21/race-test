package com.race.bets.test.race_test.controller;

import com.race.bets.test.race_test.dto.EventResponseDTO;
import com.race.bets.test.race_test.dto.EventResultDTO;
import com.race.bets.test.race_test.service.EventService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public List<EventResponseDTO> getEvents(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String country,
            @RequestParam(required = false, name = "sessionType") String sessionType
    ) {
        return eventService.getFilteredEvents(year, country, sessionType);
    }

    @PostMapping("/{sessionKey}/settle")
    public EventResultDTO getEventResult(@PathVariable Integer sessionKey) {
        return eventService.settleEventOutcome(sessionKey);
    }
}
