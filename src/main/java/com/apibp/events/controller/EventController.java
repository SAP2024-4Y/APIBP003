package com.apibp.events.controller;

import com.apibp.events.model.Event;
import com.apibp.events.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService service;

    @GetMapping
    public ResponseEntity<EventListResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal min_price,
            @RequestParam(required = false) BigDecimal max_price,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String after) {

        return ResponseEntity.ok(
            service.list(status, category, min_price, max_price, sort, order, limit, after));
    }

    // DTO records — nested here, used by service too
    public record EventDto(Long id, String title, String category,
                           String status, String description) {
        public static EventDto from(Event e) {
            return new EventDto(e.getId(), e.getTitle(), e.getCategory(),
                                e.getStatus(), e.getDescription());
        }
    }

    public record EventListResponse(List<EventDto> data, boolean has_more, String next_cursor) {}
}
