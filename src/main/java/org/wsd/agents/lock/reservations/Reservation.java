package org.wsd.agents.lock.reservations;

import jade.core.AID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.joda.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
public class Reservation {
    private Integer id;
    private LocalDateTime startOfReservation;
    private LocalDateTime endOfReservation;
    private AID agent;
    private ReservationState reservationState;
}