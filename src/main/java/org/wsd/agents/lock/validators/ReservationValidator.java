package org.wsd.agents.lock.validators;

import io.vavr.control.Validation;
import jade.lang.acl.ACLMessage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.wsd.agents.lock.reservations.Reservation;
import org.wsd.agents.lock.reservations.ReservationStateService;

@RequiredArgsConstructor
public class ReservationValidator {

    private final ReservationStateService reservationStateService;

    /* TODO: implement (check if agent's requested reservation exists) */
    public Validation<String, String> validateReservationExists(@NonNull final ACLMessage message, @NonNull final Reservation reservation) {
        if (reservationStateService.exists(reservation)) {
            return Validation.valid("ok");
        } else {
            return Validation.invalid("requested reservation does not exist");
        }
    }
}
