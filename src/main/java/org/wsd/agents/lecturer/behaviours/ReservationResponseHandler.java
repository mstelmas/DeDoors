package org.wsd.agents.lecturer.behaviours;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.API.run;
import static io.vavr.Predicates.instanceOf;

import org.wsd.agents.lecturer.LecturerAgent;
import org.wsd.agents.lecturer.reservations.Reservation;
import org.wsd.ontologies.MessageContentExtractor;
import org.wsd.ontologies.reservation.ConfirmReservationResponse;
import org.wsd.ontologies.reservation.ReservationOffer;
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
        final ACLMessage message = agent.receive();

        if (message == null) {
            block();
            return;
        }

        Try.of(() -> agent.getContentManager().extractContent(message))
        .onSuccess(contentObject -> dispatchIncomingMessage(message, contentObject))
        .onFailure(Throwable::printStackTrace);

    }

    private void onReservationReceived(ACLMessage message) {
        log.info("Received reservation offer from NEGOTIATOR {}", message);
        //TODO: Add reservation to LecturerAgent and its GUI
        messageContentExtractor.extract(message, ReservationOffer.class).ifPresent(offer -> {
            Reservation reservation = new Reservation(offer.getId(), offer.getLockAID());
            agent.addReservationOffer(reservation);
        });

    }

    private void onReservationConfimed(ACLMessage message) {
        //TODO: Show confirmed reservation in GUI
        log.info("Reservation confirmed succesfully: {}", message);
    }

    private void dispatchIncomingMessage(final ACLMessage message, ContentElement contentElement) {
        final Concept action = ((Action) contentElement).getAction();
        
        switch (message.getPerformative()) {
            case ACLMessage.INFORM:
                Match(action).of(
                        Case($(instanceOf(ReservationOffer.class)), o -> run(() -> onReservationReceived(message))),
                        Case($(instanceOf(ConfirmReservationResponse.class)), o -> run(() -> onReservationConfimed(message)))
                );
                break;
        }

    }
    
}
