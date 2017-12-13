package org.wsd.agents.lock.behaviours;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.API.run;
import static io.vavr.Predicates.instanceOf;

import org.wsd.agents.lock.LockAgent;
import org.wsd.ontologies.otp.GenerateOTPRequest;
import org.wsd.ontologies.reservation.ReservationDataRequest;
import org.wsd.ontologies.reservation.ReservationOntology;

import io.vavr.control.Try;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.onto.basic.Action;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LockReservationMessageHandler extends CyclicBehaviour {

	private final MessageTemplate RESERVATION_MESSAGE_TEMPLATE = MessageTemplate.and(
			MessageTemplate.MatchLanguage(ReservationOntology.codec.getName()),
			MessageTemplate.MatchOntology(ReservationOntology.instance.getName()));

	private final LockAgent agent;

	public LockReservationMessageHandler(final LockAgent agent) {
		super(agent);
		this.agent = agent;
	}

	@Override
	public void action() {
		final ACLMessage message = agent.receive(RESERVATION_MESSAGE_TEMPLATE);

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
					Case($(instanceOf(ReservationDataRequest.class)), run(
							() -> agent.addBehaviour(new PerformCNPbehaviour(agent, (ReservationDataRequest) action)))),
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
