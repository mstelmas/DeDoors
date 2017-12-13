package org.wsd.ontologies.reservation;

import io.vavr.control.Try;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ReservationMessageFacotry {

	private final Agent agent;

	public Try<ACLMessage> buildReservationRequest(@NonNull final AID receiver,
			@NonNull final ReservationDataRequest data) {
		final ACLMessage reservationRequestMessage = new ACLMessage(ACLMessage.REQUEST);

		reservationRequestMessage.addReceiver(receiver);
		reservationRequestMessage.setLanguage(ReservationOntology.codec.getName());
		reservationRequestMessage.setOntology(ReservationOntology.instance.getName());
		return Try.of(() -> {
			agent.getContentManager().fillContent(reservationRequestMessage, new Action(receiver, data));
			log.info("{}", data);
			return reservationRequestMessage;
		});
	}
}
