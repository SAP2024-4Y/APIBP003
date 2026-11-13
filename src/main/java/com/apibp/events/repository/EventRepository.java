package com.apibp.events.repository;

import com.apibp.events.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>,
        JpaSpecificationExecutor<Event> {

    @Query("SELECT DISTINCT e FROM Event e JOIN FETCH e.venue WHERE e.venue.id IN :venueIds")
    List<Event> findByVenueIdIn(@Param("venueIds") List<Long> venueIds);

    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.slots WHERE e.id IN :ids")
    List<Event> findByIdsWithSlots(@Param("ids") List<Long> ids);
}
