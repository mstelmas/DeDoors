package org.wsd.agents.lock.behaviours;

import org.wsd.agents.lock.LockAgent;
import org.wsd.agents.lock.configuration.LockConfigurationProvider;
import org.wsd.agents.lock.reservations.ReservationOfferService;
import org.wsd.ontologies.MessageContentExtractor;
import org.wsd.ontologies.reservation.ReservationDataRequest;
import org.wsd.ontologies.reservation.ReservationMessageFactory;
import org.wsd.ontologies.reservation.ReservationOffer;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReservationCNPLockBehaviour extends ContractNetResponder {

    private final ReservationOfferService reservationOfferService = new ReservationOfferService();

    private final LockConfigurationProvider lockConfigurationProvider;
    private final ReservationMessageFactory reservationMessageFactory;
    private final MessageContentExtractor messageContentExtractor;
    private final LockAgent agent;

    public ReservationCNPLockBehaviour(final LockAgent agent, final MessageTemplate messageTemplate) {
        super(agent, messageTemplate);
        this.agent = agent;
        this.lockConfigurationProvider = new LockConfigurationProvider(agent);
        this.reservationMessageFactory = new ReservationMessageFactory(agent);
        this.messageContentExtractor = new MessageContentExtractor(agent);
    }

    @Override
    protected ACLMessage handleCfp(final ACLMessage cfp) {
        log.info("Handling reservation CFP from {} for reservation request: {}", cfp.getSender(), cfp);

        /* TODO:
            1. refactor
            2. maybe return NOT_UNDERSTOOD/FAILURE/REFUSE instead of silently terminating CNP?
        */
        return lockConfigurationProvider.provide()
                .map(lockConfiguration ->
                    messageContentExtractor.extract(cfp, ReservationDataRequest.class)
                            .map(reservationDataRequest ->
                                    reservationMessageFactory
                                            .buildReservationOfferReply(cfp,
                                                new ReservationOffer(reservationOfferService.scoreOffer(reservationDataRequest, lockConfiguration),
                                                                     lockConfiguration, agent.getAID(), agent.getNextId()))
                                            .getOrElse(() -> null)
                            )
                            .orElseGet(() -> null)
                )
                .orElseGet(() -> null);
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
        log.info("HANDLE ACCEPT PROPOSAL");

        return null;
    }
}
