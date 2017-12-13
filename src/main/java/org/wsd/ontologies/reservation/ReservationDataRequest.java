package org.wsd.ontologies.reservation;

import jade.content.AgentAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Wither
@Data
public class ReservationDataRequest implements AgentAction {

	private Date dateSince;
	private Date dateTo;
	private int numberOfParticipants;
	private Boolean isWeekly;
	/* TODO: Additional fields (+ Ontology mappings for them!):
	    private Boolean isLaboratory;
        private Boolean isSeminaryHall;
        private Boolean isMultimediaProjectorRequired;
        private Boolean isTVRequired;
        private Integer numberOfComputers;
        private Integer specificRoomNumber;
	 */
}
