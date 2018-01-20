package org.wsd.agents.lock.reservations;

import com.google.common.collect.Maps;
import org.joda.time.LocalDateTime;

import java.util.Date;
import java.util.Map;

public class ReservationStateService {

    private final Map<Integer, Reservation> lockReservations = Maps.newConcurrentMap();

    public void add(final Reservation reservation) {
        lockReservations.put(reservation.getId(), reservation);
    }

    public void confirm(final int reservationId) {
        if (lockReservations.containsKey(reservationId)) {
            final Reservation reservation = lockReservations.get(reservationId);
            reservation.setReservationState(ReservationState.CONFIRMED);
        }
    }

    public LocalDateTime getReservationStartDate(final int reservationId) {
        return lockReservations.get(reservationId).getStartOfReservation();
    }

    public LocalDateTime getReservationEndDate(final int reservationId) {
        return lockReservations.get(reservationId).getEndOfReservation();
    }

    public boolean reservationAvailable(final Date start, final Date end) {
        LocalDateTime startOfReservation = LocalDateTime.fromDateFields(start);
        LocalDateTime endOfReservation = LocalDateTime.fromDateFields(end);

        return lockReservations.values().stream()
                .allMatch(reservation ->
                        endOfReservation.isBefore(reservation.getStartOfReservation()) ||
                        startOfReservation.isAfter(reservation.getEndOfReservation())
                );
    }

    public boolean isConfirmed(final int reservationId) {
        if (!lockReservations.containsKey(reservationId)) {
            return false;
        }

        return lockReservations.get(reservationId).getReservationState() == ReservationState.CONFIRMED;
    }
}
