package com.apibp.events;

import com.apibp.events.model.AppUser;
import com.apibp.events.model.Event;
import com.apibp.events.model.EventSlot;
import com.apibp.events.model.TicketType;
import com.apibp.events.repository.AppUserRepository;
import com.apibp.events.repository.BookingRepository;
import com.apibp.events.repository.EventRepository;
import com.apibp.events.repository.EventSlotRepository;
import com.apibp.events.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import org.hibernate.stat.Statistics;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

@SpringBootTest
@AutoConfigureGraphQlTester
@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class GraphQlControllerTest {

    @Autowired GraphQlTester graphQl;
    @Autowired EventRepository eventRepo;
    @Autowired EventSlotRepository slotRepo;
    @Autowired BookingRepository bookingRepo;
    @Autowired AppUserRepository userRepo;
    @Autowired JwtUtil jwt;
    @Autowired PasswordEncoder encoder;
    @Autowired EntityManager em;

    private String token;
    private EventSlot testSlot;

    @BeforeEach
    void seed() {
        bookingRepo.deleteAll();
        slotRepo.deleteAll();
        eventRepo.deleteAll();

        // Create event
        Event e = new Event();
        e.setTitle("GraphQL Test Event");
        e.setCategory("MUSIC");
        e.setStatus("ON_SALE");
        e.setDescription("test");
        TicketType tt = new TicketType();
        tt.setEvent(e);
        tt.setName("General");
        tt.setPrice(BigDecimal.valueOf(100L));
        e.setTicketTypes(Set.of(tt));
        eventRepo.save(e);

        // Create slot
        testSlot = new EventSlot();
        testSlot.setEvent(e);
        testSlot.setStartsAt(Instant.parse("2099-12-01T10:00:00Z"));
        testSlot.setEndsAt(Instant.parse("2099-12-01T13:00:00Z"));
        testSlot.setCapacity(2);
        slotRepo.save(testSlot);

        // Create user and token
        AppUser user = userRepo.findByUsername("test-graphql-user").orElseGet(() -> {
            AppUser u = new AppUser();
            u.setUsername("test-graphql-user");
            u.setPassword(encoder.encode("pw"));
            u.setRole("USER");
            return userRepo.save(u);
        });
        token = jwt.generate(user.getUsername());
    }

    @Test
    void events_query_executesWithoutError() {
        graphQl.document("{ events { edges { node { id title } } } }")
                .execute()
                .errors().verify();
    }

    @Test
    void slots_query_withWindow_returns() {
        String query = """
            { slots(starts_at: "2099-11-30T00:00:00Z", ends_at: "2099-12-02T00:00:00Z") { id } }
            """;
        graphQl.document(query)
                .execute()
                .errors().verify();
    }

    @Test
    @Transactional
    void events_query_hasConstantQueryCount_acrossPageSizes() {
        // ponytail: machine-checkable proof N+1 is gone — query count stays constant as page size grows
        Statistics stats = em.unwrap(org.hibernate.Session.class)
                .getSessionFactory().getStatistics();

        // Create multiple events to test pagination
        for (int i = 0; i < 10; i++) {
            Event e = new Event();
            e.setTitle("Event " + i);
            e.setCategory("TEST");
            e.setStatus("ON_SALE");
            e.setDescription("test event " + i);
            TicketType tt = new TicketType();
            tt.setEvent(e);
            tt.setName("Ticket " + i);
            tt.setPrice(BigDecimal.TEN);
            e.setTicketTypes(Set.of(tt));

            // Create 2 slots per event
            EventSlot s1 = new EventSlot();
            s1.setEvent(e);
            s1.setStartsAt(Instant.parse("2099-12-01T10:00:00Z"));
            s1.setEndsAt(Instant.parse("2099-12-01T13:00:00Z"));
            s1.setCapacity(10);

            EventSlot s2 = new EventSlot();
            s2.setEvent(e);
            s2.setStartsAt(Instant.parse("2099-12-02T10:00:00Z"));
            s2.setEndsAt(Instant.parse("2099-12-02T13:00:00Z"));
            s2.setCapacity(10);

            e.setSlots(Set.of(s1, s2));
            eventRepo.save(e);
        }

        em.flush();
        em.clear();
        stats.clear();

        // Page 1: limit 5 — run GraphQL query and measure queries
        stats.clear();
        String query1 = "{ events(limit: 5) { edges { node { id title slots { id } } } } }";
        graphQl.document(query1).execute().errors().verify();
        long queriesPage5 = stats.getPrepareStatementCount();

        em.clear();
        stats.clear();

        // Page 2: limit 10 — run GraphQL query and measure queries
        String query2 = "{ events(limit: 10) { edges { node { id title slots { id } } } } }";
        graphQl.document(query2).execute().errors().verify();
        long queriesPage10 = stats.getPrepareStatementCount();

        // Assert query count is constant (both should be 2: 1 for pagination + 1 batch load slots)
        assert queriesPage5 == queriesPage10 :
            "N+1 detected! Page size 5 ran " + queriesPage5 + " queries, size 10 ran " + queriesPage10;
    }
}
