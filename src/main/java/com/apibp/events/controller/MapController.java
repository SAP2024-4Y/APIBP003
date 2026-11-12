package com.apibp.events.controller;

import com.apibp.events.dto.GeoJsonFeatureCollection;
import com.apibp.events.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class MapController {

    private final MapService service;

    @GetMapping("/nearby")
    public ResponseEntity<GeoJsonFeatureCollection> nearby(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double radius_km) {

        return ResponseEntity.ok(service.nearby(latitude, longitude, radius_km));
    }

    @GetMapping("/within")
    public ResponseEntity<GeoJsonFeatureCollection> within(
            @RequestParam double min_lat,
            @RequestParam double min_lon,
            @RequestParam double max_lat,
            @RequestParam double max_lon) {

        return ResponseEntity.ok(service.within(min_lat, min_lon, max_lat, max_lon));
    }
}
