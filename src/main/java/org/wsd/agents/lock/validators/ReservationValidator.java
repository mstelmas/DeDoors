package org.wsd.agents.lock.validators;

import io.vavr.control.Validation;
import jade.lang.acl.ACLMessage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.joda.time.LocalDateTime;
import org.wsd.agents.lock.reservations.Reservation;
import org.wsd.agents.lock.reservations.ReservationStateService;

@RequiredArgsConstructor
public class ReservationValidator {

    private final int LOCK_OPENING_INTERVAL_MINUTES = 30;

    private final ReservationStateService reservationStateService;

    /* TODO: implement (check if agent's requested reservation exists) */
    public Validation<String, String> validateReservationExists(@NonNull final ACLMessage message, @NonNull final Reservation reservation) {
        if (!reservationStateService.isConfirmed(reservation.getId())) {
            return Validation.invalid("requested reservation does not exist");
        } else if (LocalDateTime.now().isBefore(reservationStateService.getReservationStartDate(reservation.getId()).minusMinutes(LOCK_OPENING_INTERVAL_MINUTES))) {
            return Validation.invalid(String.format("too early to open lock for reservation %d. Try again %d minutes before reservation date", reservation.getId(), LOCK_OPENING_INTERVAL_MINUTES));
        } else if (LocalDateTime.now().isAfter(reservationStateService.getReservationStartDate(reservation.getId()))) {
            return Validation.invalid(String.format("reservation %d expired!", reservation.getId()));
        }
        else {
            return Validation.valid("ok");
        }
    }
}
