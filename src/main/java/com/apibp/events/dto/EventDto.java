package com.apibp.events.dto;

import com.apibp.events.model.Event;

public record EventDto(Long id, String title, String category,
                       String status, String description) {
    public static EventDto from(Event e) {
        return new EventDto(e.getId(), e.getTitle(), e.getCategory(),
                            e.getStatus(), e.getDescription());
    }
}
