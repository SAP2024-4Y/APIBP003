package com.apibp.events.repository;

import com.apibp.events.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EventRepository extends JpaRepository<Event, Long>,
        JpaSpecificationExecutor<Event> {
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>,
        JpaSpecificationExecutor<Event> {

    @Query("SELECT e FROM Event e WHERE e.venue.id IN :venueIds")
    List<Event> findByVenueIdIn(@Param("venueIds") List<Long> venueIds);
}
