package com.apibp.events.dto;

import java.util.List;

public record EventListResponse(List<EventDto> data, boolean has_more, String next_cursor) {}
