package com.apibp.events.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GeoJsonFeature(
        String type,
        Geometry geometry,
        Properties properties
) {
    public record Geometry(String type, double[] coordinates) {}

    public record EventSummary(Long id, String title, String category, String status) {}

    public record Properties(
            Long id,
            String name,
            String address,
            List<EventSummary> events
    ) {}

    public static GeoJsonFeature of(Long id, String name, String address,
                                    double lon, double lat, List<EventSummary> events) {
        return new GeoJsonFeature(
                "Feature",
                new Geometry("Point", new double[]{lon, lat}),
                new Properties(id, name, address, events)
        );
    }
}

