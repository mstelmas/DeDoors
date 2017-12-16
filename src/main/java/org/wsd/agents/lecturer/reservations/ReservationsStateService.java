package org.wsd.agents.lecturer.reservations;

import com.google.common.collect.Lists;
import jade.core.AID;

import java.util.Collections;
import java.util.List;

/* TODO: manage reservations based on CNP results */
public class ReservationsStateService {
    private final List<Reservation> lecturerReservations = Lists.newArrayList(
            new Reservation(1, new AID("lock-agent-1", AID.ISLOCALNAME)),
            new Reservation(2, new AID("lock-agent-2", AID.ISLOCALNAME)),
            new Reservation(3, new AID("lock-agent-3", AID.ISLOCALNAME)),
            new Reservation(4, new AID("lock-agent-3", AID.ISLOCALNAME)),
            new Reservation(5, new AID("lock-agent-4", AID.ISLOCALNAME))
    );

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
