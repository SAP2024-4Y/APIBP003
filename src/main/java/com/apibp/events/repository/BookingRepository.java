package com.apibp.events.repository;

import com.apibp.events.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsBySlotIdAndUserId(Long slotId, Long userId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.slot.id = :slotId")
    int countBySlotId(@Param("slotId") Long slotId);
}
