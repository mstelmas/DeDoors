package org.wsd.agents.lock.reservations;

import jade.core.AID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Reservation {
    private Integer id;
    private AID agent;
}
