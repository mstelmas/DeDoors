package org.wsd.agents.lock.behaviours;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.wsd.agents.lock.reservations.ReservationOfferService;
import org.wsd.ontologies.MessageContentExtractor;
import org.wsd.ontologies.reservation.ReservationMessageFactory;
import org.wsd.ontologies.reservation.ReservationOffer;

import java.util.Objects;
import java.util.Vector;

@Slf4j
public class ReservationCNPNegotiatorBehaviour extends ContractNetInitiator {

    private final ReservationMessageFactory reservationMessageFactory;
    private final MessageContentExtractor messageContentExtractor;
    private final ACLMessage reservationRequestMessage;

    public ReservationCNPNegotiatorBehaviour(final Agent agent, final ACLMessage reservationRequestMessage, final ACLMessage cfpMessage) {
        super(agent, cfpMessage);
        this.reservationMessageFactory = new ReservationMessageFactory(agent);
        this.messageContentExtractor = new MessageContentExtractor(agent);
        this.reservationRequestMessage = reservationRequestMessage;
    }

    /* TODO: Timeout handling */

    @Override
    protected void handlePropose(final ACLMessage proposeMessage, final Vector acceptances) {
        messageContentExtractor.extract(proposeMessage, ReservationOffer.class).ifPresent(reservationOffer -> {
            log.info("Agent {} proposed an offer: {}", proposeMessage.getSender().getLocalName(), reservationOffer);
        });
    }

    @Override
    protected void handleRefuse(final ACLMessage refuse) {
        log.info("Agent {} refused to make an offer :/. Oh well...", refuse.getSender().getLocalName());
    }

    @Override
    protected void handleAllResponses(final Vector responses, final Vector acceptances) {
        log.info("Got a total of {} offers", responses.size());

        // TODO: inform lecturer that no offers have been found
        if (responses.isEmpty()) {
            log.info("No offers to choose from. Skipping...");
            return;
        }

        final ACLMessage bestOfferMessage = ((Vector<ACLMessage>) responses).stream().filter(m -> m.getPerformative() == ACLMessage.PROPOSE)
                .map(response -> Pair.of(messageContentExtractor.extract(response, ReservationOffer.class).get(),
                        response))
                .min((offer1, offer2) -> ReservationOfferService.reservationOfferScoreComparator
                        .compare(offer1.getLeft(), offer2.getLeft()))
                .map(Pair::getRight).get(); /* this should be safe and is guarded by an empty list check above */

        log.info("Best offer was provided by agent {}", bestOfferMessage.getSender().getLocalName());

        /* TODO:
         *   1. maybe move accepting/rejecting offers into separate (parallel) behaviours?
         *   2. best offer acceptance should NOT be done by a NEGOTIATOR - instead he should
         *      forward the offer to the LECTURER and let him decide
         */
        reservationMessageFactory.buildOfferAcceptanceReply(bestOfferMessage).onSuccess(acceptOfferAclMessage -> {
            myAgent.send(acceptOfferAclMessage);
            log.info("Best offer successfully accepted {}", acceptOfferAclMessage);
        }).onFailure(ex -> log.info("Could not accept best offer: {}", ex));

        /* TODO: Handling of offer rejection ? */
        ((Vector<ACLMessage>) responses).stream().filter(offer -> !Objects.equals(offer, bestOfferMessage))
                .forEach(offerToReject -> {
                    reservationMessageFactory.buildOfferRejectionReply(offerToReject)
                            .onSuccess(rejectOfferAclMessage -> {
                                myAgent.send(rejectOfferAclMessage);
                                log.info("Successfully rejected offer {} with {}", offerToReject,
                                        rejectOfferAclMessage);
                            }).onFailure(ex -> log.info("Could not reject offer {}: {}", offerToReject, ex));
                });

        //Sending inform message to lecturer agent
        messageContentExtractor.extract(bestOfferMessage, ReservationOffer.class).ifPresent(bestOffer -> {
            reservationMessageFactory.buildOfferReservationInform(reservationRequestMessage, bestOffer)
                    .onSuccess(offerReservationAclMessage -> {
                        myAgent.send(offerReservationAclMessage);
                        log.info("Succesfully send reservation details to lecturer: {}", offerReservationAclMessage);
                    }).onFailure(ex -> log.info("Could not get best offer: {}", ex));
        });
    }

}
