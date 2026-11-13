package com.apibp.events;

import com.apibp.events.model.Event;
import com.apibp.events.model.Venue;
import com.apibp.events.service.MapService;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureGraphQlTester
class MapGraphQlTest {

    @Autowired GraphQlTester graphQl;
    @MockBean  MapService mapService;

    private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);

    private static Event eventWithVenue() {
        Venue v = new Venue();
        v.setId(1L);
        v.setName("Test Venue");
        v.setAddress("Test Address");
        v.setLocation(GF.createPoint(new Coordinate(77.20, 28.61))); // lon, lat

        Event e = new Event();
        e.setId(1L);
        e.setTitle("Rock Fest");
        e.setCategory("MUSIC");
        e.setStatus("ON_SALE");
        e.setVenue(v);
        return e;
    }

    @Test
    void eventsNearby_returnsEvents() {
        when(mapService.eventsNearby(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(eventWithVenue()));

        graphQl.document("""
                {
                  eventsNearby(latitude: 28.61, longitude: 77.20, radius_km: 5.0) {
                    id title venue { name latitude longitude }
                  }
                }
                """)
                .execute()
                .errors().verify()
                .path("eventsNearby[0].title").entity(String.class).isEqualTo("Rock Fest")
                .path("eventsNearby[0].venue.name").entity(String.class).isEqualTo("Test Venue")
                .path("eventsNearby[0].venue.latitude").entity(Double.class).isEqualTo(28.61)
                .path("eventsNearby[0].venue.longitude").entity(Double.class).isEqualTo(77.20);
    }

    @Test
    void eventsNearby_noVenues_returnsEmpty() {
        when(mapService.eventsNearby(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of());

        graphQl.document("""
                { eventsNearby(latitude: 0.0, longitude: 0.0, radius_km: 1.0) { id } }
                """)
                .execute()
                .errors().verify()
                .path("eventsNearby").entityList(Object.class).hasSize(0);
    }

    @Test
    void eventsWithin_returnsEvents() {
        when(mapService.eventsWithin(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(eventWithVenue()));

        graphQl.document("""
                {
                  eventsWithin(min_lat: 28.5, min_lon: 77.1, max_lat: 28.7, max_lon: 77.3) {
                    id title venue { name latitude longitude }
                  }
                }
                """)
                .execute()
                .errors().verify()
                .path("eventsWithin[0].title").entity(String.class).isEqualTo("Rock Fest")
                .path("eventsWithin[0].venue.latitude").entity(Double.class).isEqualTo(28.61)
                .path("eventsWithin[0].venue.longitude").entity(Double.class).isEqualTo(77.20);
    }

    @Test
    void eventsWithin_noVenues_returnsEmpty() {
        when(mapService.eventsWithin(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of());

        graphQl.document("""
                { eventsWithin(min_lat: 0.0, min_lon: 0.0, max_lat: 1.0, max_lon: 1.0) { id } }
                """)
                .execute()
                .errors().verify()
                .path("eventsWithin").entityList(Object.class).hasSize(0);
    }
}
