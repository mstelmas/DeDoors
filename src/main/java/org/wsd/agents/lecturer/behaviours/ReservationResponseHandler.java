package org.wsd.agents.lecturer.behaviours;

import io.vavr.control.Try;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import lombok.extern.slf4j.Slf4j;
import org.wsd.agents.lecturer.LecturerAgent;
import org.wsd.agents.lecturer.reservations.Reservation;
import org.wsd.ontologies.MessageContentExtractor;
import org.wsd.ontologies.reservation.*;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;
@Slf4j
public class ReservationResponseHandler extends CyclicBehaviour {

    private final LecturerAgent agent;
    private final MessageContentExtractor messageContentExtractor;
    
	private final MessageTemplate RESERVATION_MESSAGE_TEMPLATE = MessageTemplate.and(
                    MessageTemplate.MatchLanguage(ReservationOntology.codec.getName()),
                    MessageTemplate.MatchOntology(ReservationOntology.instance.getName())
            );

    public ReservationResponseHandler(final LecturerAgent agent) {
        super(agent);
        this.agent = agent;
        this.messageContentExtractor = new MessageContentExtractor(agent);
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
        log.info("Processing incoming message: {} with content: {}", message, contentElement);

        final Concept action = ((Action) contentElement).getAction();

        switch (message.getPerformative()) {
            case ACLMessage.INFORM:
                Match(action).of(
                        Case($(instanceOf(ReservationOffer.class)), o -> run(() -> onReservationReceived(message))),
                        Case($(instanceOf(ConfirmReservationResponse.class)), o -> run(() -> onReservationConfirmed(message))),
                        Case($(instanceOf(CancelReservationResponse.class)), o -> run(() -> handleCanceledReservation(message.getSender(), (CancelReservationResponse)action))),
                        Case($(), o -> run(() -> log.warn("No handlers found for incoming message: {}", message)))
                );
                break;
            case ACLMessage.REFUSE:
                Match(action).of(
                        Case($(instanceOf(RefuseReservationCancelationResponse.class)), o -> run(() -> handleRefusalToCancelReservation(message.getSender(), (RefuseReservationCancelationResponse) action))),
                        Case($(), o -> run(() -> log.warn("No handlers found for incoming message: {}", message)))
                );
                break;

            default:
                log.warn("No handlers found for incoming message: {}", message);
        }

    }

    private void onReservationReceived(ACLMessage message) {
        log.info("Received reservation offer from NEGOTIATOR {}", message);
        messageContentExtractor.extract(message, ReservationOffer.class).ifPresent(offer -> {
            Reservation reservation = new Reservation(offer.getId(), offer.getLockAID());
            agent.onReservationReceived(offer);
        });

    }

    private void onReservationConfirmed(ACLMessage message) {
        log.info("Reservation confirmed succesfully: {}", message);
        messageContentExtractor.extract(message, ConfirmReservationResponse.class).ifPresent(offer -> {
            agent.getReservationsStateService().add(new Reservation(offer.getReservationId(), message.getSender()));
            agent.updateReservations();
        });
    }

    private void handleCanceledReservation(final AID lock, final CancelReservationResponse cancelReservationResponse) {
        log.info("Reservation {} on {} successfully canceled!", cancelReservationResponse.getReservationId(), lock.getLocalName());
        agent.getReservationsStateService().remove(new Reservation(cancelReservationResponse.getReservationId(), lock));
        agent.updateReservations();
    }

    private void handleRefusalToCancelReservation(final AID lock, final RefuseReservationCancelationResponse refuseReservationCancelationResponse) {
        log.info("Lock {} refused to cancel reservation {}!", lock.getLocalName(), refuseReservationCancelationResponse.getReservationId());
        /* TODO: implement */
    }

}
