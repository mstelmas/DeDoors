package org.wsd.agents.lecturer.reservations;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

public class ReservationsStateService {
    private final List<Reservation> lecturerReservations = Lists.newArrayList();

    public List<Reservation> findAll() {
        return Collections.unmodifiableList(lecturerReservations);
    }

    public void remove(final Reservation reservation) {
        lecturerReservations.remove(reservation);
    }

    public void add(final Reservation reservation) {
        lecturerReservations.add(reservation);
    }
}
