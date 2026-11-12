package com.apibp.events.dto;

import com.apibp.events.model.Booking;

import java.time.Instant;

public record BookingDto(Long id, Long slotId, Long userId, Instant bookedAt) {

    public static BookingDto from(Booking b) {
        return new BookingDto(b.getId(),
                b.getSlot().getId(),
                b.getUser().getId(),
                b.getBookedAt());
    }
}
