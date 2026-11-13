package com.apibp.events.controller;

import com.apibp.events.dto.BookingDto;
import com.apibp.events.dto.SlotDto;
import com.apibp.events.exception.DomainException;
import com.apibp.events.exception.ErrorCode;
import com.apibp.events.model.Event;
import com.apibp.events.model.EventSlot;
import com.apibp.events.repository.EventRepository;
import com.apibp.events.service.CalendarService;
import com.apibp.events.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class GraphQlController {

    private final EventService eventService;
    private final CalendarService calendarService;
    private final EventRepository eventRepo;

    @QueryMapping
    public EventConnectionPayload events(
            @Argument(name = "status") String status,
            @Argument(name = "category") String category,
            @Argument(name = "min_price") BigDecimal minPrice,
            @Argument(name = "max_price") BigDecimal maxPrice,
            @Argument(name = "sort") String sort,
            @Argument(name = "order") String order,
            @Argument(name = "limit") Integer limit,
            @Argument(name = "after") String after) {

        int effectiveLimit = limit != null ? limit : 20;
        var response = eventService.listEvents(
                status, category, minPrice, maxPrice, sort, order,
                effectiveLimit,
                after
        );

        boolean hasMore = response.size() > effectiveLimit;
        List<Event> page = hasMore ? response.subList(0, effectiveLimit) : response;

        var edges = page.stream()
                .map(event -> new EventEdgePayload(
                        com.apibp.events.util.CursorUtil.encode("id", event.getId()),
                        event
                ))
                .toList();

        String nextCursor = hasMore ? edges.getLast().cursor() : null;
        var pageInfo = new PageInfoPayload(hasMore, nextCursor);
        return new EventConnectionPayload(edges, pageInfo);
    }

    @QueryMapping
    public List<SlotDto> slots(
            @Argument(name = "starts_at") String startsAt,
            @Argument(name = "ends_at") String endsAt) {

        Instant windowStart = parseInstant(startsAt);
        Instant windowEnd = parseInstant(endsAt);

        return calendarService.listSlots(windowStart, windowEnd);
    }

    @QueryMapping
    public Event event(@Argument(name = "id") String id) {
        return eventRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new DomainException(ErrorCode.EVENT_NOT_FOUND, HttpStatus.NOT_FOUND, "Event not found: " + id));
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public BookingDto createBooking(
            @Argument(name = "input") BookingInputPayload input,
            Authentication auth) {

        return calendarService.book(input.slotId(), auth.getName());
    }

    @SchemaMapping(typeName = "EventSlot", field = "booked")
    public int booked(EventSlot slot) {
        return slot.getBookings().size();
    }

    @SchemaMapping(typeName = "EventSlot", field = "available")
    public int available(EventSlot slot) {
        int booked = booked(slot);
        return slot.getCapacity() - booked;
    }

    private Instant parseInstant(String value) {
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid ISO-8601 timestamp: " + value, e);
        }
    }

    // Payload DTOs for GraphQL responses
    record EventConnectionPayload(List<EventEdgePayload> edges, PageInfoPayload pageInfo) {}
    record EventEdgePayload(String cursor, Event node) {}
    record PageInfoPayload(boolean hasNextPage, String endCursor) {}
    record BookingInputPayload(Long slotId, Integer quantity) {}
}
