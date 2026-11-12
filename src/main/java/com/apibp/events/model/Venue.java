package com.apibp.events.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

@Getter
@Setter
@Entity
@Table(name = "venue")
public class Venue {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    // geography(Point,4326) — lon/lat stored as JTS Point
    @Column(nullable = false, columnDefinition = "geography(Point,4326)")
    private Point location;
}
