package com.apibp.events.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "event")
public class Event {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String status;

    private String description;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<TicketType> ticketTypes = new HashSet<>();
}
