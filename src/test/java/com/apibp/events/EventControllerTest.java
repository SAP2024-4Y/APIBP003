package com.apibp.events;

import com.apibp.events.dto.EventListResponse;
import com.apibp.events.model.Event;
import com.apibp.events.model.TicketType;
import com.apibp.events.repository.EventRepository;
import com.apibp.events.util.CursorUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired EventRepository eventRepo;

    @BeforeEach
    void seed() {
        eventRepo.deleteAll();
        for (int i = 1; i <= 10; i++) {
            Event e = new Event();
            e.setTitle("Event " + i);
            e.setCategory(i % 2 == 0 ? "MUSIC" : "SPORTS");
            e.setStatus(i == 5 ? "SOLD_OUT" : "ON_SALE");
            e.setDescription("desc " + i);
            TicketType tt = new TicketType();
            tt.setEvent(e);
            tt.setName("General");
            tt.setPrice(BigDecimal.valueOf(100L * i));
            e.setTicketTypes(Set.of(tt));
            eventRepo.save(e);
        }
    }

    @Test
    void listEvents_defaultPage_returns200() throws Exception {
        mvc.perform(get("/api/v1/events"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.data").isArray())
           .andExpect(jsonPath("$.has_more").isBoolean());
    }

    @Test
    void listEvents_filterByStatus_returnsMatchingOnly() throws Exception {
        var result = mvc.perform(get("/api/v1/events").param("status", "ON_SALE"))
           .andExpect(status().isOk()).andReturn();
        var resp = mapper.readValue(result.getResponse().getContentAsString(), EventListResponse.class);
        assertFalse(resp.data().isEmpty());
        resp.data().forEach(e -> assertEquals("ON_SALE", e.status()));
    }

    @Test
    void listEvents_filterByCategory_returnsMatchingOnly() throws Exception {
        var result = mvc.perform(get("/api/v1/events").param("category", "MUSIC"))
           .andExpect(status().isOk()).andReturn();
        var resp = mapper.readValue(result.getResponse().getContentAsString(), EventListResponse.class);
        assertFalse(resp.data().isEmpty());
        resp.data().forEach(e -> assertEquals("MUSIC", e.category()));
    }

    @Test
    void listEvents_minPrice_filtersCorrectly() throws Exception {
        var result = mvc.perform(get("/api/v1/events").param("min_price", "600"))
           .andExpect(status().isOk()).andReturn();
        var resp = mapper.readValue(result.getResponse().getContentAsString(), EventListResponse.class);
        assertFalse(resp.data().isEmpty());
    }

    @Test
    void listEvents_limitAndCursor_pagesCorrectly() throws Exception {
        var first = mapper.readValue(
            mvc.perform(get("/api/v1/events").param("limit", "3"))
               .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(),
            EventListResponse.class);

        assertTrue(first.has_more());
        assertNotNull(first.next_cursor());

        var second = mapper.readValue(
            mvc.perform(get("/api/v1/events")
                   .param("limit", "3")
                   .param("after", first.next_cursor()))
               .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(),
            EventListResponse.class);

        var firstIds = first.data().stream().map(e -> e.id()).toList();
        second.data().forEach(e -> assertFalse(firstIds.contains(e.id()), "Pages must not overlap"));
    }

    @Test
    void listEvents_malformedCursor_returns400() throws Exception {
        mvc.perform(get("/api/v1/events").param("after", "not-valid-base64!!"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.code").value("INVALID_CURSOR"));
    }

    @Test
    void cursorUtil_roundtrip() {
        String cursor = CursorUtil.encode("id", 42L);
        assertEquals(42L, CursorUtil.decodeId(cursor));
        assertEquals("id", CursorUtil.decodeSortField(cursor));
    }
}
