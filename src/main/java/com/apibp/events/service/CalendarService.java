package com.apibp.events.service;

import com.apibp.events.dto.BookingDto;
import com.apibp.events.dto.SlotDto;
import com.apibp.events.exception.DomainException;
import com.apibp.events.exception.ErrorCode;
import com.apibp.events.model.AppUser;
import com.apibp.events.model.Booking;
import com.apibp.events.model.EventSlot;
import com.apibp.events.repository.AppUserRepository;
import com.apibp.events.repository.BookingRepository;
import com.apibp.events.repository.EventSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final EventSlotRepository slotRepo;
    private final BookingRepository   bookingRepo;
    private final AppUserRepository   userRepo;

    public List<SlotDto> listSlots(Instant windowStart, Instant windowEnd) {
        return slotRepo.findOverlapping(windowStart, windowEnd)
                       .stream()
                       .map(SlotDto::from)
                       .toList();
    }

    public List<EventSlot> listSlotEntities(Instant windowStart, Instant windowEnd) {
        return slotRepo.findOverlapping(windowStart, windowEnd);
    }

    @Transactional
    public BookingDto book(Long slotId, int quantity, String username) {
        return BookingDto.from(doBook(slotId, quantity, username));
    }

    @Transactional
    public Booking bookEntity(Long slotId, int quantity, String username) {
        return doBook(slotId, quantity, username);
    }

    private Booking doBook(Long slotId, int quantity, String username) {
        EventSlot slot = slotRepo.findByIdForUpdate(slotId)
                .orElseThrow(() -> new DomainException(
                        ErrorCode.SLOT_NOT_FOUND, HttpStatus.NOT_FOUND,
                        "Slot not found: " + slotId));

        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new DomainException(
                        ErrorCode.UNAUTHENTICATED, HttpStatus.UNAUTHORIZED,
                        "User not found: " + username));

        if (slot.getEndsAt().isBefore(Instant.now())) {
            throw new DomainException(ErrorCode.UNPROCESSABLE_BOOKING, HttpStatus.UNPROCESSABLE_ENTITY,
                    "Cannot book a past slot");
        }

        if (bookingRepo.existsBySlotIdAndUserId(slotId, user.getId())) {
            throw new DomainException(ErrorCode.BOOKING_CONFLICT, HttpStatus.CONFLICT,
                    "Already booked this slot");
        }

        if (bookingRepo.countBySlotId(slotId) + quantity > slot.getCapacity()) {
            throw new DomainException(ErrorCode.SLOT_SOLD_OUT, HttpStatus.CONFLICT,
                    "Slot is sold out");
        }

        Booking booking = new Booking();
        booking.setSlot(slot);
        booking.setUser(user);
        booking.setQuantity(quantity);
        booking.setBookedAt(Instant.now());
        return bookingRepo.save(booking);
    }
}
