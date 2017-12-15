package org.wsd.agents.lock.reservations;

import jade.core.AID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Reservation {
    private Integer id;
    private AID agent;
}