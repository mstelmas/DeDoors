package org.wsd.ontologies.reservation;

import java.util.Date;

import jade.content.AgentAction;
import jade.content.Concept;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Wither;

@NoArgsConstructor
@AllArgsConstructor
@Wither
@Data
public class ReservationDataRequest implements AgentAction {

	private Date dateSince;
	private Date dateTo;
	private int numberOfParticipants;
	private Boolean isWeekly;
}
