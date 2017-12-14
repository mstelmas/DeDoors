package org.wsd.agents.lecturer.reservations;

import jade.core.AID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Reservation {
    private Integer id;
    private AID lock;
}
