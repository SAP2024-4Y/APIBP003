package com.apibp.events.dto;

import com.apibp.events.model.EventSlot;

import java.time.Instant;

public record SlotDto(Long id, Long eventId, String eventTitle,
                      Instant startsAt, Instant endsAt, int capacity) {

    public static SlotDto from(EventSlot s) {
        return new SlotDto(s.getId(),
                s.getEvent().getId(),
                s.getEvent().getTitle(),
                s.getStartsAt(),
                s.getEndsAt(),
                s.getCapacity());
    }
}
