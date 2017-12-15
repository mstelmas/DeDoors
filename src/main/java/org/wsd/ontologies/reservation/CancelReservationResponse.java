package org.wsd.ontologies.reservation;

import jade.content.Concept;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@NoArgsConstructor
@AllArgsConstructor
@Wither
@Data
public class CancelReservationResponse implements Concept {
    private Integer reservationId;
}
