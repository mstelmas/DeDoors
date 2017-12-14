package org.wsd.agents.lock.validators;

import io.vavr.control.Validation;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.wsd.agents.lock.reservations.Reservation;

public class ReservationValidator {

    /* TODO: implement (check if agent's requested reservation exists) */
    public Validation<String, String> validateReservationExists(@NonNull final ACLMessage message, @NonNull final Reservation reservation) {
        /* For testing purposes:
              Lock4 - returns invalid reservation
         */
        if (StringUtils.startsWith("lock-agent-4", ((AID)message.getAllReceiver().next()).getLocalName())) {
            return Validation.invalid("requested reservation does not exist");
        } else {
            return Validation.valid("ok");
        }
    }
}
