package com.apibp.events.service;

import com.apibp.events.dto.GeoJsonFeature;
import com.apibp.events.dto.GeoJsonFeature.EventSummary;
import com.apibp.events.dto.GeoJsonFeatureCollection;
import com.apibp.events.model.Event;
import com.apibp.events.model.Venue;
import com.apibp.events.repository.EventRepository;
import com.apibp.events.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MapService {

    private final VenueRepository venueRepo;
    private final EventRepository eventRepo;

    public GeoJsonFeatureCollection nearby(double lat, double lon, double radiusKm) {
        return toCollection(venueRepo.findNearby(lat, lon, radiusKm * 1000));
    }

    public GeoJsonFeatureCollection within(double minLat, double minLon,
                                            double maxLat, double maxLon) {
        return toCollection(venueRepo.findWithin(minLat, minLon, maxLat, maxLon));
    }

    private GeoJsonFeatureCollection toCollection(List<Venue> venues) {
        if (venues.isEmpty()) return GeoJsonFeatureCollection.of(List.of());

        // single query for all events across all venues — no N+1
        List<Long> venueIds = venues.stream().map(Venue::getId).toList();
        Map<Long, List<Event>> eventsByVenue = eventRepo.findByVenueIdIn(venueIds)
                .stream().collect(Collectors.groupingBy(e -> e.getVenue().getId()));

        List<GeoJsonFeature> features = venues.stream().map(v -> {
            List<EventSummary> events = eventsByVenue.getOrDefault(v.getId(), List.of())
                    .stream()
                    .map(e -> new EventSummary(e.getId(), e.getTitle(), e.getCategory(), e.getStatus()))
                    .toList();
            return GeoJsonFeature.of(
                    v.getId(),
                    v.getName(),
                    v.getAddress(),
                    v.getLocation().getX(), // longitude
                    v.getLocation().getY(), // latitude
                    events
            );
        }).toList();

        return GeoJsonFeatureCollection.of(features);
    }
}

