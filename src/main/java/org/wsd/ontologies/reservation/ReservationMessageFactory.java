package org.wsd.ontologies.reservation;

import io.vavr.control.Try;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ReservationMessageFactory {

	private final Agent agent;

	public Try<ACLMessage> buildReservationRequest(@NonNull final AID receiver, @NonNull final ReservationDataRequest data) {
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

	public Try<ACLMessage> buildCallForProposalRequest(@NonNull final List<AID> receivers, @NonNull final ReservationDataRequest data) {
		final ACLMessage cfpRequestMessage = new ACLMessage(ACLMessage.CFP);

		receivers.forEach(cfpRequestMessage::addReceiver);
		cfpRequestMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
		cfpRequestMessage.setReplyByDate(Date.from(LocalDateTime.now().plusSeconds(10).toInstant(ZoneOffset.ofHours(0))));
		cfpRequestMessage.setLanguage(ReservationOntology.codec.getName());
		cfpRequestMessage.setOntology(ReservationOntology.instance.getName());
		return Try.of(() -> {
			agent.getContentManager().fillContent(cfpRequestMessage, new Action((AID)cfpRequestMessage.getAllReceiver().next(), data));
			log.info("{}", data);
			return cfpRequestMessage;
		});
	}

	public Try<ACLMessage> buildReservationOfferReply(@NonNull final ACLMessage cfpMessage, final int reservationOfferScore) {
		final ACLMessage reservationOfferMessage = cfpMessage.createReply();
		reservationOfferMessage.setPerformative(ACLMessage.PROPOSE);
		reservationOfferMessage.setLanguage(ReservationOntology.codec.getName());
		reservationOfferMessage.setOntology(ReservationOntology.instance.getName());
		return Try.of(() -> {
			agent.getContentManager().fillContent(reservationOfferMessage, new Action(cfpMessage.getSender(), new ReservationOffer(reservationOfferScore)));
			return reservationOfferMessage;
		});
	}

    public Try<ACLMessage> buildOfferAcceptanceReply(@NonNull final ACLMessage offerMessage) {
        final ACLMessage acceptOfferMessage = offerMessage.createReply();
        acceptOfferMessage.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        acceptOfferMessage.setLanguage(ReservationOntology.codec.getName());
        acceptOfferMessage.setOntology(ReservationOntology.instance.getName());

        return Try.success(acceptOfferMessage);
    }

    public Try<ACLMessage> buildOfferRejectionReply(@NonNull final ACLMessage offerMessage) {
        final ACLMessage rejectOfferMessage = offerMessage.createReply();
        rejectOfferMessage.setPerformative(ACLMessage.REJECT_PROPOSAL);
        rejectOfferMessage.setLanguage(ReservationOntology.codec.getName());
        rejectOfferMessage.setOntology(ReservationOntology.instance.getName());

        return Try.success(rejectOfferMessage);
    }
}
