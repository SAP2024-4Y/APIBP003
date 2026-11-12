package com.apibp.events.repository;

import com.apibp.events.model.EventSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface EventSlotRepository extends JpaRepository<EventSlot, Long> {

    // Overlap: slot starts before window ends AND slot ends after window starts
    @Query("""
            SELECT s FROM EventSlot s
            JOIN FETCH s.event
            WHERE s.startsAt < :windowEnd
              AND s.endsAt   > :windowStart
            ORDER BY s.startsAt ASC
            """)
    List<EventSlot> findOverlapping(@Param("windowStart") Instant windowStart,
                                    @Param("windowEnd")   Instant windowEnd);
}
