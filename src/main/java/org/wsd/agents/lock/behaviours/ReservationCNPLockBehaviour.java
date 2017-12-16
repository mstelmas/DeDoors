package org.wsd.agents.lock.behaviours;

import org.wsd.agents.lock.LockAgent;
import org.wsd.agents.lock.configuration.LockConfiguration;
import org.wsd.agents.lock.configuration.LockConfigurationProvider;
import org.wsd.agents.lock.reservations.ReservationOfferService;
import org.wsd.agents.lock.validators.PermissionValidator;
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

        boolean meetsRequirements = messageContentExtractor.extract(cfp, ReservationDataRequest.class).map(request -> {
                return lockConfigurationProvider.provide().map(lockConfig -> {
                    return isMeetingRequirements(request, lockConfig);
                }).orElseGet(() -> {
                    log.info("Could not load lock configuration");
                    return false; });
            }
        ).orElseGet(() -> {
            log.info("Could not load message content");
            return false; });
        
        /* TODO:
            1. refactor
        */
        if(meetsRequirements) {
            return lockConfigurationProvider.provide().map(lockConfiguration -> messageContentExtractor
                    .extract(cfp, ReservationDataRequest.class)
                    .map(reservationDataRequest -> reservationMessageFactory.buildReservationOfferReply(cfp,
                            new ReservationOffer(
                                    reservationOfferService.scoreOffer(reservationDataRequest, lockConfiguration),
                                    lockConfiguration, agent.getAID(), agent.getNextId()))
                            .getOrElse(() -> null))
                    .orElseGet(() -> null)).orElseGet(() -> null);
        } else {
            log.info("Refusing reservation");
            return reservationMessageFactory.buildRefuseReservationResponse(cfp);
        }
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
        log.info("HANDLE ACCEPT PROPOSAL");

        return null;
    }

    private boolean isMeetingRequirements(ReservationDataRequest request, LockConfiguration lockConfig) {
        log.info("Check if room meets reqirements: {}, lock configuration {}", request, lockConfig);

        if(!PermissionValidator.isPermissionsValid(request.getCertificate(), lockConfig.getRequiredAuthorizationLevel())) {
            return false;
        }

        if(request.getNumberOfParticipants() > lockConfig.getNumberOfSeats()) {
            return false;
        }
        if(null != request.getIsLaboratory()) {
            if(request.getIsLaboratory() && !lockConfig.getIsLaboratory())
                return false;
        }
        if(null != request.getIsSeminaryHall()) {
            if(request.getIsSeminaryHall() && !lockConfig.getIsSeminaryHall())
                return false;
        }
        if(null != request.getNumberOfComputers()) {
            if(request.getNumberOfComputers() > lockConfig.getNumberOfComputers())
                return false;
        }
        if(null != request.getIsTVRequired()) {
            if(request.getIsTVRequired() && !lockConfig.getHasTV())
                return false;
        }
        if(null != request.getIsMultimediaProjectorRequired()) {
            if(request.getIsMultimediaProjectorRequired() && !lockConfig.getHasMultimediaProjector())
                return false;
        }
        return true;
    }
}
