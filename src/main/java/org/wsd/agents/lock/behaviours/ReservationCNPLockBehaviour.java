package org.wsd.agents.lock.behaviours;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import lombok.extern.slf4j.Slf4j;
import org.wsd.agents.lock.ReservationOfferService;
import org.wsd.ontologies.MessageContentExtractor;
import org.wsd.ontologies.reservation.ReservationDataRequest;
import org.wsd.ontologies.reservation.ReservationMessageFactory;

@Slf4j
public class ReservationCNPLockBehaviour extends ContractNetResponder {

    private final ReservationOfferService reservationOfferService = new ReservationOfferService();

    private final ReservationMessageFactory reservationMessageFactory;
    private final MessageContentExtractor messageContentExtractor;

    public ReservationCNPLockBehaviour(final Agent agent, final MessageTemplate messageTemplate) {
        super(agent, messageTemplate);
        this.reservationMessageFactory = new ReservationMessageFactory(agent);
        this.messageContentExtractor = new MessageContentExtractor(agent);
    }

    @Override
    protected ACLMessage handleCfp(final ACLMessage cfp) throws NotUnderstoodException {
        log.info("Handling reservation CFP from {} for reservation request: {}", cfp.getSender(), cfp);

        return messageContentExtractor.extract(cfp, ReservationDataRequest.class)
                .map(reservationDataRequest ->
                        reservationMessageFactory
                                .buildReservationOfferReply(cfp, reservationOfferService.scoreOffer(reservationDataRequest))
                                .getOrElse(() -> null)
                )
                .orElseThrow(() -> new NotUnderstoodException(cfp));
    }
}
