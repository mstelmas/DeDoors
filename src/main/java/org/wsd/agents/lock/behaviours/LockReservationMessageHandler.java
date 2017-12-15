package org.wsd.agents.lock.behaviours;

import io.vavr.control.Try;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.onto.basic.Action;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import lombok.extern.slf4j.Slf4j;
import org.wsd.agents.lock.LockAgent;
import org.wsd.ontologies.reservation.CancelReservationRequest;
import org.wsd.ontologies.reservation.ConfirmReservationRequest;
import org.wsd.ontologies.reservation.ReservationDataRequest;
import org.wsd.ontologies.reservation.ReservationOntology;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

@Slf4j
public class LockReservationMessageHandler extends CyclicBehaviour {

	private final MessageTemplate NEGOTIATOR_MESSAGE_TEMPLATE = MessageTemplate.and(
	        MessageTemplate.and(
                    MessageTemplate.MatchLanguage(ReservationOntology.codec.getName()),
                    MessageTemplate.MatchOntology(ReservationOntology.instance.getName())
            ),
            /* Reservation negotiations should be handled in a separate handler! */
            MessageTemplate.not(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET)));

	private final LockAgent agent;

	public LockReservationMessageHandler(final LockAgent agent) {
		super(agent);
		this.agent = agent;
	}

	@Override
	public void action() {
		final ACLMessage message = agent.receive(NEGOTIATOR_MESSAGE_TEMPLATE);

		if (message == null) {
			block();
			return;
		}

		Try.of(() -> agent.getContentManager().extractContent(message))
				.onSuccess(contentObject -> dispatchIncomingMessage(message, contentObject))
				.onFailure(Throwable::printStackTrace);
	}

	private void dispatchIncomingMessage(final ACLMessage message, ContentElement contentElement) {

		log.info("Processing message: {} with content: {}", message, contentElement);

		final Concept action = ((Action) contentElement).getAction();

		switch (message.getPerformative()) {
		case ACLMessage.REQUEST:
			Match(action).of(
					Case($(instanceOf(ReservationDataRequest.class)), o -> run(
							() -> agent.addBehaviour(new PerformReservationCNPBehaviour(agent, message.getSender(), (ReservationDataRequest) action)))),
					Case($(instanceOf(CancelReservationRequest.class)), o-> run(
							() -> agent.addBehaviour(new CancelReservationBehaviour(agent, message, (CancelReservationRequest) action)))),
					Case($(), o -> run(() -> replyNotUnderstood(message))));
			break;
		case ACLMessage.CONFIRM:
			Match(action).of(
				Case($(instanceOf(ConfirmReservationRequest.class)), o-> run(
							() -> agent.addBehaviour(new ConfirmReservationBehaviour(agent, message, (ConfirmReservationRequest) action)))),
				Case($(), o -> run(() -> replyNotUnderstood(message))));
		break;
		default:
			replyNotUnderstood(message);
		}
	}

	private void replyNotUnderstood(final ACLMessage message) {
		Try.run(() -> {
			final ACLMessage notUnderstoodReply = message.createReply();
			notUnderstoodReply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
			notUnderstoodReply.setContentObject(message.getContentObject());
			notUnderstoodReply.setOntology(ReservationOntology.instance.getName());
			notUnderstoodReply.setLanguage(ReservationOntology.codec.getName());
			myAgent.send(notUnderstoodReply);
		}).onFailure(ex -> log.error("Could not reply with Not Understood message: {}", ex));
	}

}
