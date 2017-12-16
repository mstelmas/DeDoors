package org.wsd.agents.lock;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import jade.lang.acl.ACLMessage;
import org.wsd.agents.lock.reservations.Reservation;
import org.wsd.agents.lock.validators.PermissionValidator;
import org.wsd.agents.lock.validators.ReservationValidator;
import org.wsd.ontologies.otp.GenerateOTPRequest;

public class LockValidationService {

    private final PermissionValidator permissionValidator = new PermissionValidator();
    private final ReservationValidator reservationValidator = new ReservationValidator();

    /* For testing purposes:
              Lock1/Lock2 - both successfully generate OTP
              Lock3 - returns invalid permissions
              Lock4 - returns invalid reservation
         */
    public Validation<Seq<String>, String> validateGenerateOTPRequest(LockAgent agent, final ACLMessage generateOTPMessage, final GenerateOTPRequest generateOTPRequest) {
        final Reservation requestedReservation = Reservation.builder()
                .id(generateOTPRequest.getReservationId())
                .agent(generateOTPMessage.getSender())
                .build();

        return Validation.combine(
                permissionValidator.validateActionPermissions(agent, generateOTPMessage, generateOTPRequest),
                reservationValidator.validateReservationExists(generateOTPMessage, requestedReservation)
        ).ap((a, b) -> "Valid");
    }
}
