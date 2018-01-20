package org.wsd.agents.lock.reservations;

import com.google.common.collect.Lists;

import java.util.List;

public class ReservationStateService {

    private final List<Reservation> lockReservations = Lists.newArrayList();

    public void add(final Reservation reservation) {
        lockReservations.add(reservation);
    }

    public boolean exists(final Reservation reservation) {
        return lockReservations.contains(reservation);
    }
}
