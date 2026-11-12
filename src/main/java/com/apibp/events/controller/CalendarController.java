package com.apibp.events.controller;

import com.apibp.events.dto.BookingDto;
import com.apibp.events.dto.BookingRequest;
import com.apibp.events.dto.SlotDto;
import com.apibp.events.exception.ApiError;
import com.apibp.events.exception.DomainException;
import com.apibp.events.exception.ErrorCode;
import com.apibp.events.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService service;

    @GetMapping("/slots")
    public ResponseEntity<List<SlotDto>> listSlots(
            @RequestParam("starts_at") String startsAt,
            @RequestParam("ends_at")   String endsAt) {

        Instant windowStart = parseInstant(startsAt, "starts_at");
        Instant windowEnd   = parseInstant(endsAt,   "ends_at");

        if (!windowStart.isBefore(windowEnd)) {
            throw new DomainException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST,
                    "starts_at must be before ends_at");
        }

        return ResponseEntity.ok(service.listSlots(windowStart, windowEnd));
    }

    @PostMapping("/bookings")
    public ResponseEntity<BookingDto> createBooking(
            @RequestBody BookingRequest req,
            Authentication auth) {

        BookingDto dto = service.book(req.slotId(), auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    private Instant parseInstant(String value, String paramName) {
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            throw new DomainException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST,
                    paramName + " must be ISO-8601 UTC, e.g. 2026-12-01T09:00:00Z");
        }
    }
}
