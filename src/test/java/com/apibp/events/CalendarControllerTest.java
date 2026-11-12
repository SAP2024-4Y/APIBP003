package com.apibp.events;

import com.apibp.events.dto.BookingDto;
import com.apibp.events.dto.BookingRequest;
import com.apibp.events.dto.SlotDto;
import com.apibp.events.model.AppUser;
import com.apibp.events.model.Event;
import com.apibp.events.model.EventSlot;
import com.apibp.events.repository.AppUserRepository;
import com.apibp.events.repository.BookingRepository;
import com.apibp.events.repository.EventRepository;
import com.apibp.events.repository.EventSlotRepository;
import com.apibp.events.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CalendarControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired EventSlotRepository slotRepo;
    @Autowired BookingRepository bookingRepo;
    @Autowired EventRepository eventRepo;
    @Autowired AppUserRepository userRepo;
    @Autowired JwtUtil jwt;
    @Autowired PasswordEncoder encoder;

    private EventSlot slot;
    private String token;

    @BeforeEach
    void seed() {
        bookingRepo.deleteAll();
        slotRepo.deleteAll();
        // keep users/events across tests — just purge what we own

        Event event = eventRepo.findAll().stream().findFirst().orElseGet(() -> {
            Event e = new Event();
            e.setTitle("Test Event");
            e.setCategory("MUSIC");
            e.setStatus("ON_SALE");
            e.setDescription("test");
            return eventRepo.save(e);
        });

        slot = new EventSlot();
        slot.setEvent(event);
        slot.setStartsAt(Instant.parse("2099-12-01T10:00:00Z"));
        slot.setEndsAt(Instant.parse("2099-12-01T13:00:00Z"));
        slot.setCapacity(2);
        slot = slotRepo.save(slot);

        AppUser user = userRepo.findByUsername("test-cal-user").orElseGet(() -> {
            AppUser u = new AppUser();
            u.setUsername("test-cal-user");
            u.setPassword(encoder.encode("pw"));
            u.setRole("USER");
            return userRepo.save(u);
        });
        token = jwt.generate(user.getUsername());
    }

    // --- GET /api/v1/slots ---

    @Test
    void listSlots_overlappingWindow_returnsSlot() throws Exception {
        var result = mvc.perform(get("/api/v1/slots")
                        .param("starts_at", "2099-11-30T00:00:00Z")
                        .param("ends_at",   "2099-12-02T00:00:00Z"))
                .andExpect(status().isOk())
                .andReturn();

        List<SlotDto> slots = mapper.readValue(
                result.getResponse().getContentAsString(),
                mapper.getTypeFactory().constructCollectionType(List.class, SlotDto.class));

        assertTrue(slots.stream().anyMatch(s -> s.id().equals(slot.getId())));
    }

    @Test
    void listSlots_nonOverlappingWindow_returnsEmpty() throws Exception {
        var result = mvc.perform(get("/api/v1/slots")
                        .param("starts_at", "2020-01-01T00:00:00Z")
                        .param("ends_at",   "2020-01-02T00:00:00Z"))
                .andExpect(status().isOk())
                .andReturn();

        List<SlotDto> slots = mapper.readValue(
                result.getResponse().getContentAsString(),
                mapper.getTypeFactory().constructCollectionType(List.class, SlotDto.class));

        assertTrue(slots.stream().noneMatch(s -> s.id().equals(slot.getId())));
    }

    @Test
    void listSlots_missingParam_returns400() throws Exception {
        mvc.perform(get("/api/v1/slots").param("starts_at", "2099-12-01T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void listSlots_malformedDate_returns400() throws Exception {
        mvc.perform(get("/api/v1/slots")
                        .param("starts_at", "not-a-date")
                        .param("ends_at",   "2099-12-02T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    // --- POST /api/v1/bookings ---

    @Test
    void createBooking_validRequest_returns201WithId() throws Exception {
        var result = mvc.perform(post("/api/v1/bookings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new BookingRequest(slot.getId()))))
                .andExpect(status().isCreated())
                .andReturn();

        BookingDto dto = mapper.readValue(result.getResponse().getContentAsString(), BookingDto.class);
        assertNotNull(dto.id());
        assertEquals(slot.getId(), dto.slotId());
    }

    @Test
    void createBooking_duplicate_returns409Conflict() throws Exception {
        String body = mapper.writeValueAsString(new BookingRequest(slot.getId()));
        // First booking succeeds
        mvc.perform(post("/api/v1/bookings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        // Second booking is a conflict
        mvc.perform(post("/api/v1/bookings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BOOKING_CONFLICT"));
    }

    @Test
    void createBooking_capacityExceeded_returns409SoldOut() throws Exception {
        // Capacity is 2; book with two different users
        AppUser other = userRepo.findByUsername("test-cal-user2").orElseGet(() -> {
            AppUser u = new AppUser();
            u.setUsername("test-cal-user2");
            u.setPassword(encoder.encode("pw"));
            u.setRole("USER");
            return userRepo.save(u);
        });
        String token2 = jwt.generate(other.getUsername());
        String body = mapper.writeValueAsString(new BookingRequest(slot.getId()));

        mvc.perform(post("/api/v1/bookings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/bookings")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        // Now capacity (2) exhausted — third user
        AppUser third = userRepo.findByUsername("test-cal-user3").orElseGet(() -> {
            AppUser u = new AppUser();
            u.setUsername("test-cal-user3");
            u.setPassword(encoder.encode("pw"));
            u.setRole("USER");
            return userRepo.save(u);
        });
        mvc.perform(post("/api/v1/bookings")
                        .header("Authorization", "Bearer " + jwt.generate(third.getUsername()))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SLOT_SOLD_OUT"));
    }

    @Test
    void createBooking_noAuth_returns401() throws Exception {
        mvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new BookingRequest(slot.getId()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createBooking_unknownSlot_returns404() throws Exception {
        mvc.perform(post("/api/v1/bookings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new BookingRequest(999999L))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SLOT_NOT_FOUND"));
    }
}
