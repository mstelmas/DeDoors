package org.wsd.agents.lecturer.reservations;

import com.google.common.collect.ImmutableMap;
import jade.core.AID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/* TODO: manage reservations based on CNP results */
public class ReservationsStateService {
    private final Map<Integer, Reservation> lecturerReservationsMap = ImmutableMap.of(
            1, new Reservation(1, new AID("agent-lock1", AID.ISLOCALNAME)),
            2, new Reservation(2, new AID("agent-lock2", AID.ISLOCALNAME)),
            3, new Reservation(3, new AID("agent-lock3", AID.ISLOCALNAME)),
            4, new Reservation(4, new AID("agent-lock3", AID.ISLOCALNAME)),
            5, new Reservation(5, new AID("agent-lock4", AID.ISLOCALNAME))
    );

    public List<Reservation> findAll() {
        return new ArrayList<>(lecturerReservationsMap.values());
    }
}
