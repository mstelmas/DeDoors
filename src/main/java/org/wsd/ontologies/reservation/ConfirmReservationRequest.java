package org.wsd.ontologies.reservation;

import jade.content.AgentAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@NoArgsConstructor
@AllArgsConstructor
@Wither
@Data
public class ConfirmReservationRequest implements AgentAction {
    private Integer reservationId;
}
