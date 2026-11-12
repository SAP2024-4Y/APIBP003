package com.apibp.events.controller;

import com.apibp.events.dto.BookingDto;
import com.apibp.events.dto.BookingRequest;
import com.apibp.events.dto.SlotDto;
import com.apibp.events.model.Event;
import com.apibp.events.model.EventSlot;
import com.apibp.events.repository.EventRepository;
import com.apibp.events.repository.EventSlotRepository;
import com.apibp.events.service.CalendarService;
import com.apibp.events.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
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
    private final EventSlotRepository slotRepo;

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

        var response = eventService.listEvents(
                status, category, minPrice, maxPrice, sort, order,
                limit != null ? limit : 20,
                after
        );

        var edges = response.stream()
                .map(event -> new EventEdgePayload(
                        com.apibp.events.util.CursorUtil.encode("id", event.getId()),
                        event
                ))
                .toList();

        // ponytail: pageInfo from first edge's event cursor; has_more by checking size
        String nextCursor = null;
        boolean hasMore = false;
        if (!edges.isEmpty()) {
            // Query fetched limit+1 to detect has_more; trim result to limit
            hasMore = response.size() > Integer.parseInt(String.valueOf(limit != null ? limit : 20));
            if (hasMore) {
                nextCursor = edges.getLast().cursor();
            }
        }

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
        return eventRepo.findById(Long.valueOf(id)).orElse(null);
    }

    @MutationMapping
    public BookingDto createBooking(
            @Argument(name = "input") BookingInputPayload input,
            Authentication auth) {

        return calendarService.book(input.slotId(), auth.getName());
    }

    @SchemaMapping(typeName = "EventSlot", field = "booked")
    public int booked(EventSlot slot) {
        return slotRepo.findById(slot.getId())
                .map(s -> s.getBookings().size())
                .orElse(0);
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
    record EventEdgePayload(String cursor, Object node) {}
    record PageInfoPayload(boolean hasNextPage, String endCursor) {}
    record BookingInputPayload(Long slotId, Integer quantity) {}
}
